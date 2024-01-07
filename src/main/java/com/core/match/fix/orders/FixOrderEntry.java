package com.core.match.fix.orders;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import com.core.app.AppConstructor;
import com.core.app.CommandException;
import com.core.app.Exposed;
import com.core.app.Param;
import com.core.app.heartbeats.HeartBeatFieldIDEnum;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.app.heartbeats.HeartbeatNumberField;
import com.core.app.heartbeats.HeartbeatStringField;
import com.core.connector.AllCommandsClearedListener;
import com.core.connector.Connector;
import com.core.connector.Dispatcher;
import com.core.fix.FIXPortInfo;
import com.core.fix.FixParser;
import com.core.fix.FixWriter;
import com.core.fix.InlineFixParser;
import com.core.fix.InlineFixWriter;
import com.core.fix.connector.FIXConnectionListener;
import com.core.fix.connector.FixServerTcpConnector;
import com.core.fix.msgs.FixConstants;
import com.core.fix.msgs.FixDispatcher;
import com.core.fix.msgs.FixNewOrderSingleListener;
import com.core.fix.msgs.FixOrderCancelReplaceRequestListener;
import com.core.fix.msgs.FixOrderCancelRequestListener;
import com.core.fix.msgs.FixTags;
import com.core.fix.store.FixFileStore;
import com.core.fix.store.FixStore;
import com.core.fix.tags.FixTag;
import com.core.fix.util.FixPrinter;
import com.core.match.MatchApplication;
import com.core.match.MatchCommandSender;
import com.core.match.fix.FixOrder;
import com.core.match.fix.FixOrderRepository;
import com.core.match.fix.FixOrderService;
import com.core.match.fix.FixStateMachine;
import com.core.match.msgs.MatchCancelCommand;
import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchCancelReplaceRejectEvent;
import com.core.match.msgs.MatchClientCancelReplaceRejectCommand;
import com.core.match.msgs.MatchClientCancelReplaceRejectEvent;
import com.core.match.msgs.MatchClientOrderRejectCommand;
import com.core.match.msgs.MatchClientOrderRejectEvent;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchMessages;
import com.core.match.msgs.MatchOrderCommand;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchOrderRejectEvent;
import com.core.match.msgs.MatchReplaceCommand;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.contributor.Contributor;
import com.core.match.services.contributor.ContributorService;
import com.core.match.services.events.SystemEventListener;
import com.core.match.services.events.SystemEventService;
import com.core.match.services.order.OrderServiceListener;
import com.core.match.services.order.OrderServiceRejectListener;
import com.core.match.services.order.ReplaceUpdates;
import com.core.match.services.risk.RiskService;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.SecurityService;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.match.util.MatchBondMath;
import com.core.util.BinaryUtils;
import com.core.util.TextUtils;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerService;

/**
 * User: jgreco
 */
