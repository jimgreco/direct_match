package com.core.util.tcp;

import com.gs.collections.impl.list.mutable.FastList;

import java.io.IOException;
import java.util.List;

/**
 * Created by jgreco on 6/25/15.
 */
public class StubTCPSocketFactory implements TCPSocketFactory {
    private List<StubTCPServerSocket> serverSockets = new FastList<>();
    private List<StubTCPClientSocket> clientSockets = new FastList<>();

    private StubTCPClientSocket nextClient;
    private StubTCPClientSocket nextServerClient;

    @Override
    public TCPServerSocket createTCPServerSocket(int port, TCPServerSocketAcceptListener listener) throws IOException {
        StubTCPServerSocket server = new StubTCPServerSocket(this, listener);

        if (nextClient != null) {
            nextClient.setServer(server);
        }

        serverSockets.add(server);
        return server;
    }

    @Override
    public TCPClientSocket createTCPClientSocket() throws IOException {
        StubTCPClientSocket client;

        if (nextClient != null) {
            client = nextClient;
            nextClient = null;
        }
        else if (nextServerClient != null) {
            client = nextServerClient;
        }
        else {
            client = new StubTCPClientSocket();
        }

        clientSockets.add(client);
        return client;
    }

    public void createServerClientPair() {
        nextClient = new StubTCPClientSocket();
        nextServerClient = new StubTCPClientSocket();
    }

    public StubTCPClientSocket getClient(int index) {
        return clientSockets.get(index);
    }

    public StubTCPServerSocket getServer(int index) {
        return serverSockets.get(index);
    }
}
