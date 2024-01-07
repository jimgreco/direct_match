package com.core.util.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public interface ReadWriteUDPSocket extends WritableUDPSocket {
    void enableRead(boolean val);
    void join(String intf, String host) throws IOException;
    void join(String intf, String host, short port) throws IOException;

}
