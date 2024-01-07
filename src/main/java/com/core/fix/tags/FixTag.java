package com.core.fix.tags;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public interface FixTag {
    byte[] getTagString();
    short getID();
    void reset(ByteBuffer buffer);
    void setValuePosition(int startPosition, int endPosition);
    boolean isPresent();
    ByteBuffer getValue();
    char getValueAsChar();
    int getValueAsInt();
    long getValueAsPrice(int impliedDecimals);
    int getGroup();
    int getNumGroups();
}
