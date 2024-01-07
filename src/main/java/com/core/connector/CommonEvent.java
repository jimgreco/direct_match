package com.core.connector;

/**
 * Created by jgreco on 7/17/15.
 */
public interface CommonEvent {
    java.nio.ByteBuffer getRawBuffer();
    String getMsgName();
    char getMsgType();
}
