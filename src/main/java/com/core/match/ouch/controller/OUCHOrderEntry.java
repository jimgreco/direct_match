package com.core.match.ouch.controller;

import static com.core.match.msgs.MatchConstants.QTY_MULTIPLIER;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import com.core.app.AppConstructor;
import com.core.app.CommandException;
import com.core.app.Param;
import com.core.app.heartbeats.HeartBeatFieldIDEnum;
import com.core.app.heartbeats.HeartbeatBooleanField;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.app.heartbeats.HeartbeatNumberField;
import com.core.app.heartbeats.HeartbeatStringField;
import com.core.connector.AllCommandsClearedListener;
import com.core.connector.Connector;
import com.core.connector.Dispatcher;
import com.core.connector.soup.SoupConnectionListener;
import com.core.match.MatchApplication;
import com.core.match.MatchCommandSender;
import com.core.match.STPHolder;
import com.core.match.STPHolderFactory;
import com.core.match.msgs.MatchCancelCommand;
import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchCancelReplaceRejectEvent;
import com.core.match.msgs.MatchClientCancelReplaceRejectCommand;
import com.core.match.msgs.MatchClientCancelReplaceRejectEvent;
import com.core.match.msgs.MatchClientOrderRejectCommand;
import com.core.match.msgs.MatchClientOrderRejectEvent;
import com.core.match.msgs.MatchCommonCommand;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchMessages;
import com.core.match.msgs.MatchOrderCommand;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchOrderRejectEvent;
import com.core.match.msgs.MatchReplaceCommand;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.match.msgs.MatchSecurityEvent;
import com.core.match.ouch.OUCHOrder;
import com.core.match.ouch.OUCHRejectReasonMap;
import com.core.match.ouch.msgs.OUCHAcceptedCommand;
import com.core.match.ouch.msgs.OUCHCancelEvent;
import com.core.match.ouch.msgs.OUCHCancelListener;
import com.core.match.ouch.msgs.OUCHCancelRejectedCommand;
import com.core.match.ouch.msgs.OUCHCanceledCommand;
import com.core.match.ouch.msgs.OUCHConstants;
import com.core.match.ouch.msgs.OUCHFillCommand;
import com.core.match.ouch.msgs.OUCHOrderEvent;
import com.core.match.ouch.msgs.OUCHOrderListener;
import com.core.match.ouch.msgs.OUCHRejectedCommand;
import com.core.match.ouch.msgs.OUCHReplaceEvent;
import com.core.match.ouch.msgs.OUCHReplaceListener;
import com.core.match.ouch.msgs.OUCHReplacedCommand;
import com.core.match.ouch2.controller.OUCHOrdersRepository;
import com.core.match.ouch2.factories.OUCHFactory;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.book.MatchBBOBookService;
import com.core.match.services.contributor.Contributor;
import com.core.match.services.contributor.ContributorService;
import com.core.match.services.events.SystemEventService;
import com.core.match.services.order.OrderServiceListener;
import com.core.match.services.order.OrderServiceRejectListener;
import com.core.match.services.order.OrderServiceWithRejectsContribIDFiltered;
import com.core.match.services.order.ReplaceUpdates;
import com.core.match.services.risk.RiskService;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.MultiLegSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.security.SecurityServiceListener;
import com.core.match.services.security.SecurityType;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.util.BinaryUtils;
import com.core.util.ByteStringBuffer;
import com.core.util.TimeUtils;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimerService;
import com.gs.collections.impl.map.mutable.UnifiedMap;

/**
 * Created by jgreco on 6/29/15.
 */
