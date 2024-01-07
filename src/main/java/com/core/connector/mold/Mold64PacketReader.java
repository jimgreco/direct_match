package com.core.connector.mold;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 7/12/15.
 */
public interface Mold64PacketReader {
    boolean wrap(ByteBuffer datagram);
    ByteBuffer getSession();
    long getStreamSeq();
    int getMsgCount();
    boolean hasMessages();
    ByteBuffer getMessage();
}
