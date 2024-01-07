package com.core.match.itch.msgs;

public interface ITCHTradeEvent extends ITCHCommonEvent {
    ITCHTradeCommand toCommand();

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
