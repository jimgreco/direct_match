package com.core.match.ouch.msgs;

public interface OUCHCommonEvent extends com.core.connector.CommonEvent {

    char getMsgType();
    boolean hasMsgType();

    long getClOrdID();
    boolean hasClOrdID();
} 
