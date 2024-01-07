package com.core.connector.soup.msgs;

public interface SoupCommonEvent extends com.core.connector.CommonEvent {

    short getMsgLength();
    boolean hasMsgLength();

    char getMsgType();
    boolean hasMsgType();
} 
