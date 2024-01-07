package com.core.match.msgs;

public interface MatchCommonEvent extends com.core.connector.CoreCommonEvent {
    MatchCommonCommand toCommand();

    char getMsgType();
    boolean hasMsgType();

    short getContributorID();
    boolean hasContributorID();

    int getContributorSeq();
    boolean hasContributorSeq();

    long getTimestamp();
    java.time.LocalDateTime getTimestampAsTime();
    boolean hasTimestamp();
} 
