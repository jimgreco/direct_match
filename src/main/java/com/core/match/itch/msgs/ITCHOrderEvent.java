package com.core.match.itch.msgs;

public interface ITCHOrderEvent extends ITCHCommonEvent {
    ITCHOrderCommand toCommand();

    int getOrderID();
    boolean hasOrderID();

    char getSide();
    boolean hasSide();

    int getQty();
    double getQtyAsQty();
    boolean hasQty();

    long getPrice();
    String getPriceAs32nd();
    double getPriceAsDouble();
    boolean hasPrice();
} 
