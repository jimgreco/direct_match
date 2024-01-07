package com.core.nio;

import com.core.app.CommandException;
import com.core.connector.mold.Mold64UDPPacket;
import com.core.util.log.Log;
import com.core.util.udp.UDPSocketReadWriteListener;
import com.core.util.udp.UDPSocketWriteListener;
import com.core.util.udp.WritableUDPSocket;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * User: jgreco
 * The common use case of this socket is joining and read/writing from a single multicast host:port
 */
class SelectorWriteUDPSocket implements WritableUDPSocket, SelectorHandler {
    private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(Mold64UDPPacket.MTU_SIZE);
    private final Log log;
    private final UDPSocketWriteListener listener;
    private final SelectableChannelService selector;

    private DatagramChannel channel;
    private boolean writeable;

    public SelectorWriteUDPSocket(SelectableChannelService selectorService, Log log, UDPSocketWriteListener listener) {
        this.selector = selectorService;
        this.log = log;
        this.listener = listener;
    }

    @Override
    public void open() throws IOException {
        if (channel != null) {
            throw new CommandException("Tried to call open() on DatagramChannel twice");
        }

        channel = DatagramChannel.open(StandardProtocolFamily.INET);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
        channel.configureBlocking(false);
        //channel.socket().setReuseAddress(true);
        channel.register(selector.getSelector(), 0, this);

    }

    @Override
    public void close() {
        writeable = false;

        if (channel == null) {
            return;
        }

        try {
            log.info(log.log().add("Closing multicast channel"));
            channel.close();
            selector.free(channel);
            channel = null;
        } catch (IOException e) {
            // fail silently
        }
    }

    @Override
    public void onAccept() {
        throw new RuntimeException("SelectorReadWriteUDPSocket cannot call onAccept()");
    }

    @Override
    public void onConnect() {
        throw new RuntimeException("SelectorReadWriteUDPSocket cannot call onConnect()");
    }

    @Override
    public void onRead() {

    }

    @Override
    public void onWrite() {
        writeable = true;
        listener.onWriteAvailable(this);
    }



    private void enableWrite(boolean val) {
        selector.enableWrite(channel, val);
    }

    @Override
    public boolean write(ByteBuffer output, InetSocketAddress address) {
        if (!writeable) {
            log.error(log.log().add("Tried to write to channel which is not joined"));
            return false;
        }

        int remaining = output.remaining();
        if (remaining == 0) {
            log.error(log.log().add("Tried to send a message with zero bytes"));
            return true;
        }

        try {
            if (channel.send(output, address) <= 0) {
                log.info(log.log().add("UDP could not write ")
                        .add(remaining).add(" bytes to channel ")
                        .add(address.getHostName()).add(":").add(address.getPort()));
                listener.onWriteUnavailable(this);
                writeable = false;
                enableWrite(true);
                return false;
            }

            return true;
        } catch (IOException e) {
            log.error(log.log().add("Error writing to channel").add(e));
            return false;
        }
    }

    @Override
    public void bind(String intf, String host, short port) throws IOException {
        log.info(log.log().add("Joining multicast channel: ").add(host).add(":").add(port).add(" on ").add(intf));

        InetAddress group = InetAddress.getByName(host);
        NetworkInterface networkInterface = NetworkInterface.getByName(intf);


        if (host.equals("224.0.0.1")) {
            channel.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, Boolean.TRUE);
        }

        if (port != 0) {
            log.info(log.log().add("Binding multicast port: ").add(port));
            channel.bind(new InetSocketAddress(port));
        }

        channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
        writeable = true;
    }

    @Override
    public boolean canWrite() {
        return writeable;
    }



}
