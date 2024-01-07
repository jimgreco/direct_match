package com.core.match.fix.stp;


import com.core.app.AppConstructor;
import com.core.app.CommandException;
import com.core.app.Exposed;
import com.core.app.Param;
import com.core.app.heartbeats.HeartBeatFieldIDEnum;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.app.heartbeats.HeartbeatStringField;
import com.core.connector.Dispatcher;
import com.core.fix.*;
import com.core.fix.connector.FixConnector;
import com.core.fix.msgs.FixConstants;
import com.core.fix.msgs.FixDispatcher;
import com.core.fix.store.FixFileStore;
import com.core.fix.store.FixStore;
import com.core.fix.tags.FixTag;
import com.core.fix.util.FixPrinter;
import com.core.match.MatchApplication;
import com.core.match.MatchCommandSender;
import com.core.match.STPHolder;
import com.core.match.STPHolderFactory;
import com.core.match.fix.FixStateMachine;
import com.core.match.msgs.*;
import com.core.match.ouch.controller.SpreadPriceProvider;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.book.MatchBBOBookService;
import com.core.match.services.order.OrderServiceListener;
import com.core.match.services.order.ReplaceUpdates;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.MultiLegSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.match.util.MatchBondMath;
import com.core.match.util.MatchPriceUtils;
import com.core.match.util.MessageUtils;
import com.core.util.*;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerService;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.core.fix.msgs.FixTags.*;

/**
 * User: jgreco
 */
