package com.core.connector.mold;

import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.match.sequencer.BackupEventQueueController;
import com.core.sequencer.BackupQueueListener;
import com.gs.collections.impl.list.mutable.FastList;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by jgreco on 7/23/15.
 */
public class StubMold64UDPEventSender implements Mold64UDPEventSender,BackupEventQueueController {
    private String session;
    private ByteBuffer lastMessage;
    private boolean flushed;
    private final List<ByteBuffer> buffers = new FastList<>();
    private         boolean senderEnabled;


    public void setSession(String session) {
        this.session = session;
    }

    @Override
    public ByteBuffer startMessage() {
        return lastMessage = ByteBuffer.allocate(1500);
    }

    @Override
    public void finalizeMessage(int length) {
        lastMessage.limit(length);

        ByteBuffer allocate = ByteBuffer.allocate(length);
        allocate.put(lastMessage);
        allocate.flip();

        buffers.add(allocate);
    }

    @Override
    public String getSession() {
        return session;
    }

    @Override
    public void open() throws IOException {

    }

    @Override
    public void close() {

    }

    @Override
    public void setSendEnabled(boolean isPrimary) {
        senderEnabled=isPrimary;
    }

    @Override
    public boolean isSenderEnabled() {
        return senderEnabled;
    }

    @Override
    public void flush() {
        flushed = true;
    }

    @Override
    public int getNextSeqNumToSend() {
        return 0;
    }

    public ByteBuffer pop() {
        return buffers.remove(0);
    }

    public int size() {
        return buffers.size();
    }

    public boolean isFlushed() {
        return flushed;
    }

    @Override
    public void verifyOrderEvent(MatchOrderEvent receivedEvent) {

    }

    @Override
    public void verifyReplaceEvent(MatchReplaceEvent receivedEvent) {

    }

    @Override
    public void verifyCancelEvent(MatchCancelEvent receivedEvent) {

    }

    @Override
    public void verifyFillEvent(MatchFillEvent receivedEvent) {

    }

    @Override
    public void sendRemaining() {

    }

    @Override
    public void addBackupQueueListener(BackupQueueListener listener) {

    }
}
