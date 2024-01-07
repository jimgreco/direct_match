package com.core.match.msgs;

public interface MatchContributorEvent extends com.core.match.msgs.MatchCommonEvent {
    MatchContributorCommand toCommand();

    char getMsgType();
    boolean hasMsgType();

    short getContributorID();
    boolean hasContributorID();

    int getContributorSeq();
    boolean hasContributorSeq();

    long getTimestamp();
    java.time.LocalDateTime getTimestampAsTime();
    boolean hasTimestamp();

    short getSourceContributorID();
    boolean hasSourceContributorID();

    java.nio.ByteBuffer getName();
    int getNameLength();
    String getNameAsString();
    boolean hasName();

    boolean getCancelOnDisconnect();
    boolean hasCancelOnDisconnect();
} 
