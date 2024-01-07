package com.core.nio;

import com.core.app.CommandException;
import com.core.connector.mold.Mold64UDPPacket;
import com.core.util.log.Log;
import com.core.util.udp.ReadWriteUDPSocket;
import com.core.util.udp.UDPSocketReadWriteListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Enumeration;

/**
 * User: jgreco
 * The common use case of this socket is joining and read/writing from a single multicast host:port
 */
class SelectorReadWriteUDPSocket implements ReadWriteUDPSocket, SelectorHandler {
    private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(Mold64UDPPacket.MTU_SIZE);
    private final Log log;
    private final UDPSocketReadWriteListener listener;
    private final SelectableChannelService selector;

    private DatagramChannel channel;
    private boolean writeable;

    public SelectorReadWriteUDPSocket(SelectableChannelService selectorService, Log log, UDPSocketReadWriteListener listener) {
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
        readBuffer.clear();

        SocketAddress lastRecvAddr;
        try {
            lastRecvAddr = channel.receive(readBuffer);
        } catch (IOException e) {
            log.error(log.log().add("Error reading channel ").add(e));
            return;
        }

        readBuffer.flip();
        if (readBuffer.hasRemaining()) {
            listener.onDatagram(this, readBuffer, (InetSocketAddress)lastRecvAddr);
        }
    }

    @Override
    public void onWrite() {
        writeable = true;
        listener.onWriteAvailable(this);
    }

    @Override
    public void enableRead(boolean val) {
        selector.enableRead(channel, val);
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
        join(intf,host,port);
    }

    @Override
    public boolean canWrite() {
        return writeable;
    }

    @Override
    public void join(String intf, String host) throws IOException {
        join(intf, host, (short)0);
    }

    @Override
    public void join(String intf, String host, short port) throws IOException {
        log.info(log.log().add("Joining multicast channel: ").add(host).add(":").add(port).add(" on ").add(intf));

        InetAddress group = InetAddress.getByName(host);
        NetworkInterface networkInterface;
        if (intf != null) {
            networkInterface = NetworkInterface.getByName(intf);
        }
        else {
            networkInterface = printAndLookupInterfaces();
        }

        if (host.equals("224.0.0.1")) {
            channel.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, Boolean.TRUE);
        }

        if (port != 0) {
            log.info(log.log().add("Binding multicast port: ").add(port));
            channel.bind(new InetSocketAddress(port));
        }

        channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
        channel.join(group, networkInterface);
        writeable = true;
    }

    private NetworkInterface printAndLookupInterfaces() throws SocketException {
        log.info(log.log().add("Interfaces"));

        NetworkInterface networkInterface = null;
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface ni = networkInterfaces.nextElement();

            log.info(log.log().add(ni.toString())
                    .add(" multicast=").add(ni.supportsMulticast())
                    .add(", loopback=").add(ni.isLoopback())
                    .add(", virtual=").add(ni.isVirtual())
                    .add(", up=").add(ni.isUp()));

            if (ni.supportsMulticast() && networkInterface == null) {
                networkInterface = ni;
            }
        }
        return networkInterface;
    }
}
