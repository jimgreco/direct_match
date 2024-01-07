package com.core.connector.mold;

import com.core.app.CommandException;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 7/12/15.
 */
public class Mold64UDPPacket implements Mold64PacketReader, Mold64PacketWriter {
    public static final int MTU_SIZE = 1400;

    public static final int SESSION_POSITION = 0;
    public static final int SEQUENCE_POSITION = 10;
    public static final int MSG_COUNT_POSITION = 18;

    public static final int SESSION_LENGTH = 10;
    public static final int SEQUENCE_LENGTH = 8;
    public static final int MSG_COUNT_LENGTH = 2;
    public static final int HEADER_LENGTH = 20;

    private final Log log;
    private final ByteBuffer session = ByteBuffer.allocate(SESSION_LENGTH);
    private ByteBuffer datagram = ByteBuffer.allocateDirect(MTU_SIZE);

    private long seqNum;
    private int numMessages;
    private int currentLimit;

    public Mold64UDPPacket(Log log) {
        this.log = log;
    }

    //
    // Packet reader functionality
    //

    @Override
    public boolean wrap(ByteBuffer datagram) {
        currentLimit = 0;

        if (datagram.remaining() < HEADER_LENGTH) {
            log.error(log.log().add("Datagram with too few bytes in header. Expected >= 20, Received ")
                    .add(datagram.remaining()));
            return false;
        }

        this.datagram = datagram;

        int limit = datagram.limit();
        datagram.limit(SESSION_LENGTH);
        session.clear();
        session.put(datagram);
        session.flip();
        datagram.limit(limit);

        datagram.position(SEQUENCE_POSITION);
        seqNum = datagram.getLong();
        numMessages = datagram.getShort();
        return true;
    }

    @Override
    public ByteBuffer getSession() {
        return session;
    }

    @Override
    public long getStreamSeq() {
        return seqNum;
    }

    @Override
    public int getMsgCount() {
        return numMessages;
    }

    @Override
    public boolean hasMessages() {
        return numMessages > 0;
    }

    @Override
    public ByteBuffer getMessage() {
        if (currentLimit != 0) {
            datagram.position(datagram.limit());
            datagram.limit(currentLimit);
        }

        if (datagram.remaining() < 2) {
            log.error(log.log().add("Malformed messages in datagram. Expected at least 2 bytes for message length and received ").add(datagram.remaining()));
            return null;
        }

        int currentLength = datagram.getShort();

        if (currentLength == 0) {
            log.error(log.log().add("Message with 0 bytes"));
            return null;
        }

        if (currentLength < 0) {
            log.error(log.log().add("Message with negative bytes"));
            return null;
        }

        if (datagram.remaining() < currentLength) {
            log.error(log.log().add("Malformed messages in datagram. Expected ").add(currentLength)
                    .add(", Received ").add(datagram.remaining()));
            return null;
        }

        numMessages--;
        currentLimit = datagram.limit();
        datagram.limit(datagram.position() + currentLength);
        return datagram;
    }

    //
    // Command Sender / Re-request functionality
    //

    @Override
    public void init(String session) {
        if (session.length() != SESSION_LENGTH) {
            throw new CommandException("Session must be of length " + SESSION_LENGTH);
        }

        clear();

        BinaryUtils.copy(datagram, session);
        datagram.putLong(0);
        datagram.putShort((short) 0);  // msg count
    }

    @Override
    public void init(String session, long seqNum, int messages) {
        clear();

        this.numMessages = messages;

        init(datagram, session, seqNum, messages);
    }

    public static void init(ByteBuffer datagram, String session, long seqNum, int messages) {
        if (session==null || session.length() != SESSION_LENGTH) {
            throw new CommandException("Session cannot be null and must be of length " + SESSION_LENGTH);
        }

        BinaryUtils.copy(datagram, session);
        datagram.putLong(seqNum);
        datagram.putShort((short) messages);  // msg count
    }

    @Override
    public void addMessage(ByteBuffer msgBuffer) {
        numMessages++;

        // length of message
        datagram.putShort((short) msgBuffer.remaining());
        // message itself
        datagram.put(msgBuffer);
    }

    @Override
    public ByteBuffer getDatagram() {
        datagram.flip();
        datagram.putShort(datagram.position() + MSG_COUNT_POSITION, (short) numMessages);
        return datagram;
    }

    @Override
    public ByteBuffer getMessageBuffer() {
        return datagram;
    }

    @Override
    public void addMessage() {
        numMessages++;
    }

    @Override
    public int remaining() {
        return datagram.remaining();
    }

    @Override
    public void clear() {
        datagram.clear();
        numMessages = 0;
    }

    @Override
    public boolean hasAddedMessages() {
        return numMessages > 0;
    }
}
