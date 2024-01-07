package com.core.util.tcp;

import com.gs.collections.impl.list.mutable.FastList;

import java.io.IOException;
import java.util.List;

/**
 * Created by jgreco on 6/15/15.
 */
public class StubTCPServerSocket implements
        TCPServerSocket {
    private final StubTCPSocketFactory factory;
    private final TCPServerSocketAcceptListener listener;
    private final List<StubTCPClientSocket> clients = new FastList<>();

    private boolean accepting;

    public StubTCPServerSocket(
            StubTCPSocketFactory factory,
            TCPServerSocketAcceptListener server) {
        this.factory = factory;
        this.listener = server;
    }

    @Override
    public void close() {
        closeClients();
    }

    @Override
    public void enableAccept(boolean accept) {
        accepting = accept;
    }

    @Override
    public void closeClients() {
        clients.forEach(com.core.util.tcp.StubTCPClientSocket::close);
        clients.clear();
    }

    @Override
    public void closeClient(TCPClientSocket clientSocket) {
        clients.remove(clientSocket);
    }

    public StubTCPClientSocket connectTo() {
        if (!accepting) {
            return null;
        }

        try {
            StubTCPClientSocket socket = (StubTCPClientSocket)factory.createTCPClientSocket();
            socket.setListener(listener.onAccept(socket));
            socket.open();

            clients.add(socket);
            return socket;
        } catch (IOException e) {
            return null;
        }
    }
}
