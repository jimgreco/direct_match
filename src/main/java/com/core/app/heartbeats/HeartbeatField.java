package com.core.app.heartbeats;

import com.core.util.ByteStringBuffer;

/**
 * Created by jgreco on 6/1/15.
 */
public interface HeartbeatField {
    String getCategory();
    String getName();
    char getType();
    String getValue();
    void writeData(ByteStringBuffer buffer);
}
