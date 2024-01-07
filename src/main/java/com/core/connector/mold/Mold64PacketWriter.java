package com.core.connector.mold;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 7/12/15.
 */
public interface Mold64PacketWriter {
    void init(String session);
    void init(String session, long streamSeq, int messages);
    void addMessage(ByteBuffer msgBuffer);
    boolean hasAddedMessages();
    void clear();
    ByteBuffer getDatagram();

    ByteBuffer getMessageBuffer();
    void addMessage();
    int remaining();
}
