package com.core.util.tcp;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public interface TCPClientSocket {
    void setListener(TCPClientSocketListener listener);

    void connect(String host, short port) throws IOException;
    void close();
    void closeClients();
    void closeWhenFinishedWriting();

    void enableWrite(boolean val);
    void enableRead(boolean val);

    boolean read(ByteBuffer buffer);
    boolean write(ByteBuffer output);

    int readBytes(ByteBuffer buffer) throws IOException;
    
    boolean canRead();
    boolean canWrite();

    boolean isConnected();
    
    SocketAddress getRemoteAddress() throws IOException;
    SocketAddress getLocalAddress() throws IOException;
}
