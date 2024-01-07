package com.core.match.msgs;

public interface MatchMiscRejectEvent extends com.core.match.msgs.MatchCommonEvent {
    MatchMiscRejectCommand toCommand();

    char getMsgType();
    boolean hasMsgType();

    short getContributorID();
    boolean hasContributorID();

    int getContributorSeq();
    boolean hasContributorSeq();

    long getTimestamp();
    java.time.LocalDateTime getTimestampAsTime();
    boolean hasTimestamp();

    char getRejectedMsgType();
    boolean hasRejectedMsgType();

    char getRejectReason();
    boolean hasRejectReason();
} 
