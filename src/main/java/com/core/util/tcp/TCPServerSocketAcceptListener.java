package com.core.util.tcp;

/**
 * User: jgreco
 */
public interface TCPServerSocketAcceptListener {
    TCPClientSocketListener onAccept(TCPClientSocket socket);
}
