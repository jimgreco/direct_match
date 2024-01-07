package com.core.match.msgs;

public interface MatchOutboundEvent extends com.core.match.msgs.MatchCommonEvent {
    MatchOutboundCommand toCommand();

    char getMsgType();
    boolean hasMsgType();

    short getContributorID();
    boolean hasContributorID();

    int getContributorSeq();
    boolean hasContributorSeq();

    long getTimestamp();
    java.time.LocalDateTime getTimestampAsTime();
    boolean hasTimestamp();

    char getFixMsgType();
    boolean hasFixMsgType();

    java.nio.ByteBuffer getReqID();
    int getReqIDLength();
    String getReqIDAsString();
    boolean hasReqID();

    java.nio.ByteBuffer getText();
    int getTextLength();
    String getTextAsString();
    boolean hasText();

    char getRefMsgType();
    boolean hasRefMsgType();

    int getRefSeqNum();
    boolean hasRefSeqNum();

    short getRefTagID();
    boolean hasRefTagID();

    char getSessionRejectReason();
    boolean hasSessionRejectReason();
} 
