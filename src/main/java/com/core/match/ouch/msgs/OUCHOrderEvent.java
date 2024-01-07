package com.core.match.ouch.msgs;

public interface OUCHOrderEvent extends OUCHCommonEvent {
    OUCHOrderCommand toCommand();

    char getSide();
    boolean hasSide();

    int getQty();
    double getQtyAsQty();
    boolean hasQty();

    java.nio.ByteBuffer getSecurity();
    int getSecurityLength();
    String getSecurityAsString();
    boolean hasSecurity();

    long getPrice();
    String getPriceAs32nd();
    double getPriceAsDouble();
    boolean hasPrice();

    char getTimeInForce();
    boolean hasTimeInForce();

    int getMaxDisplayedQty();
    double getMaxDisplayedQtyAsQty();
    boolean hasMaxDisplayedQty();

    java.nio.ByteBuffer getTrader();
    int getTraderLength();
    String getTraderAsString();
    boolean hasTrader();
} 
