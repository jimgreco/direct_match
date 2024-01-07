package com.core.connector.mold.rewindable;

import java.io.IOException;

import com.core.util.log.Log;
import com.core.util.store.IndexedStore;
import com.core.util.tcp.TCPClientSocket;
import com.core.util.tcp.TCPClientSocketListener;
import com.core.util.tcp.TCPServerSocket;
import com.core.util.tcp.TCPServerSocketAcceptListener;
import com.core.util.tcp.TCPSocketFactory;

public class TcpRewinderEventHandler implements ActiveStateListener, TCPServerSocketAcceptListener {

	private final Log logger;
	private final IndexedStore store;
	private final TCPServerSocket rewindServerSocket;
    private final RewinderSocketPool rewinderSocketPool;
    private static final int MAX_NUM_TCP_CONNECTIONS = 20;


	public TcpRewinderEventHandler(Log logger, IndexedStore store, TCPSocketFactory tcpSocketFactory, int listenPort) throws IOException {
		this.logger = logger;
		this.store = store;
		
		logger.info(logger.log().add("Instantiating rewind server socket on port ").add(listenPort));
		rewindServerSocket = tcpSocketFactory.createTCPServerSocket(listenPort, this);
        this.rewinderSocketPool=  new RewinderSocketPool(logger, store, MAX_NUM_TCP_CONNECTIONS,rewindServerSocket);
		
	}
	
	@Override
	public void onActive() {
		logger.info(logger.log().add("Rewind socket accepting logins"));
		rewindServerSocket.enableAccept(true);
	}

	@Override
	public void onPassive() {
		logger.info(logger.log().add("Rewinder denying logins"));
		rewindServerSocket.enableAccept(false);
	}

	@Override
	public TCPClientSocketListener onAccept(TCPClientSocket socket) {
		return rewinderSocketPool.create(socket);
	}




}
