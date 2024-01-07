package com.core.util.tcp;

import com.core.util.log.Log;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;


/**
 * Created by jgreco on 6/22/15.
 */
public class TCPServerSocketSingleClient implements
        TCPServerSocket,
        TCPServerSocketAcceptListener,
        TCPClientSocket,
        TCPClientSocketListener {
    private final Log log;

    private final TCPClientSocketListener listener;
    private final TCPSocketFactory factory;
    private final int port;

    private TCPServerSocket server;
    private TCPClientSocket client;

    private boolean enableRead;
    private boolean ignoreReconnect;

    public TCPServerSocketSingleClient(Log log,
                                       TCPSocketFactory factory,
                                       int port,
                                       TCPClientSocketListener listener)  {
        this.log = log;
        this.listener = listener;
        this.port = port;
        this.factory = factory;
    }

    @Override
    public void enableAccept(boolean accept) {
        throw new RuntimeException("Don't call enableAccept() with TCPServerSocketSingleClient");
    }

    @Override
    public void closeClients()  {
        log.info(log.log().add("Closing CLIENTS"));
        server.closeClients();
    }

    @Override
    public void closeClient(TCPClientSocket clientSocket) {

    }

    @Override
    public void closeWhenFinishedWriting() {
        throw new RuntimeException("Don't call closeWhenFinishedWriting() with TCPServerSocketSingleClient");
    }

    public void open() throws IOException {
        log.info(log.log().add("Opening TCPServerSocketSingleClient"));
        server = factory.createTCPServerSocket(port, this);
        server.enableAccept(true);
    }

    @Override
    public void close() {
        log.info(log.log().add("Closing"));
        ignoreReconnect = true;
        server.close();
        server = null;
        ignoreReconnect = false;
    }

    @Override
    public void enableWrite(boolean val) {
        if (client != null) {
            client.enableWrite(val);
        }
    }

    @Override
    public void enableRead(boolean enabled) {
        if (client != null) {
            client.enableRead(enabled);
        }
        enableRead = enabled;
    }

    @Override
    public TCPClientSocketListener onAccept(TCPClientSocket socket) {
        log.info(log.log().add(" Accepted client connection: ").add(socket.toString()));
        server.enableAccept(false);

        client = socket;
        client.enableRead(enableRead);
        listener.onConnect(socket);
        return this;
    }

    @Override
    public void onConnect(TCPClientSocket clientSocket) {
        throw new RuntimeException("onConnect with with TCPServerSocketSingleClient. WTF?");
    }

    @Override
    public void onDisconnect(TCPClientSocket clientSocket) {
        log.info(log.log().add("Client disconnected: ").add(clientSocket.toString()));

        client = null;
        listener.onDisconnect(clientSocket);

        if (!ignoreReconnect) {
            server.enableAccept(true);
        }
    }

    @Override
    public void onReadAvailable(TCPClientSocket clientSocket) {
        log.debug(log.log().add("Read Available"));
        listener.onReadAvailable(clientSocket);
    }

    @Override
    public void onWriteAvailable(TCPClientSocket clientSocket) {
        log.debug(log.log().add("Write Available"));
        listener.onWriteAvailable(clientSocket);
    }

    @Override
    public void onWriteUnavailable(TCPClientSocket clientSocket) {
        log.debug(log.log().add("Write Unavailable"));
        listener.onWriteUnavailable(clientSocket);
    }

    @Override
    public void setListener(TCPClientSocketListener listener) {
        throw new RuntimeException("Don't call setListener() with TCPServerSocketSingleClient");
    }

    @Override
    public void connect(String host, short port) throws IOException {
        throw new RuntimeException("Don't call connect() connect with TCPServerSocketSingleClient");
    }

    @Override
    public boolean read(ByteBuffer buffer) {
        return client != null && client.read(buffer);
    }
    
    @Override
    public int readBytes(ByteBuffer buffer) throws IOException {
    	return client.readBytes(buffer);
    }

    @Override
    public boolean write(ByteBuffer buffer) {
        return client != null && client.write(buffer);
    }

    @Override
    public boolean canRead() {
        return client != null && client.canRead();
    }

    @Override
    public boolean canWrite() {
        return client != null && client.canWrite();
    }

    @Override
    public boolean isConnected() {
        return client != null && client.isConnected();
    }

	@Override
	public SocketAddress getRemoteAddress() throws IOException {
		return client.getRemoteAddress();
	}

	@Override
	public SocketAddress getLocalAddress() throws IOException {
		return client.getLocalAddress();
	}
}
