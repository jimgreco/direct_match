package com.core.match.sequencer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Set;

import com.core.app.AppConstructor;
import com.core.app.Application;
import com.core.app.CommandException;
import com.core.app.Exposed;
import com.core.app.Param;
import com.core.app.heartbeats.HeartBeatFieldIDEnum;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.app.heartbeats.HeartbeatNumberField;
import com.core.app.heartbeats.NullHeartbeatRegister;
import com.core.connector.mold.Mold64UDPConnection;
import com.core.connector.mold.Mold64UDPEventSender;
import com.core.connector.mold.Mold64UDPEventSenderImpl;
import com.core.connector.mold.Mold64UDPListener;
import com.core.connector.mold.rewindable.ActiveStateListener;
import com.core.connector.mold.rewindable.MemoryBackedMessageStore;
import com.core.connector.mold.rewindable.RewindLocation;
import com.core.connector.mold.rewindable.RewindableMold64UDPConnector;
import com.core.connector.mold.rewindable.TcpRewinderEventHandler;
import com.core.match.msgs.MatchByteBufferDispatcher;
import com.core.match.msgs.MatchByteBufferMessages;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchMessages;
import com.core.match.services.events.SystemEventService;
import com.core.sequencer.BackupSessionListener;
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

/**
 * User: jgreco
 */