public class FixOrderEntry extends MatchApplication
		implements
		FIXConnectionListener,
		FixNewOrderSingleListener,
		FixOrderCancelReplaceRequestListener,
		FixOrderCancelRequestListener,
		OrderServiceListener<FixOrder>,
		OrderServiceRejectListener<FixOrder>,
		SystemEventListener, 
		AllCommandsClearedListener {
    private static final int CONCURRENT_ORDERS = 10000;

	private final FixTag[] requiredOrderTags;
	private final FixTag[] requiredCancelTags;
	private final FixTag[] requiredReplaceTags;
	private final FixTag account;
	private final FixTag side;
	private final FixTag orderQty;
	private final FixTag orderType;
	private final FixTag symbol;
	private final FixTag price;
	private final FixTag tif;
	private final FixTag clOrdId;
	private final FixTag origClOrdId;

	final String accountName;

    private final ByteBuffer temp = ByteBuffer.allocate(128);

	private final MatchMessages messages;
	private final SecurityService<BaseSecurity> securities;

	private final Connector connector;
	private final FixServerTcpConnector fixConnector;
	private final FixStateMachine stateMachine;
    private final FixOrderRepository fixOrderRepository;
	private final FixOrderService orderService;

	private final FIXQtyMode qtyMode;
    private final FIXOrderOutput<FixOrder> fixOutput;

    protected FIXPortInfo info;
    private final TraderService<Trader> traderService;

    private HeartbeatNumberField ordersSent;
    private HeartbeatNumberField cancelsSent;
    private HeartbeatNumberField replacesSent;
    private HeartbeatNumberField rejectsSent;
    private HeartbeatNumberField cancelReplaceRejectsSent;
    private HeartbeatNumberField fillsSent;
    private HeartbeatNumberField liveOrders;
	private HeartbeatNumberField accountIDMonitor;

	private boolean marketOpen;
	private Account accountObj;

	private Contributor contributor;
	private boolean isConnected;
	private HeartbeatStringField accountField;
	private final RiskService riskService;

	@AppConstructor
    public FixOrderEntry(Log log,
						 TCPSocketFactory socketFactory,
						 FileFactory fileFactory,
						 TimerService timeFactory,
						 TraderService<Trader> traderService,
						 AccountService<Account> accountService,
						 SecurityService<BaseSecurity> securityService,
						 SystemEventService systemEventService,
						 ContributorService<Contributor> contributorService,
						 TimeSource timeSource,
						 Dispatcher dispatcher,
						 MatchCommandSender sender,
						 Connector connector,
						 MatchMessages msgs,
						 @Param(name = "Port") int port,
						 @Param(name = "SenderCompId") String senderCompID,
						 @Param(name = "TargetCompId") String targetCompID,
						 @Param(name = "Account") String accountName) throws IOException {
		this(log, dispatcher, timeFactory, traderService, accountService, securityService, systemEventService, contributorService, sender, connector, msgs, new FixServerTcpConnector(log, socketFactory, sender, port),
                new InlineFixParser(), new InlineFixWriter(timeSource, 4, targetCompID, senderCompID), new FixFileStore(
                        fileFactory, log, sender.getName()), new FixDispatcher(log), senderCompID, targetCompID, accountName, FIXQtyMode.Notional);
		
		this.info = new FIXPortInfo(port, 4, senderCompID, targetCompID);
	}

	public FixOrderEntry(Log log,
						 Dispatcher dispatcher,
						 TimerService timerService,
						 TraderService<Trader> traderService,
						 AccountService<Account> accountService,
						 SecurityService<BaseSecurity> securityService,
						 SystemEventService systemEventService,
						 ContributorService<Contributor> contributorService,
						 MatchCommandSender sender,
						 Connector connector,
						 MatchMessages msgs,
						 FixServerTcpConnector fixConnector,
						 FixParser parser,
						 FixWriter writer,
						 FixStore store,
						 FixDispatcher fixDispatcher,
						 String senderCompID,
						 String targetCompID,
						 String accountName,
						 FIXQtyMode qtyMode) {
		super(log, sender);

		this.account = parser.createReadWriteFIXTag(FixTags.Account);
		this.side = parser.createReadWriteFIXTag(FixTags.Side);
		this.orderQty = parser.createReadWriteFIXTag(FixTags.OrderQty);
		this.orderType = parser.createReadWriteFIXTag(FixTags.OrdType);
		this.symbol = parser.createReadWriteFIXTag(FixTags.Symbol);
		this.price = parser.createReadWriteFIXTag(FixTags.Price);
		this.clOrdId = parser.createReadWriteFIXTag(FixTags.ClOrdID);
		this.origClOrdId = parser.createReadWriteFIXTag(FixTags.OrigClOrdID);
		this.tif = parser.createReadWriteFIXTag(FixTags.TimeInForce);

		this.requiredOrderTags = new FixTag[] {
				side, account, orderQty, symbol, price, clOrdId, orderType
		};

		this.requiredCancelTags = new FixTag[] {
				origClOrdId, clOrdId
		};

		this.requiredReplaceTags = new FixTag[] {
				side, orderQty, symbol, price, clOrdId, origClOrdId
		};

		sender.addAllCommandsClearedListener(this);

		this.qtyMode = qtyMode;

        this.securities = securityService;
		this.messages = msgs;
		this.stateMachine = new FixStateMachine(log, fixDispatcher, timerService, sender, parser, store, fixConnector, msgs, false, false, 4, senderCompID, targetCompID);
        this.orderService = new FixOrderService(log, dispatcher, CONCURRENT_ORDERS);
		this.connector = connector;
		this.fixConnector = fixConnector;
		this.fixConnector.addConnectionListener(this);
		this.fixConnector.init(parser, stateMachine);
		this.fixConnector.addListener(new FixPrinter(log));

		store.init(writer, this.fixConnector);
		this.fixOrderRepository = new FixOrderRepository(CONCURRENT_ORDERS);
		this.accountName = accountName;
		this.orderService.addListener(fixOrderRepository);
		this.orderService.addRejectListener(fixOrderRepository);
		this.orderService.addListener(this);
		this.orderService.addRejectListener(this);
		contributorService.addListener((contributor1, msg, isNew) -> FixOrderEntry.this.contributor = contributor1);

        accountService.addListener((accountUpdate, msg, isNew) -> {
            if (accountUpdate.getName().equals(this.accountName)) {
                this.accountIDMonitor.set(accountUpdate.getID());
                this.accountObj = accountUpdate;
            }
        });

        this.traderService = traderService;
         riskService = new RiskService<>(accountService, this.traderService, securities, log);
        orderService.addListener(riskService);
        accountService.addListener(riskService);

        this.fixOutput = new FIXOrderOutput<>(parser, store, this.qtyMode, traderService, accountService, securityService);

        riskService.isInterestedInAccount(this.accountName);

		sender.addAllCommandsClearedListener(fixConnector);
		sender.addContributorDefinedListener(orderService);
		sender.addContributorDefinedListener(stateMachine);

		fixDispatcher.subscribe(this);
		systemEventService.addListener(this);
		dispatcher.subscribe(this);
		dispatcher.subscribe(stateMachine);
	}

	@Exposed(name = "setInbound")
	public void setNextInboundSeqNum(@Param(name = "SeqNum") int seqNum) {
		log.info(log.log().add("Changing next inbound seq from ").add(stateMachine.getNextInboundSeqNo()).add(" => ").add(seqNum));
		stateMachine.setNextInboundSeqNum(seqNum);
	}

	@Exposed(name = "resetSeqNumForTest_DoNotExecute")
	public void resetSeqNumForTest() {
		log.info(log.log().add("Changing next inbound seq from ").add(stateMachine.getNextInboundSeqNo()).add(" => ").add(0));
		log.info(log.log().add("Changing next outbound seq from ").add(stateMachine.getNextOutboundSeqNo()).add(" => ").add(0));
		stateMachine.setNextInboundSeqNum(1);
		stateMachine.setNextOutboundSeqNo(1, true);
	}

	@Exposed(name = "setOutbound")
	public void setNextOutboundSeqNum(@Param(name = "SeqNum") int seqNum) {
		log.info(log.log().add("Changing next outbound seq from ").add(stateMachine.getNextOutboundSeqNo()).add(" => ").add(seqNum));
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
	protected void beforeActive() {
		if (this.accountObj == null) {
			throw new CommandException("Account not defined: " + accountName);
		}
	}

	@Override
	protected void onActive() {
		try {
			fixConnector.open();
		} catch (IOException e) {
			throw new CommandException("Could not open FIX connector");
		}
	}

	@Override
	protected void onPassive() {
		try {
			this.fixConnector.close();
		}
		catch (IOException e) {
			throw new CommandException("Error closing server socket");
		}
	}

	@Override
    public void onAddHeartbeatFields(HeartbeatFieldRegister register) {
		accountField = register.addStringField("Stats", HeartBeatFieldIDEnum.Acct);
		accountIDMonitor = register.addNumberField("Stats", HeartBeatFieldIDEnum.AcctID);

        if (info != null) {
            info.addStatus(register);
        }
        stateMachine.addStatus(register);

		liveOrders = register.addNumberField("Stats", HeartBeatFieldIDEnum.LiveOrders);
        ordersSent = register.addNumberField("Stats", HeartBeatFieldIDEnum.SendOrders);
        cancelsSent = register.addNumberField("Stats", HeartBeatFieldIDEnum.Cancels);
        replacesSent = register.addNumberField("Stats", HeartBeatFieldIDEnum.Replaces);
        rejectsSent = register.addNumberField("Stats", HeartBeatFieldIDEnum.Rejects);
        cancelReplaceRejectsSent = register.addNumberField("Stats", HeartBeatFieldIDEnum.CnlRplRejects);
        fillsSent = register.addNumberField("Stats", HeartBeatFieldIDEnum.Fills);
    }

	@Override
    public void onUpdateHeartbeatFields(HeartbeatFieldUpdater register) {
		stateMachine.updateStatus();
        liveOrders.set(orderService.size());
		accountField.set(accountName);

	}

	@Override
        public void onFixNewOrderSingle() {
        temp.clear();

        if (!checkRequiredOrderFields()) {
            return;
        }

        char sideChar = side.getValueAsChar();
        boolean buy = sideChar == FixConstants.Side.Buy;
        int qty = externalToInternalQty(orderQty.getValueAsInt());
		char orderTypeVal = orderType.getValueAsChar();
		if (orderTypeVal != FixConstants.OrdType.Limit) {
			rejectOrder(BinaryUtils.copy(temp, "Expect Limit. Invalid OrdType<40>: ").put(orderType.getValue()));
			return;
		}

		long limitPrice = price.getValueAsPrice(MatchConstants.IMPLIED_DECIMALS);

		if (!checkClOrdID(false)) {
            return;
        }
        
        if (!marketOpen ) {
        	rejectOrder(BinaryUtils.copy(temp, "Market Closed"));
        	return;
        }
        
        if (fixOrderRepository.seenClOrdID(clOrdId.getValue())) {
            rejectOrder(BinaryUtils.copy(temp, "Duplicate ClOrdID<11>: ").put(clOrdId.getValue()));
            return;
        }
        
        if (sideChar != FixConstants.Side.Buy && sideChar != FixConstants.Side.Sell) {
            rejectOrder(BinaryUtils.copy(temp, "Invalid Side<54>: ").put(side.getValue()));
            return;
        }

        if (qty <= 0) {
            rejectOrder(BinaryUtils.copy(temp, "Invalid OrderQty<38>: ").put(orderQty.getValue()));
            return;
        }

        if (limitPrice <= 0) {
            rejectOrder(BinaryUtils.copy(temp, "Invalid Price<44>: ").put(price.getValue()));
            return;
        }

        if (securities.getBond(symbol.getValue()) == null ) {
			if(securities.getMultiLegSecurityInstrument(symbol.getValue())!=null){
				log.error(log.log().add("MultiLeg Instrument Not supported"));
			}
            rejectOrder(BinaryUtils.copy(temp, "Invalid Symbol<55>: ").put(symbol.getValue()));
            return;
        }
		Bond security = securities.getBond(symbol.getValue());

        if (limitPrice % security.getTickSize() != 0) {
        	rejectOrder(BinaryUtils.copy(temp, "Non-tick Increment Price<44>: ").put(price.getValue()));
        	return;
        }

        boolean ioc = false;
        if (tif.isPresent()) {
            char tifChar = tif.getValueAsChar();
            switch (tifChar) {
                case FixConstants.TimeInForce.Day:
                    break;
                case FixConstants.TimeInForce.IOC:
                    ioc = true;
                    break;
                default:
                    rejectOrder(BinaryUtils.copy(temp, "Invalid TIF<59>: ").put(price.getValue()));
                    return;
            }
        }
        
        // 1 to 1 mapping between Account<1> and our internal trader concept
        Trader trader = this.traderService.get(account.getValue());
        if (trader == null || trader.getAccountID() != accountObj.getID()) {
            rejectOrder(BinaryUtils.copy(temp, "Invalid Account<1>: ").put(account.getValue()));
            return;
        }

		if( RiskService.violatesFatFingerQuantityLimit(trader, qty, security)){
			BinaryUtils.copy( temp, "Violation of Fat Finger risk check: " );
			BinaryUtils.copy(temp, accountName);
			rejectOrder(temp);
			return;
		}
		// check DV01 limit
		// based on current DV01 + new order's DV01
		double dv01 = MatchBondMath.getSignedDV01(security, limitPrice, qty, buy);

		if( riskService.violatesDV01Limit(this.accountObj, dv01) ){
            BinaryUtils.copy( temp, "Account Net DV01 violation: " );
            BinaryUtils.copy(temp, accountName);
            rejectOrder(temp);
            return;
        }

		MatchOrderCommand order = messages.getMatchOrderCommand();
		order.setBuy(buy);
		order.setSecurityID(security.getID());
		order.setQty(qty);
		order.setPrice(limitPrice);
		order.setTraderID(trader.getID());
		order.setClOrdID(clOrdId.getValue());
		order.setIOC(ioc);
		send(order);
	}

	@Override
	public void onFixOrderCancelRequest() {
		temp.clear();

		if (!checkRequiredCancelReplaceFields(requiredCancelTags, false)) {
			return;
		}

		if (!checkClOrdID(true)) {
			return;
		}

		FixOrder order = fixOrderRepository.getOrder(origClOrdId.getValue());
		if (order == null) {
			boolean everyExisted = fixOrderRepository.seenClOrdID(origClOrdId.getValue());
			if (everyExisted) {
				rejectCancelReplace(BinaryUtils.copy(temp, "Old OrigClOrdID<11>: ").put(origClOrdId.getValue()),
						FixConstants.CxlRejectReason.TooLateToCancel, false, 0);
			}
			else {
				rejectCancelReplace(BinaryUtils.copy(temp, "Unknown OrigClOrdID<11>: ").put(origClOrdId.getValue()),
						FixConstants.CxlRejectReason.UnknownOrder, false, 0);
			}
			return;
		}

		if (fixOrderRepository.seenClOrdID(clOrdId.getValue())) {
			rejectCancelReplace(BinaryUtils.copy(temp, "Duplicate ClOrdID<11>: ").put(clOrdId.getValue()),
					FixConstants.CxlRejectReason.BrokerOption, false, order.getID());
			return;
		}

		MatchCancelCommand cancel = messages.getMatchCancelCommand();
		cancel.setOrderID(order.getID());
		cancel.setClOrdID(clOrdId.getValue());
		cancel.setOrigClOrdID(origClOrdId.getValue());
		send(cancel);
	}

	@Override
	public void onFixOrderCancelReplaceRequest() {
		temp.clear();

		if (!checkRequiredCancelReplaceFields(requiredReplaceTags, true)) {
			return;
		}

		if (!checkClOrdID(true)) {
			return;
		}

		FixOrder order = fixOrderRepository.getOrder(origClOrdId.getValue());
		if (order == null) {
			boolean everyExisted = fixOrderRepository.seenClOrdID(origClOrdId.getValue());
			if (everyExisted) {
				rejectCancelReplace(BinaryUtils.copy(temp, "Old OrigClOrdID<41>: ").put(origClOrdId.getValue()),
						FixConstants.CxlRejectReason.TooLateToCancel, true, 0);
			}
			else {
				rejectCancelReplace(BinaryUtils.copy(temp, "Unknown OrigClOrdId<41>: ").put(origClOrdId.getValue()),
						FixConstants.CxlRejectReason.UnknownOrder, true, 0);
			}
			return;
		}

		if (fixOrderRepository.seenClOrdID(clOrdId.getValue())) {
			rejectCancelReplace(BinaryUtils.copy(temp, "Duplicate ClOrdID<11>: ").put(clOrdId.getValue()),
					FixConstants.CxlRejectReason.BrokerOption, true, order.getID());
			return;
		}

		char sideChar = side.getValueAsChar();
		boolean buy = sideChar == FixConstants.Side.Buy;
		int qty = externalToInternalQty(orderQty.getValueAsInt());
		long limitPrice = price.getValueAsPrice(MatchConstants.IMPLIED_DECIMALS);

		if (sideChar != FixConstants.Side.Buy && sideChar != FixConstants.Side.Sell) {
			rejectCancelReplace(BinaryUtils.copy(temp, "Invalid Side<54>: ").put(side.getValue()),
					FixConstants.CxlRejectReason.BrokerOption, true, order.getID());
			return;
		}
		if (buy != order.isBuy()) {
			rejectCancelReplace(BinaryUtils.copy(temp, "Cannot Replace Side<54>"), FixConstants.CxlRejectReason.BrokerOption, true, order.getID());
			return;
		}

		if (qty <= 0) {
			rejectCancelReplace(BinaryUtils.copy(temp, "Invalid OrderQty<38>: ").put(orderQty.getValue()),
					FixConstants.CxlRejectReason.BrokerOption, true, order.getID());
			return;
		}
		if (qty <= order.getCumQty()) {
			rejectCancelReplace(BinaryUtils.copy(temp, "OrderQty<38> <= CumQty<14>: ").put(orderQty.getValue()),
                    FixConstants.CxlRejectReason.BrokerOption, true, order.getID());
			return;
		}

		if (limitPrice <= 0) {
			rejectCancelReplace(BinaryUtils.copy(temp, "Invalid Price<44>: ").put(price.getValue()),
                    FixConstants.CxlRejectReason.BrokerOption, true, order.getID());
			return;
		}

		if(securities.getBond(symbol.getValue())==null){
			if(securities.getMultiLegSecurityInstrument(symbol.getValue())!=null){
				log.error(log.log().add("Cancel Rpl Failed.MultiLeg Instrument Not supported"));

			}
			rejectCancelReplace(BinaryUtils.copy(temp, "Invalid Symbol<55>: ").put(symbol.getValue()),
                    FixConstants.CxlRejectReason.BrokerOption, true, order.getID());
			return;
		}

		Bond security= securities.getBond(symbol.getValue());

		if (security.getID() != order.getSecurityID()) {
			rejectCancelReplace(BinaryUtils.copy(temp, "Cannot Replace Symbol<55>"), FixConstants.CxlRejectReason.BrokerOption, true,
                    order.getID());
			return;
		}

		if (order.getQty() == qty && order.getPrice() == limitPrice) {
			rejectCancelReplace(BinaryUtils.copy(temp, "OrderQty<38> or Price<44> or MaxFloor<111> Must Change"), FixConstants.CxlRejectReason.BrokerOption, true, order.getID());
			return;
		}

		double unitDV01 = MatchBondMath.getSignedDV01(security, limitPrice, 1,buy);
		double additionalDV01 = (qty - order.getQty()) * unitDV01;
        Trader trader = this.traderService.get(order.getTraderID());

		if (RiskService.violatesFatFingerQuantityLimit(trader, qty,security)) {
			BinaryUtils.copy(temp, "Violation of Fat Finger risk check: ");
			BinaryUtils.copy(temp, accountName);
			rejectCancelReplace(temp, FixConstants.CxlRejectReason.RiskChecks, true, order.getID());
			return;
		}

		// check DV01 limit
		// based on current DV01 + new order's DV01

		if( riskService.violatesDV01Limit(this.accountObj, additionalDV01) ){
			BinaryUtils.copy( temp, "Account Net DV01 violation: " );
			BinaryUtils.copy(temp, accountName);
			rejectCancelReplace(temp, FixConstants.CxlRejectReason.RiskChecks, true, order.getID());
			return;
		}

		MatchReplaceCommand replace = messages.getMatchReplaceCommand();
		replace.setOrderID(order.getID());
		replace.setClOrdID(clOrdId.getValue());
		replace.setOrigClOrdID(origClOrdId.getValue());
		replace.setQty(qty);
		replace.setPrice(limitPrice);
		send(replace);
	}

	private boolean checkRequiredOrderFields() {
		temp.clear();
		for (FixTag requiredOrderTag : requiredOrderTags) {
			if (!requiredOrderTag.isPresent()) {
				BinaryUtils.copy(temp, "Req Tag Missing: ");
				BinaryUtils.copy(temp, FixTags.getTagName(requiredOrderTag.getID()));
				temp.put((byte) '<');
				TextUtils.writeNumber(temp, requiredOrderTag.getID());
				temp.put((byte) '>');
				rejectOrder(temp);
				return false;
			}
		}
		return true;
	}

	@Override
	public void onClientCancelReplaceReject(FixOrder order, MatchClientCancelReplaceRejectEvent msg) {
		cancelReplaceRejectsSent.inc();
		stateMachine.incInboundSeqNo();
        fixOutput.writeClientCancelReplaceReject(order, msg);
	}

	@Override
	public void onCancelReplaceReject(FixOrder order, MatchCancelReplaceRejectEvent msg) {
		cancelReplaceRejectsSent.inc();
		stateMachine.incInboundSeqNo();
        fixOutput.writeCancelReplaceReject(order, msg);
	}

	@Override
	public void onClientOrderReject(MatchClientOrderRejectEvent msg) {
		rejectsSent.inc();
		stateMachine.incInboundSeqNo();
        fixOutput.writeClientOrderReject(msg, connector.getCurrentSeq());
	}

    @Override
    public void onOrderReject(MatchOrderRejectEvent msg) {
        rejectsSent.inc();
        stateMachine.incInboundSeqNo();
        fixOutput.writeOrderReject(msg, connector.getCurrentSeq());
    }

	@Override
	public boolean isInterested(MatchOrderEvent msg) {
		return msg.getContributorID() == getContribID();
	}

	@Override
	public void onOrder(FixOrder order, MatchOrderEvent msg) {
		ordersSent.inc();
		stateMachine.incInboundSeqNo();

		order.setIOC(msg.getIOC());
        fixOutput.writeAcceptedExecutionReport(msg.getTimestamp(), order, connector.getCurrentSeq());
	}

	@Override
	public void onCancel(FixOrder order, MatchCancelEvent msg) {
		cancelsSent.inc();
		// protect against unsolicited cancels
		if (msg.getContributorID() == getContribID() && !order.isTriedDisconnect()) {
			stateMachine.incInboundSeqNo();
		}
        fixOutput.writeCanceledExecutionReport(msg.getTimestamp(), order, connector.getCurrentSeq(), msg.getOrigClOrdID());
	}

	@Override
	public void onReplace(FixOrder order, MatchReplaceEvent msg, ReplaceUpdates updates) {
		replacesSent.inc();
		stateMachine.incInboundSeqNo();
        fixOutput.writeReplacedExecutionReport(msg.getTimestamp(), order, connector.getCurrentSeq(), msg.getOrigClOrdID());
	}

	@Override
	public void onFill(FixOrder order, MatchFillEvent msg) {
		fillsSent.inc();
        fixOutput.writeFilledExecutionReport(msg.getTimestamp(), order, connector.getCurrentSeq(), msg.getQty(), msg.getPrice());
	}

	private boolean checkClOrdID(boolean checkOrig) {
		if (clOrdId.getValue().remaining() > MatchConstants.CLORDID_LENGTH) {
			BinaryUtils.copy(temp, "ClOrdID<11> length: ").put(clOrdId.getValue()).flip();
			stateMachine.reject(temp, FixTags.ClOrdID, FixConstants.SessionRejectReason.IncorrectDataFormatForValue);
			return false;
		}

		if (checkOrig && origClOrdId.getValue().remaining() > MatchConstants.CLORDID_LENGTH) {
			BinaryUtils.copy(temp, "OrigClOrdID<41> length: ").put(origClOrdId.getValue()).flip();
			stateMachine.reject(temp, FixTags.ClOrdID, FixConstants.SessionRejectReason.IncorrectDataFormatForValue);
			return false;
		}

		return true;
	}

	private void rejectOrder(ByteBuffer msg) {
		msg.flip();

		MatchClientOrderRejectCommand reject = messages.getMatchClientOrderRejectCommand();
		reject.setTrader(account.getValue());
		reject.setBuy(side.getValueAsChar() == FixConstants.Side.Buy);
		reject.setSecurity(symbol.getValue());
		reject.setClOrdID(clOrdId.getValue());
		reject.setText(msg);
		send(reject);
	}

	private boolean checkRequiredCancelReplaceFields(FixTag[] fixTags, boolean replace) {
		temp.clear();
		for (FixTag requiredOrderTag : fixTags) {
			if (!requiredOrderTag.isPresent()) {
				BinaryUtils.copy(temp, "Req Tag Missing: ");
				BinaryUtils.copy(temp, FixTags.getTagName(requiredOrderTag.getID()));
				temp.put((byte) '<');
				TextUtils.writeNumber(temp, requiredOrderTag.getID());
				temp.put((byte) '>');
				rejectCancelReplace(temp, FixConstants.CxlRejectReason.BrokerOption, replace, 0);
				return false;
			}
		}
		return true;
	}

	private void rejectCancelReplace(ByteBuffer msg, char reason, boolean isReplace, int orderId) {
		msg.flip();

		MatchClientCancelReplaceRejectCommand reject = messages.getMatchClientCancelReplaceRejectCommand();
		reject.setOrderID(orderId);
		reject.setClOrdID(clOrdId.getValue());
		reject.setOrigClOrdID(origClOrdId.getValue());
		reject.setText(msg);
		reject.setReason(reason);
		reject.setIsReplace(isReplace);
		send(reject);
	}

	@Override
	public void onOpen(long timestamp)
	{
		this.marketOpen = true;
	}

	@Override
	public void onClose(long timestamp)
	{
		this.marketOpen = false;
	}

	@Override
	public void onConnect()
	{
		this.isConnected = true;
		if( this.contributor != null && this.contributor.isCancelOnDisconnect() )
		{
			Iterator<FixOrder> iterator = this.orderService.getOrders().iterator();
			while( iterator.hasNext() )
			{
				FixOrder order = iterator.next();
				order.setTriedDisconnect(false);
			}
		}
	}
	
	@Override
	public void onAllCommandsCleared()
	{
		cancelOrdersIfDisconnected();
	}

	@Override
	public void onDisconnect()
	{
		this.isConnected = false;
		cancelOrdersIfDisconnected();
	}

	private void cancelOrdersIfDisconnected()
	{
		if ( this.contributor != null && this.contributor.isCancelOnDisconnect() && !isConnected ) {
			Iterator<FixOrder> iterator = this.orderService.getOrders().iterator();
			while( iterator.hasNext() )
			{
				FixOrder order = iterator.next();
				if(!order.isTriedDisconnect()) {
					MatchCancelCommand cmd = messages.getMatchCancelCommand(); 
					cmd.setOrderID(order.getID());
					cmd.setOrigClOrdID(order.getClOrdID());
					cmd.setClOrdID(order.getClOrdID());
					send(cmd);
					order.setTriedDisconnect(true);
					break;
				}
			}
		}
	}

	private int externalToInternalQty(int qty) {
		if (qtyMode == FIXQtyMode.Notional) {
			qty /= MatchConstants.QTY_MULTIPLIER;
		}
		else if (qtyMode == FIXQtyMode.RoundLot) {
			qty *= (1000000 / MatchConstants.QTY_MULTIPLIER);
		}
		return qty;
	}
}
