package com.core.fix;

import com.core.fix.tags.FixTag;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public interface FixWriter {
    void initFix(char msgType, int seqNum);
    void initFix(String msgType, int seqNum);
    ByteBuffer getFixBody();
    void buildFixMessage(ByteBuffer messageBuffer, ByteBuffer bodyBuffer, boolean resend);
    void writeNumber(FixTag tag, long number);
    void writeChar(FixTag tag, char c);
    void writeString(FixTag tag, ByteBuffer str);
    void writeString(FixTag tag, String str);
    void writePrice(FixTag tag, long price, int impliedDecimals);
    void writeDateTime(FixTag tag, long time);
}
