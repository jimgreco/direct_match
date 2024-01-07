package com.core.app.heartbeats;

/**
 * Created by jgreco on 5/31/15.
 */
public interface HeartbeatFieldUpdater {
    void setField(String fieldName, String value);
    void setField(String fieldName, long value);
    void setField(String fieldName, boolean value);
    void setField(String fieldName, double value);
}
