package com.core.fix.util;

/**
 * Created by jgreco on 1/4/15.
 */
public interface HeartbeatSessionListener {
    void sendHeartbeat();
    void sendTestRequest();
}
