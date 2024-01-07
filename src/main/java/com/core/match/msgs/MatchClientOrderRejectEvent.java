package com.core.match.msgs;

public interface MatchClientOrderRejectEvent extends com.core.match.msgs.MatchCommonEvent {
    MatchClientOrderRejectCommand toCommand();

    char getMsgType();
    boolean hasMsgType();

    short getContributorID();
    boolean hasContributorID();

    int getContributorSeq();
    boolean hasContributorSeq();

    long getTimestamp();
    java.time.LocalDateTime getTimestampAsTime();
    boolean hasTimestamp();

    java.nio.ByteBuffer getTrader();
    int getTraderLength();
    String getTraderAsString();
    boolean hasTrader();

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

    java.nio.ByteBuffer getSecurity();
    int getSecurityLength();
    String getSecurityAsString();
    boolean hasSecurity();
} 
