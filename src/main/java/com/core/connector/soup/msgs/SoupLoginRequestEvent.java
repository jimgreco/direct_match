package com.core.connector.soup.msgs;

public interface SoupLoginRequestEvent extends SoupCommonEvent {
    SoupLoginRequestCommand toCommand();

    java.nio.ByteBuffer getUsername();
    int getUsernameLength();
    String getUsernameAsString();
    boolean hasUsername();

    java.nio.ByteBuffer getPassword();
    int getPasswordLength();
    String getPasswordAsString();
    boolean hasPassword();

    java.nio.ByteBuffer getRequestedSession();
    int getRequestedSessionLength();
    String getRequestedSessionAsString();
    boolean hasRequestedSession();

    java.nio.ByteBuffer getRequestedSequenceNumber();
    int getRequestedSequenceNumberLength();
    String getRequestedSequenceNumberAsString();
    boolean hasRequestedSequenceNumber();
} 
