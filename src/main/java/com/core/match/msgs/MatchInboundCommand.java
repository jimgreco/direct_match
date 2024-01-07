package com.core.match.msgs;

public interface MatchInboundCommand extends com.core.match.msgs.MatchCommonCommand {
    void copy(MatchInboundEvent cmd);
    MatchInboundEvent toEvent();

    void setFixMsgType(char val);

    void setBeginSeqNo(int val);

    void setEndSeqNo(int val);

    void setReqID(java.nio.ByteBuffer val);
    void setReqID(String val);

    void setSecurityID(short val);
}
