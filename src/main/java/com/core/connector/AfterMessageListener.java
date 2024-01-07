package com.core.connector;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 6/18/15.
 */
public interface AfterMessageListener {
    void onAfterMessage(ByteBuffer message);
}
