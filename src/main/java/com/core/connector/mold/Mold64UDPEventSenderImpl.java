package com.core.connector.mold;

import com.core.sequencer.BackupQueue;
import com.core.util.TimeUtils;
import com.core.util.log.Log;
import com.core.util.store.IndexedStore;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerHandler;
import com.core.util.time.TimerService;
import com.core.util.udp.*;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class Mold64UDPEventSenderImpl implements Mold64UDPEventSender, UDPSocketWriteListener, TimerHandler {
    private final ByteBuffer datagram = ByteBuffer.allocateDirect(2 * Mold64UDPPacket.MTU_SIZE);
    private final ByteBuffer message = ByteBuffer.allocate(Mold64UDPPacket.MTU_SIZE);
    
    private final SingleWriteUDPSocket socket;

    private  String session;

    private final IndexedStore store;

    private int nextSeqNumToSend = 1;
    private int msgCount;

    private final TimerService timers;
    private final TimeSource time;
    private final long timeout;
    private long lastMessageTimestamp = 0;

	private final Log logger;
    private boolean sendEnabled;

    private final BackupQueue backupQueue;
    public Mold64UDPEventSenderImpl(Log logger, TimeSource timeSource,
                                TimerService timerService,
                                int secondsTimeout,
                                UDPSocketFactory socketFactory,
                                String intf,
                                String host,
                                short port,
                                IndexedStore store,
                                    BackupQueue queue) throws IOException {
        this.logger = logger;
		this.time = timeSource;
        this.timers = timerService;
        this.backupQueue = queue;

        this.store = store;
        this.socket = new SingleWriteUDPSocket(socketFactory.createWritableUDPSocket(this), intf, host, port);

        this.timeout = secondsTimeout * TimeUtils.NANOS_PER_SECOND;
        this.timers.scheduleTimer(timeout, this);
    }

    @Override
    public void open() throws IOException {
        nextSeqNumToSend = store.size() + 1;
        socket.open();
    }

    @Override
    public void close() {
        socket.close();
    }

    public void setSendEnabled(boolean enabled) {
        this.sendEnabled =enabled;
    }

    @Override
    public String getSession() {
        return session;
    }
    @Override
    public void setSession(String sessionString) {
        session=sessionString;
    }

    private boolean canWrite() {
        return socket.canWrite();
    }

    void sendHeartbeat() {
        prepPacket(store.size() + 1, 0);
        flush();
    }

    @Override
	public ByteBuffer startMessage() {
        prepPacket(store.size() + 1, msgCount);

        // message length
        datagram.putShort((short)0);
        return datagram;
    }

    @Override
	public void finalizeMessage(int length) {
        if (datagram.position() + Short.BYTES + length > Mold64UDPPacket.MTU_SIZE) {
            int startOfNewMessagePosition = datagram.position();
            flush();

            // write the packet header before the message
            int newPacketPosition = startOfNewMessagePosition - Short.BYTES - Mold64UDPPacket.HEADER_LENGTH;
            datagram.position(newPacketPosition);

            Mold64UDPPacket.init(datagram, session, store.size() + 1, 0);

            // move header and message to front of packet
            datagram.position(newPacketPosition);
            datagram.limit(startOfNewMessagePosition + length);
            datagram.compact();

            // move position to start of message
            datagram.position(Mold64UDPPacket.HEADER_LENGTH + Short.BYTES);
        }

        msgCount++;
        datagram.putShort(datagram.position() - Short.BYTES, (short) length);
        store(length);
        datagram.position(datagram.position() + length);
    }

    private void store(int length) {
        int position = datagram.position();
        datagram.limit(position + length);
        datagram.position(position);
        if(!sendEnabled) {
            backupQueue.add(datagram);

        }else{
            //The reason we dont write to store is because we are doing that in the sequencer
            //backup event handler and we dont want to write twice
            store.add(datagram);
        }

        datagram.position(position);
        datagram.limit(datagram.capacity());
    }
    public boolean isSenderEnabled(){
        return sendEnabled;
    }

    @Override
	public void flush() {
        if (!canWrite()) {
            resetMsgCount();
            return;
        }

        if (isQueued()) {
            resetMsgCount();
            sendQueue();
            return;
        }
        //if not primary, we dont send anything, but we know that it is put in the queue
        if(!sendEnabled){
           return;
        }

        datagram.flip();
        datagram.putShort(Mold64UDPPacket.MSG_COUNT_POSITION, (short)msgCount);
        resetMsgCount();

        if (socket.write(datagram)) {
            nextSeqNumToSend = store.size() + 1;
            lastMessageTimestamp = time.getTimestamp();
        }
    }

    @Override
    public int getNextSeqNumToSend() {
        return nextSeqNumToSend;
    }

    private void resetMsgCount() {
        msgCount = 0;
    }

    private void sendQueue() {
        while (isQueued()) {
            prepPacket(nextSeqNumToSend, 0);
            message.clear();
            store.get(nextSeqNumToSend, message);
            message.flip();
            datagram.putShort((short) message.limit());
            datagram.put(message);
            datagram.flip();
            datagram.putShort(Mold64UDPPacket.MSG_COUNT_POSITION, (short)1);

            if (socket.write(datagram)) {
                nextSeqNumToSend++;
                lastMessageTimestamp = time.getTimestamp();
            }
            else {
                return;
            }
        }
    }

    private boolean isQueued() {
        return (nextSeqNumToSend + msgCount) < (store.size() + 1);
    }

    private void prepPacket(int seqNum, int msgCount) {
        if (msgCount == 0) {
            datagram.clear();
            Mold64UDPPacket.init(datagram, session, seqNum, 0);
        }
    }


    @Override
    public void onWriteAvailable(WritableUDPSocket clientSocket) {
        if (canWrite()) {
            sendQueue();
        }
    }

    @Override
    public void onWriteUnavailable(WritableUDPSocket clientSocket) {
    }

    @Override
    public void onTimer(int internalTimerID, int referenceData) {
        if (canWrite() && time.getTimestamp() - lastMessageTimestamp > timeout) {
            sendHeartbeat();
        }

        timers.scheduleTimer(timeout, this);
    }
}
