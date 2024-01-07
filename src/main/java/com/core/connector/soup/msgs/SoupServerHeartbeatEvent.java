package com.core.connector.soup.msgs;

public interface SoupServerHeartbeatEvent extends SoupCommonEvent {
    SoupServerHeartbeatCommand toCommand();
} 
