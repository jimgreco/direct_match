package com.core.connector.soup.msgs;

public interface SoupUnsequencedDataEvent extends SoupCommonEvent {
    SoupUnsequencedDataCommand toCommand();

    java.nio.ByteBuffer getMessage();
    int getMessageLength();
    String getMessageAsString();
    String getMessageAsHexString();
    boolean hasMessage();
} 
