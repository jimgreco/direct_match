package com.core.match.msgs;

public interface MatchOrderEvent extends com.core.match.msgs.MatchCommonEvent {
    MatchOrderCommand toCommand();

    char getMsgType();
    boolean hasMsgType();

    short getContributorID();
    boolean hasContributorID();

    int getContributorSeq();
    boolean hasContributorSeq();

    long getTimestamp();
    java.time.LocalDateTime getTimestampAsTime();
    boolean hasTimestamp();

    int getOrderID();
    boolean hasOrderID();

    boolean getBuy();
    boolean hasBuy();

    short getSecurityID();
    boolean hasSecurityID();

    int getQty();
    double getQtyAsQty();
    boolean hasQty();

    long getPrice();
    String getPriceAs32nd();
    double getPriceAsDouble();
    boolean hasPrice();

    java.nio.ByteBuffer getClOrdID();
    int getClOrdIDLength();
    String getClOrdIDAsString();
    boolean hasClOrdID();

    short getTraderID();
    boolean hasTraderID();

    boolean getIOC();
    boolean hasIOC();

    int getExternalOrderID();
    boolean hasExternalOrderID();

    boolean getInBook();
    boolean hasInBook();
} 
