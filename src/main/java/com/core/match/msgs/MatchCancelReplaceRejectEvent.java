package com.core.match.msgs;

public interface MatchCancelReplaceRejectEvent extends com.core.match.msgs.MatchCommonEvent {
    MatchCancelReplaceRejectCommand toCommand();

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

    java.nio.ByteBuffer getClOrdID();
    int getClOrdIDLength();
    String getClOrdIDAsString();
    boolean hasClOrdID();

    java.nio.ByteBuffer getOrigClOrdID();
    int getOrigClOrdIDLength();
    String getOrigClOrdIDAsString();
    boolean hasOrigClOrdID();

    java.nio.ByteBuffer getText();
    int getTextLength();
    String getTextAsString();
    boolean hasText();

    boolean getIsReplace();
    boolean hasIsReplace();

    char getReason();
    boolean hasReason();
} 
