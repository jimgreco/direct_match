package com.core.match.itch.msgs;

public interface ITCHTradeCommand extends ITCHCommonCommand {
    void copy(ITCHTradeEvent cmd);
    ITCHTradeEvent toEvent();

    void setOrderID(int val);

    void setQty(int val);

    void setPrice(long val);
    void setPrice(double val);

    void setMatchID(int val);
}
