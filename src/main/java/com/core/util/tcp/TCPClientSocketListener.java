package com.core.util.tcp;

/**
 * User: jgreco
 */
public interface TCPClientSocketListener {
    void onConnect(TCPClientSocket clientSocket);
    void onDisconnect(TCPClientSocket clientSocket);

    void onReadAvailable(TCPClientSocket clientSocket);
    void onWriteAvailable(TCPClientSocket clientSocket);
    void onWriteUnavailable(TCPClientSocket clientSocket);
}
