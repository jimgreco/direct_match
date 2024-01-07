package com.core.fix.connector;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public interface FIXConnectorListener {
    void onFIXMessageSent(ByteBuffer msg);
    void onFIXMessageRecv(ByteBuffer msg);
}
