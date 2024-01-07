package com.core.match.itch.msgs;

public interface ITCHOrderCancelEvent extends ITCHCommonEvent {
    ITCHOrderCancelCommand toCommand();

    int getOrderID();
    boolean hasOrderID();

    int getQtyCanceled();
    double getQtyCanceledAsQty();
    boolean hasQtyCanceled();
} 
