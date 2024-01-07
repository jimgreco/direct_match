package com.core.match.msgs;

public interface MatchOrderRejectCommand extends com.core.match.msgs.MatchCommonCommand {
    void copy(MatchOrderRejectEvent cmd);
    MatchOrderRejectEvent toEvent();

    void setTraderID(short val);

    void setBuy(boolean val);

    void setClOrdID(java.nio.ByteBuffer val);
    void setClOrdID(String val);

    void setText(java.nio.ByteBuffer val);
    void setText(String val);

    void setReason(char val);

    void setSecurityID(short val);
}
