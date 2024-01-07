package com.core.match.msgs;

public interface MatchInboundEvent extends com.core.match.msgs.MatchCommonEvent {
    MatchInboundCommand toCommand();

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

    int getBeginSeqNo();
    boolean hasBeginSeqNo();

    int getEndSeqNo();
    boolean hasEndSeqNo();

    java.nio.ByteBuffer getReqID();
    int getReqIDLength();
    String getReqIDAsString();
    boolean hasReqID();

    short getSecurityID();
    boolean hasSecurityID();
} 
