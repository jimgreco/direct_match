package com.core.fix.store;

import com.core.fix.FixWriter;
import com.core.fix.connector.FixConnector;
import com.core.util.BinaryUtils;
import com.gs.collections.impl.list.mutable.FastList;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * User: jgreco
 */
public class FIXMemoryStore implements FixStore {
    private final ByteBuffer messageBuffer = ByteBuffer.allocateDirect(1024 * 1024);
    private final FixWriter writer;
    private final FixConnector connector;
    private List<String> messages = new FastList<>();

    public FIXMemoryStore(FixWriter fixWriter, FixConnector fixConnector) {
        this.writer = fixWriter;
        this.connector = fixConnector;
    }

    @Override
    public void init(FixWriter initWriter, FixConnector initConnector) {
    }

    @Override
    public void reset(int seqNo) {
        messages = messages.subList(0, seqNo);
    }

    @Override
    public int resend(int beginSeqNo, int endSeqNo) {
        return endSeqNo;
    }

    @Override
    public FixWriter createMessage(char msgType) {
        writer.initFix(msgType, getNextOutboundSeqNo());
        return writer;
    }

    @Override
    public FixWriter createMessage(String msgType) {
        writer.initFix(msgType, getNextOutboundSeqNo());
        return writer;
    }

    @Override
    public void finalizeBusinessMessage() {
        ByteBuffer fixBody = writer.getFixBody();

        messageBuffer.clear();
        writer.buildFixMessage(messageBuffer, fixBody, false);
        messageBuffer.flip();

        String s = BinaryUtils.toString(messageBuffer);
        messages.add(s);

        connector.send(messageBuffer);
    }

    @Override
    public void finalizeAdminMessage() {
        ByteBuffer fixBody = writer.getFixBody();

        messageBuffer.clear();
        writer.buildFixMessage(messageBuffer, fixBody, false);
        messageBuffer.flip();

        String s = BinaryUtils.toString(messageBuffer);
        messages.add(s);

        connector.send(messageBuffer);
    }

    @Override
    public void writeAdminMessage() {
        // nothing
    }

    @Override
    public int getNextOutboundSeqNo() {
        return messages.size() + 1;
    }
}
