package com.core.app.heartbeats;

/**
 * Created by jgreco on 5/31/15.
 */
public interface HeartbeatNumberField extends HeartbeatField {
    void set(long val);
    long get();
    long inc();
}
