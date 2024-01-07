package com.core.connector.mold;

import com.core.util.log.Log;
import com.core.util.udp.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class Mold64UDPConnection implements Mold64UDP, UDPSocketReadWriteListener {
    private final Mold64PacketReader recvPacket;

    private final Log log;
    private final String multicastHost;

    private SimpleReadWriteUDPSocket socket;
    private Mold64UDPListener listener;

    public Mold64UDPConnection(UDPSocketFactory socketFactory,
                               Log log,
                               String intf,
                               String recvHost,
                               short recvPort) throws IOException {
        this.log = log;
        this.recvPacket = new Mold64UDPPacket(log);
        this.multicastHost=recvHost;
        this.socket = new SingleReadUDPSocket(socketFactory.createReadWriteUDPSocket(this), intf, recvHost, recvPort);
    }

    @Override
    public void setListener(Mold64UDPListener listener) {
        this.listener = listener;
    }

    @Override
    public void open() throws IOException {
        this.socket.open();
        this.socket.enableRead(true);
    }

    @Override
    public void close() {
        socket.close();
    }

    @Override
    public String getMulticastGroup() {
        return multicastHost;
    }

    @Override
    public void onDatagram(ReadWriteUDPSocket clientSocket, ByteBuffer datagram, InetSocketAddress addr) {
        if (!recvPacket.wrap(datagram)) {
            log.error(log.log().add("Datagram with too few bytes in header. Expected >= 20, Received ")
                    .add(datagram.remaining()));
            return;
        }

        // copy the session
        ByteBuffer session = recvPacket.getSession();
        long streamSeq = recvPacket.getStreamSeq();
        int messages = recvPacket.getMsgCount();
        long nextSeqNum = streamSeq + messages;
        boolean processMessages = false;

        if (listener != null) {
            processMessages = listener.onMold64Packet(session, streamSeq);
        }

        while (recvPacket.hasMessages()) {
            ByteBuffer message = recvPacket.getMessage();
            if (message == null) {
                return;
            }

            if (listener != null && processMessages) {
                listener.onMold64Message(streamSeq++, datagram);
            }
        }

        if (listener != null && processMessages) {
            listener.onMold64PacketComplete(session, nextSeqNum);
        }
    }

    @Override
    public void onWriteAvailable(WritableUDPSocket clientSocket) {
        // nothing
    }

    @Override
    public void onWriteUnavailable(WritableUDPSocket clientSocket) {
        // nothing
    }
}
