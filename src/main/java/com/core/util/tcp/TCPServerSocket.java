package com.core.util.tcp;



/**
 * User: jgreco
 */
public interface TCPServerSocket {
    void enableAccept(boolean accept);
    void close();
    void closeClients();
    void closeClient(TCPClientSocket clientSocket);
}
