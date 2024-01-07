package com.core.connector.soup.msgs;

public interface SoupEndOfSessionEvent extends SoupCommonEvent {
    SoupEndOfSessionCommand toCommand();
} 
