package com.core.match.itch.msgs;

public interface ITCHOrderExecutedEvent extends ITCHCommonEvent {
    ITCHOrderExecutedCommand toCommand();

    int getOrderID();
    boolean hasOrderID();

    int getQty();
    double getQtyAsQty();
    boolean hasQty();

    long getPrice();
    String getPriceAs32nd();
    double getPriceAsDouble();
    boolean hasPrice();

    int getMatchID();
    boolean hasMatchID();
} 
