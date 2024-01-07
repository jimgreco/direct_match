package com.core.match.ouch.msgs;

public interface OUCHOrderCommand extends OUCHCommonCommand {
    void copy(OUCHOrderEvent cmd);
    OUCHOrderEvent toEvent();

    void setSide(char val);

    void setQty(int val);

    void setSecurity(java.nio.ByteBuffer val);
    void setSecurity(String val);

    void setPrice(long val);
    void setPrice(double val);

    void setTimeInForce(char val);

    void setMaxDisplayedQty(int val);

    void setTrader(java.nio.ByteBuffer val);
    void setTrader(String val);
}
