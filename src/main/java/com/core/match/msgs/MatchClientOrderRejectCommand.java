package com.core.match.msgs;

public interface MatchClientOrderRejectCommand extends com.core.match.msgs.MatchCommonCommand {
    void copy(MatchClientOrderRejectEvent cmd);
    MatchClientOrderRejectEvent toEvent();

    void setTrader(java.nio.ByteBuffer val);
    void setTrader(String val);

    void setBuy(boolean val);

    void setClOrdID(java.nio.ByteBuffer val);
    void setClOrdID(String val);

    void setText(java.nio.ByteBuffer val);
    void setText(String val);

    void setReason(char val);

    void setSecurity(java.nio.ByteBuffer val);
    void setSecurity(String val);
}
