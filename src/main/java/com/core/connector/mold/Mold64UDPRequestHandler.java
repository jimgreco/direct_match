package com.core.connector.mold;

import com.core.connector.SessionSourceListener;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;
import com.core.util.store.IndexedStore;
import com.core.util.udp.ReadWriteUDPSocket;
import com.core.util.udp.UDPSocketFactory;
import com.core.util.udp.UDPSocketReadWriteListener;
import com.core.util.udp.WritableUDPSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class Mold64UDPRequestHandler implements
        SessionSourceListener,
        UDPSocketReadWriteListener {
    private final Mold64PacketReader recvPacket;
    private final Mold64PacketWriter sendPacket;

    private final Log log;
    private final IndexedStore storage;
    private ReadWriteUDPSocket socket;
    private final UDPSocketFactory socketFactory;

    private String session;

    private final String intf;
    private final String host;
    private final short recvPort;
	private final InetSocketAddress multicastSendAddress;

    public Mold64UDPRequestHandler(UDPSocketFactory socketFactory,
                                   Log log,
                                   IndexedStore storage,
                                   String intf,
                                   String host,
                                   short rerequestRecvPort,
                                   short rerequestSendPort) {
        this(socketFactory, log, storage, null, intf, host, rerequestRecvPort, rerequestSendPort);
    }

    public Mold64UDPRequestHandler(UDPSocketFactory socketFactory,
                                   Log log,
                                   IndexedStore storage,
                                   String session,
                                   String intf,
                                   String host,
                                   short rerequestRecvPort,
                                   short sendPort) {
        this.storage = storage;
        this.log = log;
        this.session = session;
        this.intf = intf;
        this.host = host;
        this.recvPort = rerequestRecvPort;
        this.recvPacket = new Mold64UDPPacket(log);
        this.sendPacket = new Mold64UDPPacket(log);
        this.socketFactory = socketFactory;
        this.multicastSendAddress = new InetSocketAddress(host, sendPort);
    }

    public void open() throws IOException {
        socket = socketFactory.createReadWriteUDPSocket(this);
        socket.open();
        socket.join(intf, host, recvPort);
        socket.enableRead(true);
    }

    public void close() {
        if (isOpen()) {
            socket.close();
        }
    }

    @Override
    public void onDatagram(ReadWriteUDPSocket clientSocket, ByteBuffer datagram, InetSocketAddress addr) {
        if (!canWrite()) {
            log.error(log.log().add("Received UDP re-request but inactive. CanWrite=").add(canWrite()).add(", IsOpen=").add(isOpen()));
            return;
        }

        if (!recvPacket.wrap(datagram)) {
            return;
        }

        if (!BinaryUtils.compare(recvPacket.getSession(), session)) {
            log.error(log.log().add("Invalid session. Expected=").add(this.session)
                    .add(" Recv=").add(recvPacket.getSession()));
            return;
        }

        long seqNum = recvPacket.getStreamSeq();
        int messageCount = recvPacket.getMsgCount();

        sendPacket.init(session, seqNum, 0);

        for (long i=seqNum; i<seqNum + messageCount; i++) {
            int size = storage.getSize(i);
            if (size == 0) {

                break;
            }

            if (sendPacket.remaining() < Short.BYTES + size) {
                if(log.isDebugEnabled()){
                    log.debug(log.log().add("Send Packet do not have enough capacity to send all the msgs.Current Msg:").add(i-1).add(" starting at :").add(seqNum).add(" of total: ").add(messageCount));
                }
                break;
            }

            storage.get(i, sendPacket.getMessageBuffer());
            sendPacket.addMessage();
        }

        ByteBuffer result = sendPacket.getDatagram();

        if (socket.write(result, multicastSendAddress)) {
            result.clear();
        } else {
            log.error(log.log().add("UDP buffer overflow. Stop writing"));
        }
    }

    @Override
    public void onWriteAvailable(WritableUDPSocket clientSocket) {
        log.error(log.log().add("UDP buffer cleared. Writing again."));
    }

    @Override
    public void onWriteUnavailable(WritableUDPSocket clientSocket) {
    }

    public boolean isOpen() {
        return socket != null;
    }

    public boolean canWrite() {
        return isOpen() && socket.canWrite();
    }



    @Override
    public void onSessionDefinition(String session) {
        this.session = session;
    }
}
