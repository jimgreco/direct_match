package com.core.match.ouch2.msgs;

public interface OUCH2OrderEvent extends OUCH2CommonEvent {
    OUCH2OrderCommand toCommand();

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

    int getReserved();
    boolean hasReserved();

    java.nio.ByteBuffer getTrader();
    int getTraderLength();
    String getTraderAsString();
    boolean hasTrader();
} 
