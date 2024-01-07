package com.core.match.msgs;

public interface MatchAccountEvent extends com.core.match.msgs.MatchCommonEvent {
    MatchAccountCommand toCommand();

    char getMsgType();
    boolean hasMsgType();

    short getContributorID();
    boolean hasContributorID();

    int getContributorSeq();
    boolean hasContributorSeq();

    long getTimestamp();
    java.time.LocalDateTime getTimestampAsTime();
    boolean hasTimestamp();

    short getAccountID();
    boolean hasAccountID();

    java.nio.ByteBuffer getName();
    int getNameLength();
    String getNameAsString();
    boolean hasName();

    int getNetDV01Limit();
    boolean hasNetDV01Limit();

    long getCommission();
    String getCommissionAs32nd();
    double getCommissionAsDouble();
    boolean hasCommission();

    java.nio.ByteBuffer getSSGMID();
    int getSSGMIDLength();
    String getSSGMIDAsString();
    boolean hasSSGMID();

    boolean getNettingClearing();
    boolean hasNettingClearing();
} 
