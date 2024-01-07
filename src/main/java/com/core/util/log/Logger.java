package com.core.util.log;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public interface Logger {
    Logger add(boolean b);
    Logger add(char b);
    Logger add(byte b);
    Logger add(String str);
    Logger add(ByteBuffer buf);
    Logger add(byte[] bytes);
    Logger add(long i);
    Logger add(Throwable e);
    Logger addAsHex(ByteBuffer buf);
}
