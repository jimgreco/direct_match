package com.core.match.msgs;

public interface MatchTraderEvent extends com.core.match.msgs.MatchCommonEvent {
    MatchTraderCommand toCommand();

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

    short getAccountID();
    boolean hasAccountID();

    java.nio.ByteBuffer getName();
    int getNameLength();
    String getNameAsString();
    boolean hasName();

    int getFatFinger2YLimit();
    boolean hasFatFinger2YLimit();

    int getFatFinger3YLimit();
    boolean hasFatFinger3YLimit();

    int getFatFinger5YLimit();
    boolean hasFatFinger5YLimit();

    int getFatFinger7YLimit();
    boolean hasFatFinger7YLimit();

    int getFatFinger10YLimit();
    boolean hasFatFinger10YLimit();

    int getFatFinger30YLimit();
    boolean hasFatFinger30YLimit();
} 
