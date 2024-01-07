package com.core.match.msgs;

public interface MatchCancelCommand extends com.core.match.msgs.MatchCommonCommand {
    void copy(MatchCancelEvent cmd);
    MatchCancelEvent toEvent();

    void setOrderID(int val);

    void setClOrdID(java.nio.ByteBuffer val);
    void setClOrdID(String val);

    void setOrigClOrdID(java.nio.ByteBuffer val);
    void setOrigClOrdID(String val);
}
