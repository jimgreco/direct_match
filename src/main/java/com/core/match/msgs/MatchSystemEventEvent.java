package com.core.match.msgs;

public interface MatchSystemEventEvent extends com.core.match.msgs.MatchCommonEvent {
    MatchSystemEventCommand toCommand();

    char getMsgType();
    boolean hasMsgType();

    short getContributorID();
    boolean hasContributorID();

    int getContributorSeq();
    boolean hasContributorSeq();

    long getTimestamp();
    java.time.LocalDateTime getTimestampAsTime();
    boolean hasTimestamp();

    char getEventType();
    boolean hasEventType();
} 
