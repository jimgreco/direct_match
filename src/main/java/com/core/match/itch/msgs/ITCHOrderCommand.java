package com.core.match.itch.msgs;

public interface ITCHOrderCommand extends ITCHCommonCommand {
    void copy(ITCHOrderEvent cmd);
    ITCHOrderEvent toEvent();

    void setOrderID(int val);

    void setSide(char val);

    void setQty(int val);

    void setPrice(long val);
    void setPrice(double val);
}
