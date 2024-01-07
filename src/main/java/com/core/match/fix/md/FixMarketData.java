package com.core.match.fix.md;

import com.core.app.AppConstructor;
import com.core.app.CommandException;
import com.core.app.Exposed;
import com.core.app.Param;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.connector.Connector;
import com.core.connector.Dispatcher;
import com.core.connector.MessageGroupCompleteListener;
import com.core.fix.FIXPortInfo;
import com.core.fix.FixParser;
import com.core.fix.FixWriter;
import com.core.fix.InlineFixParser;
import com.core.fix.InlineFixWriter;
import com.core.fix.connector.FIXConnectionListener;
import com.core.fix.connector.FixConnector;
import com.core.fix.connector.FixServerTcpConnector;
import com.core.fix.msgs.FixConstants;
import com.core.fix.msgs.FixDispatcher;
import com.core.fix.msgs.FixMarketDataRequestListener;
import com.core.fix.msgs.FixMsgTypes;
import com.core.fix.msgs.FixSecurityListRequestListener;
import com.core.fix.store.FixFileStore;
import com.core.fix.store.FixStore;
import com.core.fix.util.FixPrinter;
import com.core.match.MatchApplication;
import com.core.match.MatchCommandSender;
import com.core.match.fix.FixStateMachine;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchInboundCommand;
import com.core.match.msgs.MatchInboundEvent;
import com.core.match.msgs.MatchInboundListener;
import com.core.match.msgs.MatchMessages;
import com.core.match.msgs.MatchOutboundCommand;
import com.core.match.msgs.MatchOutboundEvent;
import com.core.match.msgs.MatchOutboundListener;
import com.core.match.services.book.MatchDisplayedPriceLevelBookService;
import com.core.match.services.book.MatchDisplayedPriceLevelBookServiceRoundingType;
import com.core.match.services.order.DisplayedOrder;
import com.core.match.services.order.DisplayedOrderService;
import com.core.match.services.order.OrderService;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.SecurityService;
import com.core.match.services.trades.TradeService;
import com.core.match.services.trades.TradeServiceListener;
import com.core.nio.SelectorService;
import com.core.services.price.PriceLevel;
import com.core.services.price.PriceLevelBook;
import com.core.services.price.PriceLevelBookUpdateListener;
import com.core.util.BinaryUtils;
import com.core.util.TimeUtils;
import com.core.util.datastructures.ArraySliceIterator;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerHandler;
import com.core.util.time.TimerService;
import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;
import com.gs.collections.impl.set.mutable.UnifiedSet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Set;

/**
 * User: jgreco
 */