public class OUCHOrderEntry extends
        MatchApplication implements
        OrderServiceListener<OUCHOrder>,
        OrderServiceRejectListener<OUCHOrder>,
        OUCHOrderListener,
        OUCHCancelListener,
        OUCHReplaceListener,
        AllCommandsClearedListener,
        SecurityServiceListener<BaseSecurity>,
        SoupConnectionListener
{
    private static final int MAX_ALLOWED_DROPPED_ORDER_LIMIT = 5;
    private final SystemEventService systemEventService;
    protected final OUCHAdapter ouchAdapter;
    protected final AccountService<Account> accounts;
    protected final TraderService<Trader> traders;
    private final SecurityService<BaseSecurity> securities;
    private final UnifiedMap<ByteBuffer, BaseSecurity> securitiesByLongName = new UnifiedMap<>();
    protected final MatchMessages coreMessages;

    private final OrderServiceWithRejectsContribIDFiltered<OUCHOrder> orders;
    protected final STPHolderFactory<OUCHOrder> stpHolders = new STPHolderFactory<>();

    private final ByteStringBuffer stringBuffer = new ByteStringBuffer();
    protected final ByteBuffer temp = ByteBuffer.allocate(1024);

    private final String fixedAccount;
    protected final UnifiedMap<ByteBuffer, Trader> fixedTraders = new UnifiedMap<>();
    private final OUCHFactory ouchComponentFactory;
    private final RiskValidator riskValidator;
    private final OUCHOrdersRepository orderRepository;
    private final SpreadPriceProvider spreadPriceProvider;
    private Account account;
    private Contributor contributor;
    protected int todayTradeDate = 0;
    private boolean connected;
    protected final RiskService<OUCHOrder> riskService;

    private HeartbeatNumberField ordersSent;
    private HeartbeatNumberField cancelsSent;
    private HeartbeatNumberField replacesSent;
    private HeartbeatNumberField rejectsSent;
    private HeartbeatNumberField cancelReplaceRejectsSent;
    protected HeartbeatNumberField fillsSent;
    private HeartbeatNumberField liveOrders;
    private HeartbeatNumberField accountIDMonitor;
    private HeartbeatStringField accountHeartbeatStringField;
    private HeartbeatBooleanField loggedInHeartbeatField;
    private HeartbeatBooleanField connectedHeartbeatField;
    private final TradeConfirmProcessor tradeConfirmProcessor;
    private final QuantityValidator quantityValidator;

    private boolean ouchAdaptorOpened;
    private int numberMsgDropped;


    @AppConstructor
    public OUCHOrderEntry(Log log,
                          TCPSocketFactory tcpFactory,
                          FileFactory fileFactory,
                          TimerService timers,
                          MatchCommandSender sender,
                          Dispatcher dispatcher,
                          MatchMessages coreMessages,
                          AccountService<Account> accounts,
                          TraderService<Trader> traders,
                          SecurityService<BaseSecurity> securities,
                          SystemEventService systemEventService,
                          ContributorService<Contributor> contributorService,
                          MatchBBOBookService bboBookService,
                          Connector coreConnector,
                          OUCHFactory ouchComponentFactory,
                          @Param(name = "Name") String name,
                          @Param(name = "Port") int port,
                          @Param(name = "Username") String username,
                          @Param(name = "Password") String password,
                          @Param(name = "FixedAccount") String fixedAccount) throws IOException
    {


        super(log, sender);

        this.ouchComponentFactory = ouchComponentFactory;
        this.ouchAdapter= ouchComponentFactory.getOUCHAdaptor(name, log, fileFactory, tcpFactory, timers, port, username, password);
        Dispatcher ouchDispatcher = ouchAdapter.getOUCHDispatcher();
        this.accounts = accounts;
        this.traders = traders;

        this.securities = securities;
        this.fixedAccount = fixedAccount;
        this.numberMsgDropped=0;
        this.ouchAdaptorOpened =false;
        contributorService.addListener((contributor, msg, isNew) -> {
            this.contributor = contributor;

        });

        this.systemEventService = systemEventService;
        ouchDispatcher.subscribe(this);

        this.coreMessages = coreMessages;
        this.orders = new OrderServiceWithRejectsContribIDFiltered<>(
                OUCHOrder.class,
                log,
                dispatcher,
                10000);
        riskService = new RiskService<>(
                this.accounts,
                this.traders,
                this.securities,this.log);
        this.orders.addListener(riskService);
        this.accounts.addListener(riskService);
        addAccountAndTrader(fixedAccount);

        sender.addContributorDefinedListener(this.orders);
        this.addAllCommandsClearedListener(this);
        this.securities.addListener(this);
        this.orders.addListener(this);
        this.orders.addRejectListener(this);

        coreConnector.addSessionSourceListener(ouchAdapter::setSession);
        ouchAdapter.addConnectionListener(this);
        this.orderRepository = ouchComponentFactory.getOUCHRepository();

        spreadPriceProvider =new SpreadPriceProvider(securities,bboBookService,dispatcher,log);
        connected=false;

        tradeConfirmProcessor=new OuchTradeConfirmProcessor(todayTradeDate,ouchAdapter,spreadPriceProvider,traders);
        riskValidator=new OrderRiskValidator(log,riskService);
        quantityValidator=new OrderQuantityValidator(log);

    }


    private void addAccountAndTrader(String fixedAccount) {
        if (fixedAccount != null && fixedAccount.length() > 0)
        {
            this.accounts.addListener((account, msg, isNew) -> {

                if (this.fixedAccount.equals(account.getName()))
                {
                    // KEEP IN MIND THAT BECAUSE BEFOREACTIVE CHECKS ACCOUNT FOR NON-NULL, TODAYTRADEDATE IS ASSUMED
                    // NON-ZERO IN "POST-SET-ACTIVE" PARTS OF HTE CODE
                    this.account = account;
                    todayTradeDate = TimeUtils.toDateInt(msg.getTimestampAsTime().toLocalDate());
                    accountHeartbeatStringField.set(this.account.getName());
                    accountIDMonitor.set(this.account.getID());
                }
            });
        }

        this.traders.addListener((tempTrader, msg, isNew) -> {
            ByteStringBuffer temp = new ByteStringBuffer();
            temp.clear();
            temp.add(tempTrader.getName());
            ByteBuffer buffer = temp.getUnderlyingBuffer();
            if (this.fixedAccount.equals(this.accounts.get(tempTrader.getAccountID()).getName())) {
                this.fixedTraders.put(buffer, tempTrader);
            }
        });
    }

    @Override
    protected void beforeActive()
    {
        if (fixedTraders.size() == 0)
        {
            throw new CommandException(
                    "Trader not defined: " + fixedTraders);
        }
        if (account == null)
        {
            throw new CommandException(
                    "Account not defined: " + fixedAccount);
        }
    }

    //The whole logic is so that we dont try to open adaptor when the session is
    //already established when we try to toggle it from active to passive

    @Override
    protected void onActive()
    {
        try
        {
            if(!ouchAdaptorOpened){
                ouchAdapter.open();
                ouchAdaptorOpened =true;
            }

        }
        catch (IOException e)
        {
            throw new CommandException(
                    "Could not open OUCH adapter");
        }
    }
    private void resetDropPacketsCounter(){
        numberMsgDropped=0;
    }

    //TODO: When do we close the adaptr?- Ans: We only allow commandSender to go passive on passive state, the ouch adaptor will always be active in our hot-warm desgin
    @Override
    protected void onPassive()
    {
        try
        {
            resetDropPacketsCounter();
            if(!ouchAdaptorOpened){
                ouchAdapter.open();
                ouchAdaptorOpened =true;
            }		}
        catch (IOException e)
        {
            throw new CommandException(
                    "Could not close OUCH adapter");
        }
    }

    @Override
    public void onAddHeartbeatFields(HeartbeatFieldRegister fieldRegister)
    {
        accountIDMonitor = fieldRegister.addNumberField("Stats", HeartBeatFieldIDEnum.AcctID);
        accountHeartbeatStringField = fieldRegister.addStringField("Stats", HeartBeatFieldIDEnum.Acct);
        liveOrders = fieldRegister.addNumberField("Stats", HeartBeatFieldIDEnum.LiveOrders);
        ordersSent = fieldRegister.addNumberField("Stats", HeartBeatFieldIDEnum.SendOrders);
        cancelsSent = fieldRegister.addNumberField("Stats", HeartBeatFieldIDEnum.Cancels);
        replacesSent = fieldRegister.addNumberField("Stats", HeartBeatFieldIDEnum.Replaces);
        rejectsSent = fieldRegister.addNumberField("Stats", HeartBeatFieldIDEnum.Rejects);
        cancelReplaceRejectsSent = fieldRegister.addNumberField("Stats", HeartBeatFieldIDEnum.CnlRplRejects);
        fillsSent = fieldRegister.addNumberField("Stats", HeartBeatFieldIDEnum.Fills);
        loggedInHeartbeatField = fieldRegister.addBoolField("Stats", HeartBeatFieldIDEnum.LoggedIn);
        connectedHeartbeatField = fieldRegister.addBoolField("Stats", HeartBeatFieldIDEnum.Connected);

    }


    @Override
    public void onUpdateHeartbeatFields(HeartbeatFieldUpdater fieldUpdater)
    {
        accountHeartbeatStringField.set(this.fixedAccount);
        liveOrders.set(orderRepository.getNumberOfLiveOrders());
        loggedInHeartbeatField.set(ouchAdapter.isLoggedIn());
        connectedHeartbeatField.set(connected);
    }



    private void dropMessage(long clOrdId){
        numberMsgDropped++;
        ouchAdapter.sendSessionPassiveDebug(clOrdId);
        log.warn(log.log().add("Passive State. Incoming Order Dropped. ClOrId: ").add(clOrdId).add(" Total Message Received that is Dropped: ").add(numberMsgDropped));
        if(numberMsgDropped>=MAX_ALLOWED_DROPPED_ORDER_LIMIT){
            ouchAdapter.closeAllClients();
            ouchAdaptorOpened =true;
            resetDropPacketsCounter();
            log.warn(log.log().add("Reaching limit of accepted order dropped. ouch session closed "));

        }
    }



    @Override
    public void onOUCHOrder(OUCHOrderEvent msg)
    {
        long clOrdId = msg.getClOrdID();


        //This whole block accounts for dropping order when the application is passive
        if(isPassive()){
            dropMessage(clOrdId);
            return;
        }

        log.debug(log.log().add("Received ouch order at: ").add(System.nanoTime()));
        char side = msg.getSide();
        if (side != OUCHConstants.Side.Buy && side != OUCHConstants.Side.Sell)
        {
            rejectOrder(OUCHConstants.RejectReason.InvalidSide, clOrdId, null, false, null);
            return;
        }
        boolean isBuy = side == OUCHConstants.Side.Buy;


        BaseSecurity baseSecurity = securitiesByLongName.get(msg.getSecurity());
        if (baseSecurity == null )
        {
            rejectOrder(OUCHConstants.RejectReason.InvalidSecurity, clOrdId, null, isBuy, null);
            return;
        }

        if (!msg.hasTrader() )
        {
            rejectOrder(OUCHConstants.RejectReason.InvalidTrader, clOrdId, null, isBuy, null);
            return;
        }

        Trader storedTrader;
        if (this.fixedTraders.size() > 1)
        {
            storedTrader = this.fixedTraders.get(msg.getTrader());
            if (storedTrader == null)
            {
                log.error(log.log().add("Trader is null"));
                rejectOrder(OUCHConstants.RejectReason.InvalidTrader, clOrdId, baseSecurity, isBuy, null);
                return;
            }
        }
        else
        {
            storedTrader = this.fixedTraders.getFirst();
        }

        if (!systemEventService.isOpen())

        {
            log.debug(log.log().add("Rejected: system not open"));

            rejectOrder(OUCHConstants.RejectReason.TradingSystemClosed, clOrdId, baseSecurity, isBuy, storedTrader);
            return;
        }

        if (orderRepository.contains(clOrdId))
        {
            log.debug(log.log().add("Rejected: no clorid "));

            // duplicate
            rejectOrder(OUCHConstants.RejectReason.DuplicateClOrdID, clOrdId, baseSecurity, isBuy, storedTrader);
            return;
        }

        if(!quantityValidator.validateQuantity(baseSecurity,msg.getQty())){
            rejectOrder(OUCHConstants.RejectReason.InvalidQuantity, clOrdId, baseSecurity, isBuy, storedTrader);
            return;
        }

        int qty = msg.getQty() / QTY_MULTIPLIER;
        long limitPrice = msg.getPrice();
        if (baseSecurity.isBond() &&limitPrice <= 0 || limitPrice % baseSecurity.getTickSize() != 0)
        {
            log.debug(log.log().add("Rejected: Invalid price: ").add(limitPrice));
            rejectOrder(OUCHConstants.RejectReason.InvalidPrice, clOrdId, baseSecurity, isBuy, storedTrader);
            return;
        }

        if (!riskValidator.validateFatFingerRisk(baseSecurity,storedTrader,qty))
        {
            rejectOrder(OUCHConstants.RejectReason.RiskViolation, clOrdId, baseSecurity, isBuy, storedTrader);
            return;
        }


        if (!riskValidator.validateDV01Risk(baseSecurity,account,limitPrice,qty,isBuy))
        {
            rejectOrder(OUCHConstants.RejectReason.RiskViolation, clOrdId, baseSecurity, isBuy, storedTrader);
            return;
        }



        MatchOrderCommand cmd = coreMessages.getMatchOrderCommand();
        cmd.setSecurityID(baseSecurity.getID());
        cmd.setTraderID(storedTrader.getID());
        cmd.setQty(qty);
        cmd.setPrice(limitPrice);
        if(msg.hasTimeInForce()){

            char timeInForce=msg.getTimeInForce();
            log.debug(log.log().add("TIF is:").add(timeInForce));

            if(OUCHConstants.TimeInForce.IOC==timeInForce){
                cmd.setIOC(true);
            }else{
                log.debug(log.log().add("TIF is:").add(timeInForce).add(" Assumed to be DAY"));
            }
        }
        temp.clear();
        temp.putLong(msg.getClOrdID());
        temp.flip();
        cmd.setClOrdID(temp);
        cmd.setBuy(msg.getSide() == OUCHConstants.Side.Buy);
        send(cmd);
    }

    @Override
    public void onOUCHCancel(OUCHCancelEvent msg)
    {
        long clOrdId = msg.getClOrdID();

        if(isPassive()){
            dropMessage(clOrdId);
            return;
        }
        OUCHOrder order = orderRepository.getByClientOrderID(clOrdId);
        if (order == null)
        {
            rejectCancelOrReplace(OUCHConstants.RejectReason.UnknownClOrdID, clOrdId, 0, false, 0);
            return;
        }

        MatchCancelCommand cmd = coreMessages.getMatchCancelCommand();
        cmd.setClOrdID(wrapLong(clOrdId));
        cmd.setOrderID(order.getID());

        send(cmd);
    }

    @Override
    public void onOUCHReplace( OUCHReplaceEvent msg)
    {
        // incoming replace from the client
        long newClOrdID = msg.getNewClOrdID();
        long oldClOrdID = msg.getClOrdID();

        if(isPassive()) {
            dropMessage(newClOrdID);
            return;
        }

        OUCHOrder ouchOrder = orderRepository.getByClientOrderID(oldClOrdID);
        if (ouchOrder == null)
        {
            rejectCancelOrReplace(OUCHConstants.RejectReason.UnknownClOrdID, newClOrdID, 0, true, 0);
            return;
        }

        try{
            stringBuffer.clear();
            stringBuffer.add(this.traders.get(ouchOrder.getTraderID()).getName());
            Trader storedTrader = this.fixedTraders.get(stringBuffer.getUnderlyingBuffer());
            BaseSecurity security = this.securities.get(ouchOrder.getSecurityID());

            int newQty = msg.getNewQty() / QTY_MULTIPLIER;

            if(!quantityValidator.validateQuantity(security,msg.getNewQty())){
                rejectCancelOrReplace(OUCHConstants.RejectReason.InvalidQuantity, newClOrdID, ouchOrder.getClOrdID(), true, ouchOrder.getID());
                return;
            }
            if ( newQty <= ouchOrder.getCumQty())
            {
                rejectCancelOrReplace(OUCHConstants.RejectReason.InvalidQuantity, newClOrdID, ouchOrder.getClOrdID(), true, ouchOrder.getID());
                return;
            }

            long newLimitPrice = msg.getNewPrice();
            if (security.isBond() && newLimitPrice <= 0 || newLimitPrice % security.getTickSize() != 0)
            {
                rejectCancelOrReplace(OUCHConstants.RejectReason.InvalidPrice, newClOrdID, ouchOrder.getClOrdID(), true, ouchOrder.getID());
                return;
            }

            if(!riskValidator.validateFatFingerRisk(security,storedTrader,newQty)){
                rejectCancelOrReplace(OUCHConstants.RejectReason.RiskViolation, newClOrdID, ouchOrder.getClOrdID(), ouchOrder.isBuy(), ouchOrder.getID());

            }

            int qtyDifference = (newQty - ouchOrder.getQty()) ;

            if (!riskValidator.validateDV01Risk(security,this.account,newLimitPrice,qtyDifference,ouchOrder.isBuy()))
            {
                rejectCancelOrReplace(OUCHConstants.RejectReason.RiskViolation, newClOrdID, ouchOrder.getClOrdID(), ouchOrder.isBuy(), ouchOrder.getID());
                return;
            }

            MatchReplaceCommand cmd = coreMessages.getMatchReplaceCommand();
            cmd.setClOrdID(wrapLong(msg.getNewClOrdID()));
            cmd.setOrigClOrdID(wrapLong(msg.getClOrdID()));
            cmd.setQty(newQty);
            cmd.setPrice(msg.getNewPrice());
            cmd.setOrderID(ouchOrder.getID());
            send(cmd);
        } catch(IndexOutOfBoundsException e){
            log.error(log.log().add("Caught java.lang.IndexOutOfBoundsException, request not properly formatted. REJECTING ORDER").add(e.getMessage()));
            rejectCancelOrReplace(OUCHConstants.RejectReason.RiskViolation, newClOrdID, ouchOrder.getClOrdID(), ouchOrder.isBuy(), ouchOrder.getID());

        }
    }

    @Override
    public void onOrder(OUCHOrder order, MatchOrderEvent msg)
    {
        log.debug(log.log().add("TX Ouch Accpt: ").add(msg.getClOrdIDAsString()));
        log.debug(log.log().add("Received OUCH Accepted: ").add(System.nanoTime()));
        ordersSent.inc();
        orderRepository.add(order,msg);

        OUCHAcceptedCommand cmd = ouchAdapter.getOUCHAccepted();
        cmd.setClOrdID(order.getClOrdID());
        cmd.setSide(msg.getBuy() ? OUCHConstants.Side.Buy : OUCHConstants.Side.Sell);
        cmd.setQty(msg.getQty() * QTY_MULTIPLIER);
        cmd.setPrice(msg.getPrice());
        cmd.setTimeInForce(msg.getIOC() ? OUCHConstants.TimeInForce.IOC : OUCHConstants.TimeInForce.DAY);
        cmd.setTrader(traders.get(msg.getTraderID()).getName());
        cmd.setSecurity(securities.get(msg.getSecurityID()).getName());
        ouchAdapter.send(cmd);
    }

    @Override
    public void onCancel(OUCHOrder order, MatchCancelEvent msg) {
        log.debug(log.log().add("TX Ouch Cancel: ").add(msg.getClOrdIDAsString()));

        cancelsSent.inc();
        orderRepository.removeOrder(order);
        // outgoing order canceled to client
        OUCHCanceledCommand cmd = ouchAdapter.getOUCHCanceled();
        cmd.setClOrdID(order.getClOrdID());
        cmd.setReason(OUCHConstants.CanceledReason.UserRequest);
        ouchAdapter.send(cmd);
    }

    @Override
    public void onReplace(OUCHOrder order, MatchReplaceEvent msg, ReplaceUpdates updates)
    {
        long oldClOrdID = order.getClOrdID();
        replacesSent.inc();
        orderRepository.replaceAndRefresh(order,msg);
        OUCHReplacedCommand cmd = ouchAdapter.getOUCHReplaced();
        cmd.setClOrdID(order.getClOrdID());
        cmd.setOldClOrdId(oldClOrdID);
        cmd.setQty(msg.getQty() * QTY_MULTIPLIER);
        cmd.setPrice(msg.getPrice());
        ouchAdapter.send(cmd);
    }

    @Override
    public void onFill(OUCHOrder order, MatchFillEvent msg)
    {
        fillsSent.inc();
        if (order.isFilled()){
            orderRepository.removeOrder(order);
        }
        // outgoing order executed to client
        OUCHFillCommand cmd = ouchAdapter.getOUCHFillCommand();
        long clOrdId = order.getClOrdID();
        cmd.setClOrdID(clOrdId);
        cmd.setExecutionPrice(msg.getPrice());
        cmd.setExecutionQty(msg.getQty() * QTY_MULTIPLIER);
        int matchID = msg.getMatchID();
        cmd.setMatchID(matchID);
        ouchAdapter.send(cmd);
        if (!account.isNetting()) {
            STPHolder<OUCHOrder> holder = stpHolders.addFill(order, order.getClOrdID(), msg);
            boolean passiveFill = msg.getPassive();
            boolean lastFill = msg.getLastFill();
            if ((lastFill && !passiveFill) || passiveFill) {
                BaseSecurity security = this.securities.get(holder.getSecurityID());
                tradeConfirmProcessor.sendTradeConfirmation(msg.getTimestamp(),msg.getMatchID(),holder,security);
            }
        }
    }


    @Override
    public boolean isInterested(MatchOrderEvent msg)
    {
        return msg.getContributorID() == getContribID();
    }

    @Override
    public void onOrderReject(MatchOrderRejectEvent msg) {
        rejectsSent.inc();
        OUCHRejectedCommand cmd = ouchAdapter.getOUCHRejected();

        long clOrdId = msg.getClOrdID().getLong();
        orderRepository.removeOrder(clOrdId);
        cmd.setClOrdID(clOrdId);
        cmd.setReason(OUCHRejectReasonMap.matchToOUCH(msg.getReason()));
        ouchAdapter.send(cmd);
    }

    @Override
    public void onClientOrderReject(MatchClientOrderRejectEvent msg) {
        OUCHRejectedCommand cmd = ouchAdapter.getOUCHRejected();
        cmd.setReason(msg.getReason());
        cmd.setClOrdID(msg.getClOrdID().getLong());
        ouchAdapter.send(cmd);
    }

    @Override
    public void onCancelReplaceReject(OUCHOrder order, MatchCancelReplaceRejectEvent msg)
    {
        cancelReplaceRejectsSent.inc();

        if (msg.getIsReplace())
        {
            OUCHRejectedCommand cmd = ouchAdapter.getOUCHRejected();
            cmd.setClOrdID(msg.getClOrdID().getLong());
            cmd.setReason(OUCHRejectReasonMap.matchToOUCH(msg.getReason()));
            ouchAdapter.send(cmd);
        }
        else
        {
            OUCHCancelRejectedCommand cmd = ouchAdapter.getOUCHCancelRejected();
            if (msg.hasClOrdID()){
                cmd.setClOrdID(msg.getClOrdID().getLong());
            }
            else if (order != null) {
                cmd.setClOrdID(order.getClOrdID());
            }
            cmd.setReason(OUCHRejectReasonMap.matchToOUCH(msg.getReason()));
            ouchAdapter.send(cmd);
        }

        // set triedDisconnect if not connected
    }

    @Override
    public void onClientCancelReplaceReject(OUCHOrder order, MatchClientCancelReplaceRejectEvent msg)
    {if (msg.getIsReplace())
    {
        OUCHRejectedCommand cmd = ouchAdapter.getOUCHRejected();
        cmd.setReason(msg.getReason());
        cmd.setClOrdID(msg.getClOrdID().getLong());
        ouchAdapter.send(cmd);
    }
    else
    {
        OUCHCancelRejectedCommand cmd = ouchAdapter.getOUCHCancelRejected();
        cmd.setReason(msg.getReason());
        cmd.setClOrdID(msg.getClOrdID().getLong());
        ouchAdapter.send(cmd);
        if (contributor.isCancelOnDisconnect() && !ouchAdapter.isConnected()&& (order != null) ){
                order.setTriedDisconnect(true);
            }
        }


    }

    protected void rejectCancelOrReplace(char c, long clOrdId, long origClOrdId, boolean isReplace, int orderID)
    {
        MatchClientCancelReplaceRejectCommand command = coreMessages.getMatchClientCancelReplaceRejectCommand();
        command.setOrderID(orderID);
        command.setReason(c);
        command.setIsReplace(isReplace);
        command.setClOrdID(wrapLong(clOrdId));
        command.setOrigClOrdID(wrapLong(origClOrdId));
        send(command);
    }

    protected void rejectOrder(char c, long clOrdId, BaseSecurity security, boolean isBuy, Trader trader)
    {
        MatchClientOrderRejectCommand command = coreMessages.getMatchClientOrderRejectCommand();
        command.setReason(c);
        command.setTrader(this.account.getName());
        command.setBuy(isBuy);
        command.setClOrdID(wrapLong(clOrdId));
        if (security != null) {
			command.setSecurity(security.getName());
		}
        if (trader != null && trader.getName() != null) {
			command.setTrader(trader.getName());
		}
        send(command);
    }

    @Override
    protected void send(MatchCommonCommand cmd)
    {
        this.ouchAdapter.enableRead(false);
        super.send(cmd);
    }


    @Override
    public void onAllCommandsCleared()
    {
        ouchAdapter.enableRead(true);
        cancelOrdersIfDisconnected();
    }

    protected ByteBuffer wrapLong(long aLong) {
        temp.clear();
        temp.putLong(aLong);
        temp.flip();
        return temp;
    }

    public int getNumberMsgDropped(){
        return numberMsgDropped;
    }

    public boolean isOuchAdaptorOpen(){
        return ouchAdaptorOpened;
    }

    public OrderServiceWithRejectsContribIDFiltered<OUCHOrder> getOrders()
    {
        return orders;
    }

    @Override
    public void onConnect()
    {
        connected=true;
        if (this.contributor.isCancelOnDisconnect())
        {
            Iterator<OUCHOrder> iterator = orders.getOrders().iterator();
            while (iterator.hasNext())
            {
                OUCHOrder next = iterator.next();
                next.setTriedDisconnect(false);
            }
        }
    }

    @Override
    public void onDisconnect()
    {	connected=false;

        log.error(log.log().add("Client disconnect.  Starting Cancel on Disconnect."));
        cancelOrdersIfDisconnected();
    }



    private void cancelOrdersIfDisconnected()
    {
        if (this.contributor.isCancelOnDisconnect() && !ouchAdapter.isConnected() && orders.size() > 0)
        {
            Iterator<OUCHOrder> iterator = orders.getOrders().iterator();
            while (iterator.hasNext())
            {
                OUCHOrder next = iterator.next();
                if (!next.hasTriedDisconnect())
                {
                    MatchCancelCommand cmd = coreMessages.getMatchCancelCommand();
                    temp.clear();
                    temp.putLong(next.getClOrdID());
                    temp.flip();
                    cmd.setClOrdID(temp);
                    cmd.setOrderID(next.getID());
                    send(cmd);
                    next.setTriedDisconnect(true);
                    break;
                }
            }
        }
    }


    @Override
    public void onBond(Bond security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
        stringBuffer.clear();
        stringBuffer.add(security.getName());
        ByteBuffer buffer = ByteBuffer.allocate(security.getName().length());
        BinaryUtils.copy(buffer, stringBuffer.getUnderlyingBuffer());
        buffer.flip();
        securitiesByLongName.put(buffer, security);
    }

    @Override
    public void onMultiLegSecurityInstrument(MultiLegSecurity security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
        log.debug(log.log().add("Adding Multileg security to map"));
        stringBuffer.clear();
        stringBuffer.add(security.getName());
        ByteBuffer buffer = ByteBuffer.allocate(security.getName().length());
        BinaryUtils.copy(buffer, stringBuffer.getUnderlyingBuffer());
        buffer.flip();
        securitiesByLongName.put(buffer, security);
    }
}
