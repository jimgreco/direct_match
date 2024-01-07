package com.core.connector.soup.msgs;

public interface SoupSequencedDataEvent extends SoupCommonEvent {
    SoupSequencedDataCommand toCommand();

    java.nio.ByteBuffer getMessage();
    int getMessageLength();
    String getMessageAsString();
    String getMessageAsHexString();
    boolean hasMessage();
} 
