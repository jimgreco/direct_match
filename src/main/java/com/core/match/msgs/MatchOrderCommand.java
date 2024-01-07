package com.core.match.msgs;

public interface MatchOrderCommand extends com.core.match.msgs.MatchCommonCommand {
    void copy(MatchOrderEvent cmd);
    MatchOrderEvent toEvent();

    void setOrderID(int val);

    void setBuy(boolean val);

    void setSecurityID(short val);

    void setQty(int val);

    void setPrice(long val);
    void setPrice(double val);

    void setClOrdID(java.nio.ByteBuffer val);
    void setClOrdID(String val);

    void setTraderID(short val);

    void setIOC(boolean val);

    void setExternalOrderID(int val);

    void setInBook(boolean val);
}
