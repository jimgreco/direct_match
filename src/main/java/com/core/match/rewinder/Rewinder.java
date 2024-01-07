package com.core.match.rewinder;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.core.app.AppConstructor;
import com.core.app.Application;
import com.core.app.Param;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.connector.BeforeMessageListener;
import com.core.connector.Connector;
import com.core.connector.mold.rewindable.ActiveStateListener;
import com.core.connector.mold.rewindable.RewinderSocketPool;
import com.core.connector.mold.rewindable.TcpRewinderEventHandler;
import com.core.match.MatchApplication;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.store.FileIndexStore1Index;
import com.core.util.store.IndexedStore;
import com.core.util.tcp.TCPSocketFactory;

public class Rewinder extends MatchApplication implements Application, BeforeMessageListener {

	private final Log logger;
	private final ActiveStateListener rewinder;
	private IndexedStore store;

	@AppConstructor
	public Rewinder(Log logger, TCPSocketFactory tcpSocketFactory, Connector coreConnector, FileFactory fileFactory,
			@Param(name = "RewinderPort") int listenPort,
			@Param(name = "Name") String name) throws IOException {
		
		super(logger);
		
		this.logger = logger;
		this.store = new FileIndexStore1Index(logger, fileFactory, "RewinderStore-"+name);
		
		coreConnector.addBeforeListener(this);

		this.rewinder = new TcpRewinderEventHandler(logger, store, tcpSocketFactory, listenPort );
		
		
	}
	
	@Override
	public void onBeforeMessage(ByteBuffer message) {
		store.add(message);
	}
	
	@Override
	protected void onActive() {
		super.onActive();
		rewinder.onActive();
	}
	
	@Override
	protected void onPassive() {
		logger.info(logger.log().add("Rewinder denying logins"));
		rewinder.onPassive();
	}

	@Override
	public void onAddHeartbeatFields(HeartbeatFieldRegister fieldRegister) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpdateHeartbeatFields(HeartbeatFieldUpdater fieldUpdater) {
		// TODO Auto-generated method stub

	}

}