public class Sequencer implements
		Application,
		SequencerMarketHoursServiceListener , BackupSessionListener {
	private static final int TCP_CONNECTIONS_NUMBER = 20;
	private final ByteStringBuffer status = new ByteStringBuffer();
	private final String name;

	private final Mold64UDPEventSender sender;
	private final Mold64UDPConnection commandReceiver;
	private final RewindableMold64UDPConnector eventReceiver;

	private final SequencerContributorService contributors;
	private final SequencerAccountService accounts;
	private final SequencerTraderService traders;
	private final SequencerSecurityService securities;
	private final SequencerLimitBookService books;
    private final SequencerMarketHoursService marketHours;

    private final StaticsCommandHandler staticsCommandHandler;
    private final OrderCommandHandler orderCommandHandler;
	private IndexedStore store;
	private final OrderCommandHandler backupEventHandler;
	private final SystemEventService systemEventService;
	private final FileFactory fileFactory;
	private  String session;

	private HeartbeatNumberField ordersMonitor;
	private HeartbeatNumberField nextOrderIDMonitor;
	private HeartbeatNumberField nextMatchIDMonitor;
	private HeartbeatNumberField nSecuritiesMonitor;
	private HeartbeatNumberField nContributorsMonitor;
	private HeartbeatNumberField nAccountsMonitor;
	private HeartbeatNumberField nTradersMonitor;
	private HeartbeatNumberField nStoreMonitor;

	private Log log;
	private HeartbeatNumberField nEventReceiverSeqNum;
	private final ActiveStateListener rewinder;
	private final Set<RewindLocation> rewindLocations = new UnifiedSet<>();

	@AppConstructor
	public Sequencer(
            UDPSocketFactory socketFactory,FileFactory fileFactory,
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
			@Param(name = "MarketOpenTime") String marketOpen,
			@Param(name = "MarketCloseTime") String marketClose,
			@Param(name = "RewinderPort") int rewindPort,
			@Param(name = "RXRewindHost") String rxRewindHost,
			@Param(name = "RXRewindPort") int rxRewindPort	)throws IOException
	{

		rewindLocations.add(new RewindLocation(rxRewindHost,(short)rxRewindPort));


		this.log=log;
		this.name = name;
		SequencerOrderService orders = new SequencerOrderService(log);
		this.books = new SequencerLimitBookService(log, orders);
		String filename= "SEQFileStore"+Long.toString(System.currentTimeMillis() / 1000);
		store = new FileIndexStore1Index(log, fileFactory, filename);
		log.info(log.log().add("Current Session saved in : ").add(filename));

        MatchMessages messages = new MatchByteBufferMessages();
        MatchByteBufferDispatcher commandDispatcher = new MatchByteBufferDispatcher(messages);
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
		this.fileFactory= fileFactory;

        MatchByteBufferDispatcher eventDispatcher = new MatchByteBufferDispatcher(messages);
		this.eventReceiver = new RewindableMold64UDPConnector(log,eventDispatcher, socketFactory,tcpSocketFactory,new MemoryBackedMessageStore(log), downstreamMulticastGroup,intf,rewindLocations, eventPort);
		this.eventReceiver.onHeartbeatRegister(new NullHeartbeatRegister());
		BackupEventQueue backupEventQueue= new BackupEventQueue(log,messages);

		this.sender = new Mold64UDPEventSenderImpl(log, timeSource, timerService, 1, socketFactory, intf, downstreamMulticastGroup, eventPort, store,backupEventQueue);

        this.contributors = new SequencerContributorService(log);
        this.accounts = new SequencerAccountService(log);
        this.traders = new SequencerTraderService(log);
        this.securities = new SequencerSecurityService(log, books);
		this.marketHours = new SequencerMarketHoursService(timeSource, timerService, this, marketOpen, marketClose);

        this.staticsCommandHandler = new StaticsCommandHandler(log, timeSource, messages, sender, contributors, securities, accounts, traders);


        this.orderCommandHandler = new OrderCommandHandler(log, timeSource, messages, sender, contributors, securities, accounts, traders, books, marketHours);
        commandDispatcher.subscribe(orderCommandHandler);

        MiscCommandHandler miscCommandHandler = new MiscCommandHandler(log, timeSource, messages, sender, contributors, securities);
        commandDispatcher.subscribe(miscCommandHandler);


		this.rewinder = new TcpRewinderEventHandler(log, store, tcpSocketFactory, rewindPort);

		//Components  backup- sharing the same book
		systemEventService = SystemEventService.create();
		systemEventService.addListener(marketHours);


		backupEventHandler= new OrderCommandHandler(log, timeSource, messages, sender, contributors, securities, accounts, traders, books, marketHours);
        SequencerEventHandler eventHandler = new SequencerEventHandler(log, store, books, contributors,backupEventHandler,backupEventQueue);
        eventDispatcher.subscribe(eventHandler);
		eventDispatcher.subscribe(systemEventService);
		eventDispatcher.subscribe(contributors);
		eventDispatcher.subscribe(securities);
		eventDispatcher.subscribe(accounts);
		eventDispatcher.subscribe(traders);
		openBackup();
	}

	@Override
	public void setActive() {	}

	@Override
	public void setPassive() {	}

	@Override
	public void onHeartbeatRegister(HeartbeatFieldRegister register) {
		ordersMonitor = register.addNumberField("", HeartBeatFieldIDEnum.LiveOrders);
		register.addNumberField("", HeartBeatFieldIDEnum.MaxLiveOrders).set(MatchConstants.MAX_LIVE_ORDERS);
		nextOrderIDMonitor = register.addNumberField("", HeartBeatFieldIDEnum.NextOrderId);
		nextMatchIDMonitor = register.addNumberField("", HeartBeatFieldIDEnum.NextMatchId);
		nSecuritiesMonitor = register.addNumberField("", HeartBeatFieldIDEnum.NumSecurities);
		nContributorsMonitor = register.addNumberField("", HeartBeatFieldIDEnum.NumContributors);
		nAccountsMonitor = register.addNumberField("", HeartBeatFieldIDEnum.Acct);
		nTradersMonitor = register.addNumberField("", HeartBeatFieldIDEnum.TRADER);
		nStoreMonitor=register.addNumberField("",HeartBeatFieldIDEnum.MsgStoreSize);
		nEventReceiverSeqNum=register.addNumberField("",HeartBeatFieldIDEnum.EventReceiverNum);
	}

	@Override
	public void onHeartbeatUpdate(HeartbeatFieldUpdater register) {
		ordersMonitor.set(books.numOrders());
		nextOrderIDMonitor.set(books.getNextOrderID());
		nextMatchIDMonitor.set(books.getNextMatchID());
		nSecuritiesMonitor.set(books.size());
		nContributorsMonitor.set(contributors.size());
		nAccountsMonitor.set(accounts.size());
		nTradersMonitor.set(traders.size());
		nStoreMonitor.set(store.size());
		nEventReceiverSeqNum.set(eventReceiver.getCurrentSeq());
	}

	@Exposed(name = "closeMarket")
	public void setClose() {
		marketHours.forceClose();
	}


	@Exposed(name = "startSession")
	public void startSession() {
		session = Long.toString(System.currentTimeMillis() / 1000);
		log.info(log.log().add("New Session Started : ").add(session));

	}



	@Exposed(name = "openMarket")
	public void setOpen() {
		marketHours.forceOpen();
	}

	@Exposed(name = "status")
	@Override
	public ByteStringBuffer status() {
		// TODO: see if we're primary
		status.clear();
		status.add("Name: ").add(name).addNL();
		status.add("Primary: ").add(isPrimary()).addNL();
		status.add("StoreSize: ").add(store.size()).addNL();
		status.add("Live Orders: ").add(books.numOrders()).addNL();
		status.add("Max Orders: ").add(MatchConstants.MAX_LIVE_ORDERS).addNL();
		status.add("Next OrderID: ").add(books.getNextOrderID()).addNL();
		status.add("Next MatchID: ").add(books.getNextMatchID()).addNL();
		status.add("Next ExternalOrderID: ").add(books.getNextExternalOrderID()).addNL();
		status.add("Securities: ").add(books.size()).addNL();
		status.add("Contributors: ").add(contributors.size()).addNL();
		status.add("Accounts: ").add(accounts.size()).addNL();
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

	@Exposed(name = "book")
	public String printBook(@Param(name = "security") String security) {
		short id = securities.getID(security);
		return books.printBook(id);
	}

	@Exposed(name = "openAsPrimary")
	public void openPrimary() {
		//Setting orderCommandHAndler has the listener for fill events and send that onto the stream
		books.setListener(orderCommandHandler);
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
		
		marketHours.checkStatus();
	}

	@Exposed(name = "openAsBackup")
	public void openBackup()
	{
		books.setListener(backupEventHandler);
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
			throw new CommandException("Error opening eventReceiver: " + e.getCause());
		}

		//rewinder.onPassive();
		rewinder.onActive();
		
		// don't send any events
		sender.close();
		sender.setSendEnabled(false);
		session=eventReceiver.getSession();
		eventReceiver.setSessionListener(this);
		sender.setSession(session); //backup session is read from the event receiver
	}

	@Exposed(name = "addContributor")
	public void addContributor(@Param(name = "name") String contributorName) {
        staticsCommandHandler.addContributor(contributorName);
	}

	@Exposed(name = "addTrader")
	public void addTrader(@Param(name = "name") String traderName, @Param(name = "accountName") String accountName
			,  @Param(name = "2YFatFingerQtyLimitMM") int ff2yLimit
			,  @Param(name = "3YFatFingerQtyLimitMM") int ff3yLimit
			,  @Param(name = "5YFatFingerQtyLimitMM") int ff5yLimit
			,  @Param(name = "7YFatFingerQtyLimitMM") int ff7yLimit
			,  @Param(name = "10YFatFingerQtyLimitMM") int ff10yLimit
			,  @Param(name = "30YFatFingerQtyLimitMM") int ff30yLimit) {
        staticsCommandHandler.addTrader(traderName, accountName, ff2yLimit,ff3yLimit,ff5yLimit,ff7yLimit,ff10yLimit,ff30yLimit);
	}

	@Exposed(name = "addAccount")
	public void addAccount(@Param(name = "name") String accountName
						, @Param(name = "netDV01Limit") int netDV01Limit
						, @Param(name = "stateStreetInternalID") String stateStreetInternalID
						, @Param(name = "netting") boolean netting
						, @Param(name = "commission") double commission) {
		if(!isPrimary()){
			throw new CommandException("Cannot send command. Seq is BACKUP");
		}
        staticsCommandHandler.addAccount(accountName, netDV01Limit,stateStreetInternalID, netting, commission);
	}
	private boolean isPrimary(){
		return sender.isSenderEnabled();
	}

    @Exposed(name = "addSecurity")
    public void addSecurity(
            @Param(name = "name") String securityName,
            @Param(name = "CUSIP") String cusip,
            @Param(name = "coupon") double coupon,
            @Param(name = "maturityDate") int maturityDate,
            @Param(name = "issueDate") int issueDate,
            @Param(name = "tickSize") int tickValue,
			@Param(name = "lotSize") int lotSize,
            @Param(name = "type") String type,
			@Param(name = "previousClosePrice") double referencePrice) {
		if(!isPrimary()){
			throw new CommandException("Cannot send command. Seq is BACKUP");
		}
        staticsCommandHandler.addSecurity(securityName, cusip, coupon, maturityDate, issueDate, tickValue, lotSize, 2, type,referencePrice);

    }

	//   //2Y10Y 2Y 10Y 4 1 0.001 1000 DiscreteSpread
	@Exposed(name = "addDiscreteSpread")
	public void addSecurity(
			@Param(name = "name") String securityName,
			@Param(name = "firstLegSecurity") String firstLeg,
			@Param(name = "secondLegSecurity") String secondLeg,
			@Param(name = "firstLegRatio") int firstLegRatio,
			@Param(name = "secondLegRatio") int secondLegRatio,

			@Param(name = "tickSize") double tickSize,
			@Param(name = "lotSize") int lotSize,
			@Param(name = "type") String type) {
		if(!isPrimary()){
			throw new CommandException("Cannot send command. Seq is BACKUP");
		}
		staticsCommandHandler.addSpread(securityName, firstLeg, secondLeg, firstLegRatio, secondLegRatio, tickSize, lotSize, type);

	}

    @Exposed(name="disableAccount")
    public void disableAccount( @Param(name="accountName") String accountName ) {
        if (!staticsCommandHandler.enableAccount(accountName, false)) {
			if(!isPrimary()){
				throw new CommandException("Cannot send command. Seq is BACKUP");
			}
            throw new CommandException("Could not find AccountID associated with name: " + accountName);
        }
    }

    @Exposed(name="enableAccount")
    public void enableAccount( @Param(name="accountName") String accountName )
    {
		if(!isPrimary()){
			throw new CommandException("Cannot send command. Seq is BACKUP");
		}
        if (!staticsCommandHandler.enableAccount(accountName, true)) {
            throw new CommandException("Could not find AccountID associated with name: " + accountName);
        }
    }

	@Exposed(name="disableTrader")
	public void disableTrader( @Param(name="traderName") String traderName ) {
		if(!isPrimary()){
			throw new CommandException("Cannot send command. Seq is BACKUP");
		}
        if (!staticsCommandHandler.enableTrader(traderName, false)) {
            throw new CommandException("Could not find TraderID associated with name: " + traderName);
        }
	}
	
	@Exposed(name="enableTrader")
	public void enableTrader( @Param(name="traderName") String traderName ) {
		if(!isPrimary()){
			throw new CommandException("Cannot send command. Seq is BACKUP");
		}
        if (!staticsCommandHandler.enableTrader(traderName, true)) {
            throw new CommandException("Could not find TraderID associated with name: " + traderName);
        }
	}
	
	@Exposed(name="disableSecurity")
	public void disableSecurity(@Param(name = "securityName") String securityName ) {
		if(!isPrimary()){
			throw new CommandException("Cannot send command. Seq is BACKUP");
		}
        if (!staticsCommandHandler.enableSecurity(securityName, false)) {
            throw new CommandException("Could not find SecurityID associated with name: " + securityName);
        }
	}
	
	@Exposed(name="enableSecurity")
	public void enableSecurity(@Param(name = "securityName") String securityName ) {
		if(!isPrimary()){
			throw new CommandException("Cannot send command. Seq is BACKUP");
		}
        if (!staticsCommandHandler.enableSecurity(securityName, true)) {
            throw new CommandException("Could not find SecurityID associated with name: " + securityName);
        }
	}

	@Exposed(name = "cancel")
	public void cancelOrder(@Param(name = "id") int id) {
		if(!isPrimary()){
			throw new CommandException("Cannot send command. Seq is BACKUP");
		}
        if (!orderCommandHandler.forceCancel(id)) {
            throw new CommandException("Unknown order id: " + id);
        }
	}

	@Override
	public boolean onOpen() {
        // stupid constructor thing
        if (staticsCommandHandler != null) {
            staticsCommandHandler.systemEvent(MatchConstants.SystemEvent.Open);
        }
		return isPrimary();
	}

	@Override
	public boolean onClose() {
        // stupid constructor thing
        if (staticsCommandHandler != null) {
            staticsCommandHandler.systemEvent(MatchConstants.SystemEvent.Close);
        }
		return isPrimary();
	}

	@Override
	public void onSessionStarted(String session) {
		this.session=session;
		this.sender.setSession(session);
		log.info(log.log().add("Session set : ").add(session));


	}
}
