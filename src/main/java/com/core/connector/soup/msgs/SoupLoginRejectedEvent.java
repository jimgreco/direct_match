package com.core.connector.soup.msgs;

public interface SoupLoginRejectedEvent extends SoupCommonEvent {
    SoupLoginRejectedCommand toCommand();

    char getRejectReasonCode();
    boolean hasRejectReasonCode();
} 
