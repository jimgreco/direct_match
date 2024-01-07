package com.core.app;

import com.core.app.heartbeats.HeartbeatSource;
import com.core.util.ByteStringBuffer;

/**
 * User: jgreco
 */
public interface Application extends HeartbeatSource {
    void setActive();
    void setPassive();
    ByteStringBuffer status();
    void setDebug(boolean debug);
}
