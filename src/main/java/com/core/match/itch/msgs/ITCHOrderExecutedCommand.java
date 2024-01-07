package com.core.match.itch.msgs;

public interface ITCHOrderExecutedCommand extends ITCHCommonCommand {
    void copy(ITCHOrderExecutedEvent cmd);
    ITCHOrderExecutedEvent toEvent();

    void setOrderID(int val);

    void setQty(int val);

    void setPrice(long val);
    void setPrice(double val);

    void setMatchID(int val);
}
