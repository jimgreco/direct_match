package com.core.connector.soup.msgs;

public interface SoupLogoutRequestEvent extends SoupCommonEvent {
    SoupLogoutRequestCommand toCommand();
} 
