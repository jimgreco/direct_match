package com.core.match.itch.msgs;

public interface ITCHCommonEvent extends com.core.connector.CommonEvent {

    char getMsgType();
    boolean hasMsgType();

    short getSecurityID();
    boolean hasSecurityID();

    long getTimestamp();
    java.time.LocalDateTime getTimestampAsTime();
    boolean hasTimestamp();
} 
