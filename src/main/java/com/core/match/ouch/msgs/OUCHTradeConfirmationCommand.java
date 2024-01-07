package com.core.match.ouch.msgs;

public interface OUCHTradeConfirmationCommand extends OUCHCommonCommand {
    void copy(OUCHTradeConfirmationEvent cmd);
    OUCHTradeConfirmationEvent toEvent();

    void setExecQty(int val);

    void setExecPrice(long val);
    void setExecPrice(double val);

    void setMatchID(int val);

    void setSide(char val);

    void setSecurity(java.nio.ByteBuffer val);
    void setSecurity(String val);

    void setTradeDate(int val);
    void setTradeDateAsDate(java.time.LocalDate val);

    void setTradeTime(long val);

    void setSettlementDate(int val);
    void setSettlementDateAsDate(java.time.LocalDate val);

    void setCommissionAmount(long val);
    void setCommissionAmount(double val);

    void setTrader(java.nio.ByteBuffer val);
    void setTrader(String val);
}