public class FixSTP extends MatchApplication implements
		OrderServiceListener<FIXSTPOrder> {
	private static final int CONCURRENT_ORDERS = 10000;

	protected final ByteBuffer temp = ByteBuffer.allocate(128);

	protected final SecurityService<BaseSecurity> securities;
	protected final TraderService<Trader> traders;

	private final FixConnector fixConnector;
	private final FixStateMachine stateMachine;
	protected final AccountService<Account> accounts;
	protected final FixStore store;
	private final FIXPortInfo info;
	private final STPHolderFactory<FIXSTPOrder> stpHolders = new STPHolderFactory<>();
	private final String accountNames;

	protected final TradeDateUtils tradeDateUtils = new TradeDateUtils(MessageUtils.zoneID(), MatchConstants.SESSION_ROLLOVER_TIME);
    protected final FIXSTPOrderService orders;
    private HeartbeatStringField accountHB;
	private SpreadPriceProvider spreadPriceProvider;

	protected final FixParser parser;
	protected final FixTag account;
	protected final FixTag side;
	protected final FixTag symbol;
	protected final FixTag clOrdId;
	protected final FixTag orderId;
	protected final FixTag execId;
	protected final FixTag lastPx;
	protected final FixTag lastQty;
	protected final FixTag transactTime;
	protected final FixTag noSides;
	protected final FixTag commission;
	protected final FixTag commissionType;
	protected final FixTag tradeDate;
	protected final FixTag settlementDate;
	protected final FixTag previouslyReported;
	protected final FixTag tradeReportID;
	protected final FixTag securityID;
	protected final FixTag securityIDSource;
	protected final FixTag netMoney;

	@AppConstructor
	public FixSTP(Log log,
			TCPSocketFactory socketFactory,
			FileFactory fileFactory,
			TimerService timeFactory,
			TimeSource timeSource,
			TraderService<Trader> traders,
			SecurityService<BaseSecurity> securityService,
            AccountService<Account> accountService,
			Dispatcher dispatcher,
			MatchCommandSender sender,
			MatchMessages messages, MatchBBOBookService bboBookService,
            FixServerTcpConnectorFactory fixConnectorFactory,
			@Param(name = "Port") int port,
			@Param(name = "Version") int version,
			@Param(name = "SenderCompId") String senderCompID,
			@Param(name = "TargetCompId") String targetCompID,
			@Param(name = "Accounts") String accounts) throws IOException
	{
		this(log,
				timeFactory,
				dispatcher,
				sender,
				messages,
				traders,
				securityService,
                accountService,
				fixConnectorFactory.create(
						log,
						socketFactory,
						sender,
						port),
				new InlineFixParser(),
				new InlineFixWriter(
						timeSource,
						version,
						targetCompID,
						senderCompID),
				new FixFileStore(
						fileFactory,
						log,
						sender.getName()),
				new FixDispatcher(
						log),
				new FIXPortInfo(
						port,
						version,
						senderCompID,
						targetCompID),
				accounts,
				bboBookService);
	}

	public FixSTP(Log log,
                  TimerService timerService,
                  Dispatcher dispatcher,
                  MatchCommandSender sender,
                  MatchMessages messages,
                  TraderService<Trader> traders,
                  SecurityService<BaseSecurity> securityService,
                  AccountService<Account> accountService,
                  FixConnector connector,
                  FixParser parser,
                  FixWriter writer,
                  FixStore store,
                  FixDispatcher fixDispatcher,
                  FIXPortInfo fixPortInfo,
                  String accountNames, MatchBBOBookService bboBookService)
	{
		super(log, sender);

		this.parser = parser;
		this.account = parser.createWriteOnlyFIXTag(Account);
		this.side = parser.createWriteOnlyFIXTag(Side);
		this.symbol = parser.createWriteOnlyFIXTag(Symbol);
		this.clOrdId = parser.createWriteOnlyFIXTag(ClOrdID);
		this.orderId = parser.createWriteOnlyFIXTag(OrderID);
		this.execId = parser.createWriteOnlyFIXTag(ExecID);
		this.lastPx = parser.createWriteOnlyFIXTag(LastPx);
		this.lastQty = parser.createWriteOnlyFIXTag(LastShares);
		this.transactTime = parser.createWriteOnlyFIXTag(TransactTime);
		this.noSides = parser.createWriteOnlyFIXTag(NoSides);
		this.commission = parser.createWriteOnlyFIXTag(Commission);
		this.commissionType = parser.createWriteOnlyFIXTag(CommissionType);
		this.settlementDate = parser.createWriteOnlyFIXTag(SettlementDate);
		this.tradeDate = parser.createWriteOnlyFIXTag(TradeDate);
		this.previouslyReported = parser.createWriteOnlyFIXTag(PreviouslyReported);
		this.tradeReportID = parser.createWriteOnlyFIXTag(TradeReportID);
		this.securityID = parser.createWriteOnlyFIXTag(SecurityID);
		this.securityIDSource = parser.createWriteOnlyFIXTag(SecurityIDSource);
		this.netMoney = parser.createWriteOnlyFIXTag(NetMoney);

		this.info = fixPortInfo;

		this.traders = traders;
		this.securities = securityService;
        this.accounts = accountService;

		this.stateMachine = new FixStateMachine(
				log,
				fixDispatcher,
				timerService,
				sender,
				parser,
				store,
				connector,
				messages,
				false,
				false,
				info.getMinorVersion(),
				info.getSenderCompID(),
				info.getTargetCompID());

		this.fixConnector = connector;
		this.fixConnector.init(parser, stateMachine);
		this.fixConnector.addListener(new FixPrinter(
				log));

		this.store = store;
		this.store.init(writer, fixConnector);
		this.accountNames = accountNames;

		spreadPriceProvider =new SpreadPriceProvider(securities,bboBookService,dispatcher,log);

		orders = new FIXSTPOrderService(
				log,
				dispatcher,
				CONCURRENT_ORDERS,
                traders,
                accounts,
                accountNames.split(","));

		sender.addContributorDefinedListener(stateMachine);

		orders.addListener(this);

		fixDispatcher.subscribe(this);

		dispatcher.subscribe(this);
		dispatcher.subscribe(stateMachine);
	}

	@Exposed(name = "setInbound")
	public void setNextInboundSeqNum(@Param(name = "SeqNum") int seqNum)
	{
		stateMachine.setNextInboundSeqNum(seqNum);
	}

	@Exposed(name = "setOutbound")
	public void setNextOutboundSeqNum(@Param(name = "SeqNum") int seqNum)
	{
		stateMachine.setNextOutboundSeqNo(seqNum, false);
	}

	@Override
	protected void onActive()
	{
		try
		{
			fixConnector.open();
		}
		catch (IOException e)
		{
			throw new CommandException(
					"Could not open FIX connector");
		}
	}

	@Override
	protected void onPassive()
	{
		try
		{
			this.fixConnector.close();
		}
		catch (IOException e)
		{
			throw new CommandException(
					"Error closing server socket");
		}
	}

	@Override
	public void onAddHeartbeatFields(HeartbeatFieldRegister register)
	{
		accountHB = register.addStringField("FIX", HeartBeatFieldIDEnum.Acct);

		if (info != null)
		{
			info.addStatus(register);
		}
		stateMachine.addStatus(register);
	}

	@Override
	public void onUpdateHeartbeatFields(HeartbeatFieldUpdater register)
	{
		stateMachine.updateStatus();
		accountHB.set(accountNames);
	}

	@Override
	public boolean isInterested(MatchOrderEvent msg)
	{
        // this is done elsewhere
		return false;
	}

	@Override
	public void onOrder(FIXSTPOrder order, MatchOrderEvent msg) {

	}

	@Override
	public void onCancel(FIXSTPOrder order, MatchCancelEvent msg) {

	}

	@Override
	public void onReplace(FIXSTPOrder order, MatchReplaceEvent msg, ReplaceUpdates updates) {

	}

	@Override
	public void onFill(FIXSTPOrder order, MatchFillEvent msg) {
		Trader trader = traders.get(order.getTraderID());
		Account account = accounts.get(trader.getAccountID());

		STPHolder<FIXSTPOrder> holder = stpHolders.addFill(order, order.getClOrdID(), msg);
		boolean passiveFill = msg.getPassive();
		boolean lastFill = msg.getLastFill();
		if (( lastFill && !passiveFill) || passiveFill) {

			BaseSecurity security = this.securities.get(holder.getSecurityID());

			if(security.isBond()){
				writeSTP((Bond)security, msg.getTimestamp(), msg.getMatchID(), holder, holder.getAccumulatedQty(),
						holder.getAveragePrice(), account, holder.isBuy());
			}else if(security.isMultiLegInstrument()) {
				MultiLegSecurity spread = ((MultiLegSecurity)security);

				double leg1Price= PriceUtils.toDouble(spreadPriceProvider.getPrice(spread.getLeg1(),holder.isBuy()),
						MatchConstants.IMPLIED_DECIMALS);
				double leg2Price=PriceUtils.toDouble(spreadPriceProvider.getPrice(spread.getLeg2(),holder.isBuy()),
						MatchConstants.IMPLIED_DECIMALS);

				if (security.isSpread()) {
					double leg1Qty = holder.getAccumulatedQty();
					double leg2Qty = leg1Qty * (spread.getLeg2Size() / spread.getLeg1Size());

					writeSTP(spread.getLeg1(), msg.getTimestamp(), msg.getMatchID(), holder, leg1Qty, leg1Price, account, holder.isBuy());
					writeSTP(spread.getLeg2(), msg.getTimestamp(), msg.getMatchID(), holder, leg2Qty, leg2Price, account, !holder.isBuy());
				}else if(security.isButterfly()){
					double leg3Price=PriceUtils.toDouble(spreadPriceProvider.getPrice(spread.getLeg3(),holder.isBuy()),
							MatchConstants.IMPLIED_DECIMALS);

					double leg2Qty = holder.getAccumulatedQty();
					double leg1Qty = leg2Qty * (spread.getLeg1Size() / spread.getLeg2Size());
					double leg3Qty = leg1Qty * (spread.getLeg3Size() / spread.getLeg1Size());

					writeSTP(spread.getLeg1(), msg.getTimestamp(), msg.getMatchID(), holder, leg1Qty, leg1Price, account, !holder.isBuy());
					writeSTP(spread.getLeg2(), msg.getTimestamp(), msg.getMatchID(), holder, leg2Qty, leg2Price, account, holder.isBuy());
					writeSTP(spread.getLeg3(), msg.getTimestamp(), msg.getMatchID(), holder, leg3Qty, leg3Price, account, !holder.isBuy());
				}
			}

			holder.clear();
		}
	}

	protected void writeSTP(Bond security, long time, int matchID, STPHolder<FIXSTPOrder> holder, double qty, double price, Account account, boolean isBuy) {

		Trader trader = this.traders.get(holder.getTraderID());

		FixWriter fix = store.createMessage("AE");
		fix.writeChar(noSides, FixConstants.NoSides.OneSide);
		fix.writeChar(side, isBuy ? FixConstants.Side.Buy : FixConstants.Side.Sell);
		fix.writeNumber(orderId, holder.getOrderID());
		fix.writeString(clOrdId, holder.getClOrdId());
		fix.writeString(this.account, trader.getName());

		long commissionValue = (long)Math.ceil(MatchPriceUtils.toQtyRoundLot((int)qty) * account.getCommission());
		fix.writePrice(commission, commissionValue, MatchConstants.IMPLIED_DECIMALS) ;
		fix.writeChar(commissionType, FixConstants.CommissionType.Absolute);

		temp.clear();
		temp.put((byte) 'X');
		TextUtils.writeNumber(temp, matchID).flip();
		fix.writeString(execId, temp);

		long avgPx = (long) (price * MatchPriceUtils.getPriceMultiplier());

		fix.writePrice(lastPx, avgPx, MatchConstants.IMPLIED_DECIMALS);
		fix.writeNumber(lastQty,  internalToExternalQty(qty));

		double netMoneyDbl = MatchBondMath.getNetMoney(security, isBuy, avgPx, (int)qty, account.getCommission());
		long netMoneyRounded = Math.round(netMoneyDbl * 100);
		fix.writePrice(netMoney, netMoneyRounded, 2);

		temp.clear();
		BinaryUtils.copy(temp, security.getName()).flip();
		fix.writeString(symbol, temp);

        fix.writeChar(securityIDSource, FixConstants.SecurityIDSource.CUSIP);

        temp.clear();
        BinaryUtils.copy(temp, security.getCUSIP()).flip();
        fix.writeString(securityID, temp);

		fix.writeDateTime(transactTime, time);

		int settlementDateNum = TimeUtils.toDateInt(security.getSettlementDate());
		fix.writeNumber(settlementDate, settlementDateNum);

		int tradeDateNum = TimeUtils.toDateInt(tradeDateUtils.getTradeDate(time));
		fix.writeNumber(tradeDate, tradeDateNum);

		fix.writeChar(previouslyReported, FixConstants.PreviouslyReported.No);
		fix.writeNumber(tradeReportID, matchID);

		store.finalizeBusinessMessage();
	}

	protected int internalToExternalQty(double qty) {
		return (int)(qty * MatchConstants.QTY_MULTIPLIER);
	}
}
