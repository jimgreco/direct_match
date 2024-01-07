package com.core.match.msgs;

public interface MatchOrderRejectEvent extends com.core.match.msgs.MatchCommonEvent {
    MatchOrderRejectCommand toCommand();

    char getMsgType();
    boolean hasMsgType();

    short getContributorID();
    boolean hasContributorID();

    int getContributorSeq();
    boolean hasContributorSeq();

    long getTimestamp();
    java.time.LocalDateTime getTimestampAsTime();
    boolean hasTimestamp();

    short getTraderID();
    boolean hasTraderID();

    boolean getBuy();
    boolean hasBuy();

    java.nio.ByteBuffer getClOrdID();
    int getClOrdIDLength();
    String getClOrdIDAsString();
    boolean hasClOrdID();

    java.nio.ByteBuffer getText();
    int getTextLength();
    String getTextAsString();
    boolean hasText();

    char getReason();
    boolean hasReason();

    short getSecurityID();
    boolean hasSecurityID();
} 
