package com.core.match.msgs;

public interface MatchOutboundCommand extends com.core.match.msgs.MatchCommonCommand {
    void copy(MatchOutboundEvent cmd);
    MatchOutboundEvent toEvent();

    void setFixMsgType(char val);

    void setReqID(java.nio.ByteBuffer val);
    void setReqID(String val);

    void setText(java.nio.ByteBuffer val);
    void setText(String val);

    void setRefMsgType(char val);

    void setRefSeqNum(int val);

    void setRefTagID(short val);

    void setSessionRejectReason(char val);
}
