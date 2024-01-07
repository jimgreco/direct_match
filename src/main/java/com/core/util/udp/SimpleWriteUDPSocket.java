package com.core.util.udp;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by hli on 4/13/16.
 */
public interface SimpleWriteUDPSocket {
    void open() throws IOException;

    void close();

    boolean canWrite();

    boolean write(ByteBuffer output);
}
