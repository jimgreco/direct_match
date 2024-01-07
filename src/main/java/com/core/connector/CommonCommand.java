package com.core.connector;

/**
 * Created by jgreco on 7/17/15.
 */
public interface CommonCommand {
    java.nio.ByteBuffer getRawBuffer();
    int getLength();
    void setLength(int length);
}
