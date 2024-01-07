package com.core.fix;

import com.core.fix.connector.FixConnector;
import com.core.fix.store.FixStore;
import com.core.util.time.SimulatedTimeSource;
import com.core.util.time.SystemTimeSource;

/**
 * Created by jgreco on 1/7/15.
 */
public class StubFIXStore implements FixStore {
    private int nextOutboundSeqNo = 1;
    private int resendResult = 0;
    private int lastBeginSeqNo = 0;
    private int lastEndSeqNo = 0;
    private boolean resetFlag=false;
    private FixConnector connector;
    private InlineFixWriter writer;

    public void setResendResult(int resendResult) {
        this.resendResult = resendResult;
    }

    @Override
    public void init(FixWriter writer, FixConnector connector) {
        this.connector = connector;
    }

    @Override
    public int resend(int beginSeqNo, int endSeqNo) {
        this.lastBeginSeqNo = beginSeqNo;
        this.lastEndSeqNo = endSeqNo;
        return resendResult;
    }

    @Override
    public void reset(int seqNo) {
        resetFlag=true;
    }

    @Override
    public FixWriter createMessage(char msgType) {
        return writer = new InlineFixWriter(new SystemTimeSource(), 2, "SENDER", "TARGET");
    }

    @Override
    public FixWriter createMessage(String msgType) {
        return writer = new InlineFixWriter(new SimulatedTimeSource(), 2, "SENDER", "TARGET");
    }

    @Override
    public void finalizeBusinessMessage() {
        nextOutboundSeqNo++;
    }

    @Override
    public void finalizeAdminMessage() {
        nextOutboundSeqNo++;
    }

    @Override
    public void writeAdminMessage() {
        nextOutboundSeqNo++;
    }

    @Override
    public int getNextOutboundSeqNo() {
        return nextOutboundSeqNo;
    }

    public int getLastBeginSeqNo() {
        return lastBeginSeqNo;
    }

    public int getLastEndSeqNo() {
        return lastEndSeqNo;
    }
}
