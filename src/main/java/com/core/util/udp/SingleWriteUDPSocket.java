package com.core.util.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by jgreco on 8/10/15.
 */
public class SingleWriteUDPSocket implements SimpleWriteUDPSocket {
    private final WritableUDPSocket socket;
    private final String multicastGroup;
    private final int recvPort;
    private final InetSocketAddress sendAddress;
    private final String intf;
    private boolean channelOpened;

    public SingleWriteUDPSocket(WritableUDPSocket socket,
                                String intf,
                                String multicastGroup,
                                int sendPort) {
        this(socket, intf, multicastGroup, sendPort, 0);
    }

    public SingleWriteUDPSocket(WritableUDPSocket socket,
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
    public boolean write(ByteBuffer output) {
        if (channelOpened &&!canWrite()) {
            return false;
        }
        return socket.write(output, sendAddress);
    }



    @Override
    public boolean canWrite() {
        return sendAddress != null && socket.canWrite();
    }

    @Override
    public void open() throws IOException {

        socket.open();
        if (recvPort != 0) {
            socket.bind(intf, multicastGroup, (short) recvPort);
        }
        else {
            socket.bind(intf, multicastGroup, (short)0);
        }        channelOpened = true;
    }

    @Override
    public void close() {
        socket.close();
        channelOpened = false;
    }
}
