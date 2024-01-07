package com.core.match.msgs;

public interface MatchFillEvent extends com.core.match.msgs.MatchCommonEvent {
    MatchFillCommand toCommand();

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

    int getMatchID();
    boolean hasMatchID();

    boolean getLastFill();
    boolean hasLastFill();

    boolean getPassive();
    boolean hasPassive();

    boolean getInBook();
    boolean hasInBook();
} 
