package com.core.sequencer;

import com.core.app.*;
import com.core.app.heartbeats.*;
import com.core.connector.ByteBufferDispatcher;
import com.core.connector.mold.Mold64UDPConnection;
import com.core.connector.mold.Mold64UDPEventSender;
import com.core.connector.mold.Mold64UDPEventSenderImpl;
import com.core.connector.mold.Mold64UDPListener;
import com.core.connector.mold.rewindable.*;
import com.core.util.ByteStringBuffer;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.store.FileIndexStore1Index;
import com.core.util.store.IndexedStore;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerService;
import com.core.util.udp.UDPSocketFactory;
import com.gs.collections.impl.set.mutable.UnifiedSet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Set;

/**
 * User: jgreco
 */
public abstract class CoreSequencer implements
		Application,
		BackupSessionListener {
	private final ByteStringBuffer status = new ByteStringBuffer();
	private final String name;
	private final Log log;

	protected final Mold64UDPEventSender sender;
	private final Mold64UDPConnection commandReceiver;

	private final RewindableMold64UDPConnector eventReceiver;
	private final ActiveStateListener rewinder;
	private final Set<RewindLocation> rewindLocations = new UnifiedSet<>();

	private final IndexedStore store;
	private String session;

	private HeartbeatNumberField nEventReceiverSeqNum;
	private HeartbeatNumberField nStoreMonitor;

	protected final ByteBufferDispatcher eventDispatcher;
	protected final ByteBufferDispatcher commandDispatcher;

	@AppConstructor
	public CoreSequencer(
			UDPSocketFactory socketFactory, FileFactory fileFactory,
			TimerService timerService,
			Log log,
			TimeSource timeSource,
			TCPSocketFactory tcpSocketFactory,
			@Param(name = "Name") String name,
			@Param(name = "DownstreamMulticastGroup") String downstreamMulticastGroup, // Towards other components
			@Param(name = "UpstreamMulticastGroup") String upstreamMulticastGroup, // Commands FROM other components
			@Param(name = "Intf") String intf,
			@Param(name = "CommandPort") short commandPort,
			@Param(name = "EventPort") short eventPort,
			@Param(name = "RewinderPort") int rewindPort,
			@Param(name = "RXRewindHost") String rxRewindHost,
			@Param(name = "RXRewindPort") int rxRewindPort)throws IOException {
		this.log = log;
		this.name = name;

		rewindLocations.add(new RewindLocation(rxRewindHost, (short)rxRewindPort));

		String filename = "SEQFileStore"+Long.toString(System.currentTimeMillis() / 1000);
		this.store = new FileIndexStore1Index(log, fileFactory, filename);
		log.info(log.log().add("Current Session saved in : ").add(filename));

		this.rewinder = new TcpRewinderEventHandler(log, store, tcpSocketFactory, rewindPort);

		commandDispatcher = getDispatcher();
		eventDispatcher = getDispatcher();

        this.commandReceiver = new Mold64UDPConnection(
				socketFactory,
				log,
				intf,
				upstreamMulticastGroup,
				commandPort);
		this.commandReceiver.setListener(new Mold64UDPListener() {
			@Override
			public boolean onMold64Packet(ByteBuffer session, long streamSeq) {
				return true;
			}

			@Override
			public void onMold64Message(long seqNum, ByteBuffer message) {
				commandDispatcher.dispatch(message);
			}

			@Override
			public void onMold64PacketComplete(ByteBuffer session, long nextSeqNum) {
			}
		});

		this.eventReceiver = new RewindableMold64UDPConnector(log, eventDispatcher, socketFactory, tcpSocketFactory, new MemoryBackedMessageStore(log), downstreamMulticastGroup, intf, rewindLocations, eventPort);
		this.eventReceiver.onHeartbeatRegister(new NullHeartbeatRegister());

		BackupQueue backupEventQueue = getBackupEventQueue();
		this.sender = new Mold64UDPEventSenderImpl(log, timeSource, timerService, 1, socketFactory, intf, downstreamMulticastGroup, eventPort, store, backupEventQueue);
	}

	protected abstract BackupQueue getBackupEventQueue();
	protected abstract ByteBufferDispatcher getDispatcher();
	protected abstract void onLocalAddContributor(String contributorName);

	@Override
	public void setActive() {	}

	@Override
	public void setPassive() {	}

	@Override
	public void onHeartbeatRegister(HeartbeatFieldRegister register) {
		nStoreMonitor=register.addNumberField("",HeartBeatFieldIDEnum.MsgStoreSize);
		nEventReceiverSeqNum=register.addNumberField("",HeartBeatFieldIDEnum.EventReceiverNum);
	}

	@Override
	public void onHeartbeatUpdate(HeartbeatFieldUpdater register) {
		nStoreMonitor.set(store.size());
		nEventReceiverSeqNum.set(eventReceiver.getCurrentSeq());
	}

	@Exposed(name = "startSession")
	public void startSession() {
		session = Long.toString(System.currentTimeMillis() / 1000);
		log.info(log.log().add("New Session Started : ").add(session));
	}

	@Exposed(name = "status")
	@Override
	public ByteStringBuffer status() {
		// TODO: see if we're primary
		status.clear();
		status.add("Name: ").add(name).addNL();
		status.add("Primary: ").add(isPrimary()).addNL();
		status.add("StoreSize: ").add(store.size()).addNL();
		status.add("BackupEventRecSeqNum: ").add(eventReceiver.getCurrentSeq()).addNL();
		status.add("SenderNextSeqNum: ").add(sender.getNextSeqNumToSend()).addNL();
		status.add("Session: ").add(session).addNL();
		return status;
	}

	@Exposed(name = "setDebug")
	@Override
	public void setDebug(@Param(name = "Debug") boolean debug) {
		log.setDebug(debug);
	}

	@Exposed(name = "openAsPrimary")
	public void openPrimary() {
		// make sure we aren't listening to events
		try
		{
			//Close Stream event receiver
			eventReceiver.close();
			// start listening to commands
			commandReceiver.open();
		}
		catch (IOException e)
		{
			throw new CommandException("Error opening commandReceiver: " + e.getCause());
		}
		try
		{
			// send events through this channel
			sender.open();
			sender.setSendEnabled(true);
			sender.setSession(session);

		}
		catch (IOException e)
		{
			throw new CommandException("Error opening sender: " + e.getCause());
		}

		rewinder.onActive();
		
		// send out our contributor
		addContributor(name);
	}

	@Exposed(name = "openAsBackup")
	public void openBackup()
	{
		// make sure we aren't listening to commands
		commandReceiver.close();

		try
		{
			eventReceiver.setNextExpectedSequenceNumber(store.size()+1);
			// start listening to events
			eventReceiver.open();
		}
		catch (IOException e)
		{
			throw new CommandException(
					"Error opening eventReceiver: " + e.getCause());
		}

		rewinder.onPassive();
		
		// don't send any events
		sender.close();
		sender.setSendEnabled(false);
		session = eventReceiver.getSession();
		eventReceiver.setSessionListener(this);
		sender.setSession(session); //backup session is read from the event receiver
	}

	@Exposed(name = "addContributor")
	public void addContributor(@Param(name = "name") String contributorName) {
		onLocalAddContributor(contributorName);
	}

	@Override
	public void onSessionStarted(String session) {
		this.session=session;
		this.sender.setSession(session);
		log.info(log.log().add("Session set : ").add(session));
	}

	private boolean isPrimary(){
		return sender.isSenderEnabled();
	}
}
