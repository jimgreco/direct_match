package com.core.util.udp;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by jgreco on 8/10/15.
 */
public interface SimpleReadWriteUDPSocket extends SimpleWriteUDPSocket {
    void enableRead(boolean val);
    boolean hasJoined();
}
