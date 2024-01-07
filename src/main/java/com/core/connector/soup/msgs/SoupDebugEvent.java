package com.core.connector.soup.msgs;

public interface SoupDebugEvent extends SoupCommonEvent {
    SoupDebugCommand toCommand();

    java.nio.ByteBuffer getText();
    int getTextLength();
    String getTextAsString();
    String getTextAsHexString();
    boolean hasText();
} 
