package com.core.util.tcp;

import java.io.IOException;

/**
 * User: jgreco
 */
public interface TCPSocketFactory {
    TCPServerSocket createTCPServerSocket(int port, TCPServerSocketAcceptListener listener) throws IOException;
    TCPClientSocket createTCPClientSocket() throws IOException;
}
