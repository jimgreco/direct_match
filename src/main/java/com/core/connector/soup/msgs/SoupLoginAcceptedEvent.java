package com.core.connector.soup.msgs;

public interface SoupLoginAcceptedEvent extends SoupCommonEvent {
    SoupLoginAcceptedCommand toCommand();

    java.nio.ByteBuffer getSession();
    int getSessionLength();
    String getSessionAsString();
    boolean hasSession();

    java.nio.ByteBuffer getSequenceNumber();
    int getSequenceNumberLength();
    String getSequenceNumberAsString();
    boolean hasSequenceNumber();
} 
