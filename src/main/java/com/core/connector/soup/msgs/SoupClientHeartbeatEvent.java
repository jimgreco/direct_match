package com.core.connector.soup.msgs;

public interface SoupClientHeartbeatEvent extends SoupCommonEvent {
    SoupClientHeartbeatCommand toCommand();
} 