public class FixMarketData extends MatchApplication
		implements
		FixSecurityListRequestListener,
		FixMarketDataRequestListener,
		MatchInboundListener,
		MatchOutboundListener,
		PriceLevelBookUpdateListener,
		TradeServiceListener<DisplayedOrder>,
        TimerHandler,
		MessageGroupCompleteListener {
	private final ByteBuffer temp = ByteBuffer.allocate(128);

	private final MatchMessages messages;

	private final MatchDisplayedPriceLevelBookService books;
	private final SecurityService<BaseSecurity> securities;

	private final FixConnector fixConnector;
	final FixStateMachine stateMachine;
	private final FixStore store;

	private final MarketDataTags tags;
	final IntObjectHashMap<MarketDataSubscription> subscriptions = new IntObjectHashMap<>();

	private FIXPortInfo info;
    private final Set<MarketDataSubscription> subscriptionsToSend = new UnifiedSet<>();

	private int marketDataLevels;
	public boolean connected;
	private TimerService timerService;

	@AppConstructor
	public FixMarketData(Log log,
						 TCPSocketFactory socketFactory,
						 FileFactory fileFactory,
						 TimerService timeFactory,
						 TimeSource timeSource,
						 Connector connector,
						 Dispatcher dispatcher,
						 MatchCommandSender sender,
						 MatchMessages msgs,
						 @Param(name = "Port") int port,
						 @Param(name = "Version") int version,
						 @Param(name = "SenderCompId") String senderCompID,
						 @Param(name = "TargetCompId") String targetCompID,
						 @Param(name = "Levels") int marketDataLevels) throws IOException {
		this(log,
				timeSource,
				timeFactory,
				connector,
				dispatcher,
				sender,
				msgs,
				new FixServerTcpConnector(log, socketFactory, sender, port),
				new InlineFixParser(),
				new InlineFixWriter(timeSource, version, targetCompID, senderCompID),
				new FixFileStore(fileFactory, log, sender.getName()),
				new FixDispatcher(log), version, senderCompID, targetCompID, marketDataLevels);
		info = new FIXPortInfo(port, version, senderCompID, targetCompID);
	}

	public FixMarketData(Log log,
						 TimeSource source,
						 TimerService timerService,
						 Connector connector,
						 Dispatcher dispatcher,
						 MatchCommandSender sender,
						 MatchMessages messages,
						 FixConnector fixConnector,
						 FixParser parser,
						 FixWriter writer,
						 FixStore store,
						 FixDispatcher fixDispatcher,
						 int minorVersion,
						 String senderCompID,
						 String targetCompID,
						 int marketDataLevels) {
		super(log, sender);

		this.marketDataLevels = marketDataLevels;

		this.securities = SecurityService.create(log, source);

		this.tags = new MarketDataTags(parser);

		this.messages = messages;

		this.timerService = timerService;
		
		this.stateMachine = new FixStateMachine(log, fixDispatcher, timerService, sender, parser, store, fixConnector, messages, false, false, minorVersion, senderCompID, targetCompID);

		this.fixConnector = fixConnector;
		this.fixConnector.init(parser, stateMachine);
		this.fixConnector.addListener(new FixPrinter(log));
		this.fixConnector.addConnectionListener(new FIXConnectionListener() {
			@Override
			public void onConnect() {
				connected = true;
				stateMachine.resetSequenceNums();
				subscriptions.clear();
			}

			@Override
			public void onDisconnect() {
				connected = false;
				stateMachine.resetSequenceNums();
				subscriptions.clear();
			}
		});

		this.store = store;
		this.store.init(writer, this.fixConnector);

		OrderService<DisplayedOrder> orders = OrderService.create(DisplayedOrder.class, log, dispatcher);

		DisplayedOrderService<DisplayedOrder> displayedOrderService = new DisplayedOrderService<>(orders, log);
		this.books = new MatchDisplayedPriceLevelBookService(displayedOrderService, securities, log, MatchDisplayedPriceLevelBookServiceRoundingType.NONE);

		this.books.addListener(this);

        TradeService<DisplayedOrder> trades = new TradeService<>(orders);
        trades.addListener(this);

        sender.addContributorDefinedListener(stateMachine);

        connector.addMessageGroupCompleteListener(this);

		fixDispatcher.subscribe(this);

		dispatcher.subscribe(this);
		dispatcher.subscribe(stateMachine);
		dispatcher.subscribe(securities);
		dispatcher.subscribe(orders);
	}

	@Exposed(name = "setInbound")
	public void setNextInboundSeqNum(@Param(name = "SeqNum") int seqNum) {
		stateMachine.setNextInboundSeqNum(seqNum);
	}

	@Exposed(name = "setOutbound")
	public void setNextOutboundSeqNum(@Param(name = "SeqNum") int seqNum) {
		stateMachine.setNextOutboundSeqNo(seqNum, false);
	}

	@Exposed(name = "sendTestRequest")
	public void sendTestRequest() {
		if (!canSend()) {
			throw new CommandException("Cannot send a test request while a command is in flight");
		}
		stateMachine.sendTestRequest();
	}

	@Exposed(name = "sendHeartbeat")
	public void sendHeartbeat() {
		if (!canSend()) {
			throw new CommandException("Cannot send a heartbeat while a command is in flight");
		}
		stateMachine.sendHeartbeat();
	}

	@Override
	protected void onActive() {
		try {
			fixConnector.open();
		} catch (IOException e) {
			throw new CommandException("Could not open FIX connector");
		}
		this.timerService.scheduleTimer(0, this);
	}

	@Override
	protected void onPassive() {
		try {
			fixConnector.close();
		}
		catch (IOException e) {
			throw new CommandException("Error closing server socket");
		}
	}

	@Override
	public void onAddHeartbeatFields(HeartbeatFieldRegister register) {
		if (info != null) {
			info.addStatus(register);
		}
		stateMachine.addStatus(register);
	}

	@Override
	public void onUpdateHeartbeatFields(HeartbeatFieldUpdater register) {
		stateMachine.updateStatus();
	}

	@Override
	public void onFixSecurityListRequest() {
		temp.clear();

		if (!tags.checkFields(getSender(), messages, tags.requiredSecurityListRequest, FixMsgTypes.BusinessReject)) {
			return;
		}

		MatchOutboundCommand outbound = messages.getMatchOutboundCommand();
		outbound.setFixMsgType(FixMsgTypes.SecurityList);
		outbound.setReqID(tags.securityReqId.getValue());
		send(outbound);
	}

	@Override
	public void onFixMarketDataRequest() {
		temp.clear();

		if (!tags.checkFields(getSender(), messages, tags.requiredMarketDataRequest, FixMsgTypes.MarketDataRequestReject)) {
			return;
		}

		BaseSecurity security = securities.get(tags.symbol.getValue());

		if (security == null) {
			temp.clear();
			BinaryUtils.copy(temp, "Unknown Symbol<55>: ");
			temp.put(tags.symbol.getValue());
			temp.flip();

			MatchOutboundCommand outbound = messages.getMatchOutboundCommand();
			outbound.setFixMsgType(FixMsgTypes.MarketDataRequestReject);
			outbound.setReqID(tags.mdReqId.getValue());
			outbound.setText(temp);

			send(outbound);
			return;
		}

		MatchInboundCommand inbound = messages.getMatchInboundCommand();
		inbound.setFixMsgType(tags.msgType.getValueAsChar());
		inbound.setReqID(tags.mdReqId.getValue());
		inbound.setSecurityID(security.getID());
		send(inbound);
	}

	@Override
	public void onMatchInbound(MatchInboundEvent msg) {
		if (msg.getContributorID() != getContribID()) {
			return;
		}

		if (!connected) {
			return;
		}

		if (msg.getFixMsgType() == FixMsgTypes.MarketDataRequest) {
			BaseSecurity security = securities.get(msg.getSecurityID());
			MarketDataSubscription sub = new MarketDataSubscription(msg.getReqIDAsString(), books.getBook(security), this.marketDataLevels);
			subscriptions.put(security.getID(), sub);
            PriceLevelBook book = books.getBook(msg.getSecurityID());
            sendIncremental(book);
		}
	}

	private void send(MarketDataSubscription subscription) {
		if (!connected) {
			return;
		}

		subscription.reset();
		categorizeLevels(subscription, true, subscription.getBidsIterator(), subscription.getLastBidsIterator());
		categorizeLevels(subscription, false, subscription.getOffersIterator(), subscription.getLastOffersIterator());
 		Iterator<PriceLevel> lastTradesIterator = subscription.getLastTradesIterator();
		while(lastTradesIterator.hasNext())
		{
			subscription.addUpdate(FixConstants.MDUpdateAction.New, lastTradesIterator.next(), FixConstants.MDEntryType.Trade);
		}

		if( subscription.getUpdateLength() == 0 ) {
			return;
		}
		FixWriter incremental = store.createMessage(FixMsgTypes.MarketDataIncrementalRefresh);
		incremental.writeString(tags.mdReqId, subscription.getReqId());
		incremental.writeNumber(tags.noMdEntries, subscription.getUpdateLength());

		for (int i = 0; i < subscription.getUpdateLength(); i++) {
			MarketDataSubscription.MarketDataUpdate next = subscription.getUpdate(i);
			incremental.writeChar(tags.mdUpdateAction, next.updateType);
			incremental.writeChar(tags.mdEntryType, next.entryType);
			incremental.writeString(tags.symbol, subscription.getSecurityName());
			incremental.writePrice(tags.mdEntryPx, next.level.price, MatchConstants.IMPLIED_DECIMALS);
			incremental.writeNumber(tags.mdEntrySize, next.level.qty * MatchConstants.QTY_MULTIPLIER);
		}

		store.finalizeBusinessMessage();
		subscription.copyCurrentToLast();
	}

	private void categorizeLevels(MarketDataSubscription subscription, boolean bid, PriceLevelBook.PriceLevelIterator currentIt, ArraySliceIterator<PriceLevel> lastIt) {
		char entryType = bid ? FixConstants.MDEntryType.Bid : FixConstants.MDEntryType.Offer;

		lastIt.reset();
		PriceLevel lastLevel = lastIt.hasNext() ? lastIt.next() : null;
		PriceLevel currentLevel = currentIt.hasNext() ? currentIt.next() : null;
		int currentSize = 0;

		// Iteration through current and last reported levels
		// 1) Price is the same.
		//    MdUpdateAction.Change: Qty at price level has changed
		//    Nothing: Qty at price level has not changed
		// 2) The current offer price is less than the last offer or the current bid price is greater than the last bid
		//    MdUpdateAction.New.  This price level is new to the user.
		// 3) The current offer price is greater than the last offer or the current bid price is less than the last bid
		//    MdUpdateAction.Delete.  This price level no longer exists, but was reported to the user previously.
		while (lastLevel != null && currentLevel != null && currentSize < this.marketDataLevels) {
			boolean currentLevelLessThanLastLevel = currentLevel.price < lastLevel.price;

			if( currentLevel.price == lastLevel.price ) {
				// both exist
				if( currentLevel.qty != lastLevel.qty ) {
					subscription.addUpdate(FixConstants.MDUpdateAction.Change, currentLevel, entryType);
				}
				currentLevel = currentLevel.next();
				lastLevel = lastIt.next();
				currentSize++;
			}
			else if( ( currentLevelLessThanLastLevel && !bid ) || ( !currentLevelLessThanLastLevel && bid )) {
				// exists in current but not last, so its an add
				subscription.addUpdate(FixConstants.MDUpdateAction.New, currentLevel, entryType);
				currentLevel = currentLevel.next();
				currentSize++;
			}
			else {
				// exists in last but not current
				subscription.addUpdate(FixConstants.MDUpdateAction.Delete, lastLevel, entryType);
				lastLevel = lastIt.next();
			}
		}

		// the while loop can break with one of them not having been exhausted
 		if(lastLevel != null)
		{
			// We need to delete everything else outstanding to the user
			while(lastLevel != null)
			{
				subscription.addUpdate(FixConstants.MDUpdateAction.Delete, lastLevel, entryType);
				lastLevel = lastIt.next();
			}
		}
		else
		{
			// we know there is more current levels left
			// iterate through and add as new levels
			while(currentLevel != null && currentSize < this.marketDataLevels)
			{
				subscription.addUpdate(FixConstants.MDUpdateAction.New, currentLevel, entryType);
				currentLevel = currentLevel.next();
				currentSize++;
			}
		}
	}

	@Override
	public void onMatchOutbound(MatchOutboundEvent msg) {
		if (msg.getContributorID() != getContribID()) {
			return;
		}

		if (!connected) {
			return;
		}

		switch (msg.getFixMsgType()) {
			case FixMsgTypes.SecurityList:
				sendSecurityList(msg);
				break;
			case FixMsgTypes.MarketDataRequestReject:
				sendMarketDataRequestReject(msg);
				break;
			case FixMsgTypes.BusinessReject:
				sendBusinessReject(msg);
				break;
			default:
				break;
		}
	}

	private void sendBusinessReject(MatchOutboundEvent msg) {
		stateMachine.incInboundSeqNo();

		FixWriter writer = store.createMessage(msg.getFixMsgType());
		writer.writeNumber(tags.refSeqNum, msg.getRefSeqNum());
		if (msg.hasText()) {
			writer.writeString(tags.text, msg.getText());
		}
		writer.writeChar(tags.refMsgType, msg.getRefMsgType());
		writer.writeNumber(tags.businessRejectRefID, msg.getRefTagID());
		writer.writeChar(tags.businessRejectReason, msg.getSessionRejectReason());

		store.finalizeBusinessMessage();
	}

	private void sendMarketDataRequestReject(MatchOutboundEvent msg) {
		stateMachine.incInboundSeqNo();

		FixWriter writer = store.createMessage(msg.getFixMsgType());
		writer.writeString(tags.mdReqId, msg.getReqID());
		writer.writeString(tags.text, msg.getText());

		store.finalizeBusinessMessage();
	}

	private void sendSecurityList(MatchOutboundEvent msg) {
		stateMachine.incInboundSeqNo();

		FixWriter writer = store.createMessage(msg.getFixMsgType());
		writer.writeString(tags.securityReqId, msg.getReqID());
		writer.writeString(tags.securityResponseId, "SEC_LIST_001");
		writer.writeChar(tags.securityRequestResult, FixConstants.SecurityRequestResult.ValidRequest);
		writer.writeNumber(tags.totNoRelatedSym, securities.size());
		writer.writeChar(tags.lastFragment, FixConstants.LastFragment.Yes);
		writer.writeNumber(tags.noRelatedSym, securities.size());

		Iterator<BaseSecurity> iterator = securities.iterator();
		while (iterator.hasNext()) {
			BaseSecurity baseSecurity = iterator.next();
			if(baseSecurity.isMultiLegInstrument()){
				//TODO: DO this for multileg
				return;
			}
			Bond security=(Bond)baseSecurity;
			writer.writeString(tags.symbol, security.getName());
			writer.writeString(tags.securityId, security.getCUSIP());
			writer.writeChar(tags.securityIdSource, FixConstants.SecurityIDSource.CUSIP);
			writer.writeString(tags.securityType, security.getType().getFixName());
			writer.writeNumber(tags.maturityDate, TimeUtils.toDateInt(security.getMaturityDate()));
			writer.writePrice(tags.couponRate, security.getCoupon(), MatchConstants.IMPLIED_DECIMALS);
		}

		store.finalizeBusinessMessage();
	}

	@Override
	public void onPriceLevelAdded(PriceLevelBook book, boolean buy, PriceLevel level, int position) {
		sendIncremental(book);
	}

	@Override
	public void onPriceLevelRemoved(PriceLevelBook book, boolean buy, PriceLevel level, int position) {
		sendIncremental(book);
	}

	@Override
	public void onPriceLevelChanged(PriceLevelBook book, boolean buy, PriceLevel level, int position) {
		sendIncremental(book);
	}

	@Override
	public void onTrade(long timestamp, int matchID, long execPrice, int execQty, DisplayedOrder associatedOrder, boolean aggressor) {
		// we only care about matches, otherwise we'd double count trades we report
	}

	@Override
	public void onMatch(long timestamp, int matchID, long execPrice, int execQty, short securityID) {
		MarketDataSubscription marketDataSubscription = subscriptions.get(securityID);
        PriceLevelBook book = books.getBook(securityID);
        if( marketDataSubscription != null ) {
			marketDataSubscription.addTrade(execQty, execPrice);
			sendIncremental(book);
		}
	}

    private void sendIncremental(PriceLevelBook book) {
        MarketDataSubscription subscription = subscriptions.get(book.getSecurityID());
        if (subscription != null) {
			/*int incremented = subscription.incrementAndGetUpdatesSinceLastTimer();
			if(incremented == this.updatesPerPeriod) {
				subscriptionsAtCapacity.add(subscription);
			}
			else if( incremented > this.updatesPerPeriod ) {
				return;
			}
			else {
				sendIncremental(subscription);
			}*/
            subscriptionsToSend.add(subscription);
            //sendIncremental(subscription);
        }
    }

	@Override
	public void onTimer(int internalTimerID, int referenceData) {

        if (!connected) {
			subscriptionsToSend.clear();
			return;
		}

		Iterator<MarketDataSubscription> iterator = subscriptionsToSend.iterator();
		while (iterator.hasNext()) {
			MarketDataSubscription sub = iterator.next();
			send(sub);
		}

		subscriptionsToSend.clear();

		this.timerService.scheduleTimer(0, this);
	}

    @Override
    public void onMessageGroupComplete(long nextSeqNum) {

    }
}
