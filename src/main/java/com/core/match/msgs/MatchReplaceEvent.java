package com.core.match.msgs;

public interface MatchReplaceEvent extends com.core.match.msgs.MatchCommonEvent {
    MatchReplaceCommand toCommand();

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

    java.nio.ByteBuffer getOrigClOrdID();
    int getOrigClOrdIDLength();
    String getOrigClOrdIDAsString();
    boolean hasOrigClOrdID();

    int getExternalOrderID();
    boolean hasExternalOrderID();

    boolean getInBook();
    boolean hasInBook();
} 
