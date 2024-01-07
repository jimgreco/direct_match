package com.core.util.tcp;

import com.core.util.TimeUtils;
import com.core.util.log.Log;
import com.core.util.time.TimerHandler;
import com.core.util.time.TimerService;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by jgreco on 7/8/15.
 */
public class ReconnectingTCPClientSocket implements
        TCPClientSocket,
        TimerHandler,
        TCPClientSocketListener {
    public static final int DEFAULT_TIMEOUT_SECONDS = 5;

    private final long timeout;
    private final TCPClientSocketListener listener;

    private final TimerService timers;
    private final TCPSocketFactory selector;
    private final Log log;
    private TCPClientSocket socket;

    private String host;
    private short port;
    private volatile int timer;
    private boolean enableRead;

    public ReconnectingTCPClientSocket(TimerService timers,
                                       TCPSocketFactory selector,
                                       Log log,
                                       TCPClientSocketListener listener)  {

        this(timers, selector, log, listener, DEFAULT_TIMEOUT_SECONDS);
    }


    public ReconnectingTCPClientSocket(TimerService timers,
                                       TCPSocketFactory selector,
                                       Log log,
                                       TCPClientSocketListener listener,
                                       int timeoutSeconds)  {

        this.log = log;
        this.timers = timers;
        this.selector = selector;
        this.timeout = timeoutSeconds * TimeUtils.NANOS_PER_SECOND;
        this.listener = listener;
    }

    @Override
    public void onTimer(int internalTimerID, int referenceData) {
        try {
            tryConnect();
        } catch (IOException e) {
            log.error(log.log().add("Error connecting: ").add(e));
        }
    }

    @Override
    public void setListener(TCPClientSocketListener listener) {
        throw new RuntimeException("Cannot set TCPClientSocketListener in ReconnectingTCPClientSocket");
    }

    @Override
    public void connect(String host, short port) throws IOException {
        this.host = host;
        this.port = port;

        tryConnect();
    }

    @Override
    public void close() {
        cancelReconnectTimer();

        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    @Override
    public void closeClients() {
        socket.closeClients();
    }

    @Override
    public void closeWhenFinishedWriting() {
        if (socket != null) {
            socket.closeWhenFinishedWriting();
            socket = null;
        }
    }

    @Override
    public void enableWrite(boolean val) {
        if (socket != null) {
            socket.enableWrite(val);
        }
    }

    @Override
    public void enableRead(boolean val) {
        this.enableRead = val;

        if (socket != null) {
            socket.enableRead(val);
        }
    }

    @Override
    public boolean read(ByteBuffer buffer) {
        return socket != null && socket.read(buffer);
    }
    
    @Override
    public int readBytes(ByteBuffer buffer) throws IOException {
    	return socket.readBytes(buffer);
    }

    @Override
    public boolean write(ByteBuffer output) {
        return socket != null && socket.write(output);
    }

    @Override
    public boolean canRead() {
        return socket != null && socket.canRead();
    }

    @Override
    public boolean canWrite() {
        return socket != null && socket.canWrite();
    }

    @Override
    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    @Override
    public void onConnect(TCPClientSocket clientSocket) {
        cancelReconnectTimer();
        enableRead(enableRead);
        listener.onConnect(clientSocket);
    }

    @Override
    public void onDisconnect(TCPClientSocket clientSocket) {
        listener.onDisconnect(clientSocket);
        setReconnectTimer();
    }

    @Override
    public void onReadAvailable(TCPClientSocket clientSocket) {
        listener.onReadAvailable(clientSocket);
    }

    @Override
    public void onWriteAvailable(TCPClientSocket clientSocket) {
        listener.onWriteAvailable(clientSocket);
    }

    @Override
    public void onWriteUnavailable(TCPClientSocket clientSocket) {
        listener.onWriteUnavailable(clientSocket);
    }

    private void tryConnect() throws IOException {
        close();

        // this is set before in case we connect immediately
        // only happens in test

        socket = selector.createTCPClientSocket();
        socket.setListener(this);
        socket.connect(host, port);
    }

    private void setReconnectTimer() {
    	cancelReconnectTimer();
        timer = timers.scheduleTimer(timeout, this);
    }

    private void cancelReconnectTimer() {
        timer = timers.cancelTimer(timer);
    }


	@Override
	public SocketAddress getRemoteAddress() throws IOException {
		return socket.getRemoteAddress();
	}


	@Override
	public SocketAddress getLocalAddress() throws IOException {
		return socket.getLocalAddress();
	}
}
