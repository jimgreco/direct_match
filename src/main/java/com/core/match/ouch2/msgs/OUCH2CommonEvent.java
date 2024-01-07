package com.core.match.ouch2.msgs;

public interface OUCH2CommonEvent extends com.core.connector.CommonEvent {

    char getMsgType();
    boolean hasMsgType();

    long getTimestamp();
    java.time.LocalDateTime getTimestampAsTime();
    boolean hasTimestamp();

    long getClOrdID();
    boolean hasClOrdID();
} 
