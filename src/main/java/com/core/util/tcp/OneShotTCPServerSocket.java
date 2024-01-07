package com.core.util.tcp;



import com.core.util.log.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by jgreco on 7/9/15.
 * This class is meant to do a read and then service the request completely
 * We don't care when a client connects, disconnects, or we can't write anymore data
 */
public class OneShotTCPServerSocket {
    private final TCPServerSocket server;
    final ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024 * 1024);
    private final Log log;

    public OneShotTCPServerSocket(TCPSocketFactory factory, int port, OneShotTCPServerTCPServerSocketListener listener,Log log) throws IOException {
        this.log=log;
        server = factory.createTCPServerSocket(port, new TCPServerSocketAcceptListener() {
            @Override
            public TCPClientSocketListener onAccept(TCPClientSocket socket) {
                socket.enableRead(true);

                return new TCPClientSocketListener() {
                    @Override
                    public void onConnect(TCPClientSocket clientSocket) {

                    }

                    @Override
                    public void onDisconnect(TCPClientSocket clientSocket) {
                        clientSocket.setListener(null);
                        server.closeClient(clientSocket);
                    }

                    @Override
                    public void onReadAvailable(TCPClientSocket clientSocket) {
                        readBuffer.clear();
                        clientSocket.read(readBuffer);
                        readBuffer.flip();

                        listener.onRead(clientSocket, readBuffer);
                    }

                    @Override
                    public void onWriteAvailable(TCPClientSocket clientSocket) {

                    }

                    @Override
                    public void onWriteUnavailable(TCPClientSocket clientSocket) {

                    }

                };
            }
        });
    }

    public void open() {
        server.enableAccept(true);
    }

    public void close() {
        server.closeClients();
        server.enableAccept(false);
    }
}
