package com.core.util.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Created by hli on 4/13/16.
 */
public interface WritableUDPSocket {
    void open() throws IOException;
    void close();
    boolean canWrite();
    boolean write(ByteBuffer output, InetSocketAddress address);

    void bind(String intf, String host, short port) throws IOException;
}
