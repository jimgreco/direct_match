package com.core.util.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by jgreco on 8/10/15.
 */
public class SingleReadWriteUDPSocket implements SimpleReadWriteUDPSocket {
    private final ReadWriteUDPSocket socket;
    private final String multicastGroup;
    private final int recvPort;
    private final InetSocketAddress sendAddress;
    private final String intf;
    private boolean joined;

    public SingleReadWriteUDPSocket(ReadWriteUDPSocket socket,
                                    String intf,
                                    String multicastGroup,
                                    int sendPort) {
        this(socket, intf, multicastGroup, sendPort, 0);
    }

    public SingleReadWriteUDPSocket(ReadWriteUDPSocket socket,
                                    String intf,
                                    String multicastGroup,
                                    int sendPort,
                                    int recvPort) {
        this.socket = socket;
        this.intf = intf;
        this.multicastGroup = multicastGroup;
        this.recvPort = recvPort;
        this.sendAddress = sendPort != 0 ? new InetSocketAddress(multicastGroup, sendPort) : null;
    }

    @Override
    public boolean hasJoined() {
        return joined;
    }

    @Override
    public boolean write(ByteBuffer output) {
        if (!canWrite()) {
            return false;
        }

        return socket.write(output, sendAddress);
    }

    @Override
    public void enableRead(boolean val) {
        socket.enableRead(val);
    }

    @Override
    public boolean canWrite() {
        return hasJoined() && sendAddress != null && socket.canWrite();
    }

    @Override
    public void open() throws IOException {
        if (hasJoined()) {
            throw new IOException("Cannot re-open() when joined to multicast group");
        }

        socket.open();
        if (recvPort != 0) {
            socket.join(intf, multicastGroup, (short) recvPort);
        }
        else {
            socket.join(intf, multicastGroup);
        }
        joined = true;
    }

    @Override
    public void close() {
        socket.close();
        joined = false;
    }
}
