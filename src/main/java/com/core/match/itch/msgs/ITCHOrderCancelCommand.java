package com.core.match.itch.msgs;

public interface ITCHOrderCancelCommand extends ITCHCommonCommand {
    void copy(ITCHOrderCancelEvent cmd);
    ITCHOrderCancelEvent toEvent();

    void setOrderID(int val);

    void setQtyCanceled(int val);
}
