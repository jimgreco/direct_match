package com.core.util.file;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public interface File {
    long size();
    int write(ByteBuffer buf, long offset) throws IOException;
    int write(ByteBuffer buf) throws IOException;
    int read(ByteBuffer buffer, long offset) throws IOException;
    int read(ByteBuffer buffer) throws IOException;
}
