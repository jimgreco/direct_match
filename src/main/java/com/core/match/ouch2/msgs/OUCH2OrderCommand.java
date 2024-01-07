package com.core.match.ouch2.msgs;

public interface OUCH2OrderCommand extends OUCH2CommonCommand {
    void copy(OUCH2OrderEvent cmd);
    OUCH2OrderEvent toEvent();

    void setSide(char val);

    void setQty(int val);

    void setSecurity(java.nio.ByteBuffer val);
    void setSecurity(String val);

    void setPrice(long val);
    void setPrice(double val);

    void setTimeInForce(char val);

    void setReserved(int val);

    void setTrader(java.nio.ByteBuffer val);
    void setTrader(String val);
}
