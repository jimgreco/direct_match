package com.core.match.ouch2.controller;

import static com.core.match.msgs.MatchConstants.QTY_MULTIPLIER;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import com.core.app.AppConstructor;
import com.core.app.CommandException;
import com.core.app.Param;
import com.core.app.heartbeats.HeartBeatFieldIDEnum;
import com.core.app.heartbeats.HeartbeatBooleanField;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.app.heartbeats.HeartbeatNumberField;
import com.core.connector.AllCommandsClearedListener;
import com.core.connector.Connector;
import com.core.connector.Dispatcher;
import com.core.connector.soup.SoupConnectionListener;
import com.core.match.MatchApplication;
import com.core.match.MatchCommandSender;
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
import com.core.match.msgs.MatchSecurityEvent;
import com.core.match.ouch.OUCHOrder;
import com.core.match.ouch.OUCHRejectReasonMap;
import com.core.match.ouch.controller.OrderQuantityValidator;
import com.core.match.ouch.controller.OrderRiskValidator;
import com.core.match.ouch.msgs.OUCHConstants;
import com.core.match.ouch2.factories.OUCHFactory;
import com.core.match.ouch2.msgs.OUCH2AcceptedCommand;
import com.core.match.ouch2.msgs.OUCH2CancelEvent;
import com.core.match.ouch2.msgs.OUCH2CancelListener;
import com.core.match.ouch2.msgs.OUCH2CancelRejectedCommand;
import com.core.match.ouch2.msgs.OUCH2CanceledCommand;
import com.core.match.ouch2.msgs.OUCH2FillCommand;
import com.core.match.ouch2.msgs.OUCH2OrderEvent;
import com.core.match.ouch2.msgs.OUCH2OrderListener;
import com.core.match.ouch2.msgs.OUCH2RejectedCommand;
import com.core.match.ouch2.msgs.OUCH2ReplaceEvent;
import com.core.match.ouch2.msgs.OUCH2ReplaceListener;
import com.core.match.ouch2.msgs.OUCH2ReplacedCommand;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.contributor.Contributor;
import com.core.match.services.contributor.ContributorService;
import com.core.match.services.events.SystemEventListener;
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
import com.core.match.util.MessageUtils;
import com.core.util.BinaryUtils;
import com.core.util.ByteStringBuffer;
import com.core.util.HolidayCalendar;
import com.core.util.TimeUtils;
import com.core.util.TradeDateUtils;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimerService;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import com.gs.collections.impl.set.mutable.UnifiedSet;

/**
 * Created by liuli on 3/30/2016.
 */
public class OUCHMultiAccountOrderEntry extends
        MatchApplication implements
        OrderServiceListener<OUCHOrder>,
        OrderServiceRejectListener<OUCHOrder>,
        OUCH2OrderListener,
        OUCH2CancelListener,
        OUCH2ReplaceListener,
        AllCommandsClearedListener,
        SecurityServiceListener<BaseSecurity>,
        DisconnectCancelListener,
        SoupConnectionListener,
        SystemEventListener {

    private final TradeDateUtils tradeDateUtils = new TradeDateUtils(MessageUtils.zoneID(), MatchConstants.SESSION_ROLLOVER_TIME);
    private final HolidayCalendar calendar;
    private static final String ACCOUNT_WILDCARD = "*";
    private final SystemEventService systemEventService;
    private final AccountService<Account> accounts;
    private final TraderService<Trader> traders;
    private final SecurityService<BaseSecurity> securities;
    private final UnifiedMap<ByteBuffer, BaseSecurity> securitiesByLongName = new UnifiedMap<>();
    private final MatchMessages coreMessages;

    private final OrderServiceWithRejectsContribIDFiltered<OUCHOrder> orderService;

    private final ByteStringBuffer stringBuffer = new ByteStringBuffer();
    private final ByteBuffer temp = ByteBuffer.allocate(1024);
    private final ByteBuffer clientOrderIdBuffer = ByteBuffer.allocate(8);

    private final UnifiedMap<ByteBuffer, Trader> fixedTraders = new UnifiedMap<>();
    private final OUCH2Adaptor ouchAdapter;
    private final OrderRiskValidator riskValidator;
    private final OrderQuantityValidator quantityValidator;
    private  OUCHConnectionController ouchSoupConnectionController;
    private int todayTradeDate = 0;
    private final boolean connected;
    private final RiskService<OUCHOrder> riskService;

    private HeartbeatNumberField ordersSent;
    private HeartbeatNumberField cancelsSent;
    private HeartbeatNumberField replacesSent;
    private HeartbeatNumberField rejectsSent;
    private HeartbeatNumberField cancelReplaceRejectsSent;
    private HeartbeatNumberField fillsSent;
    private HeartbeatNumberField liveOrders;

    private HeartbeatBooleanField loggedInHeartbeatField;
    private HeartbeatBooleanField connectedHeartbeatField;

    private boolean ouchAdaptorOpened;


    private final UnifiedMap<Short, Account> fixedAccounts = new UnifiedMap<>();
    private final Set<Short> contribIDSet =new UnifiedSet<>();
    private final OUCHOrdersRepository ordersRepository;
    private final OUCHFactory adaptorFactory;
    private HeartbeatBooleanField activeHeartbeatField;


    @AppConstructor
    public OUCHMultiAccountOrderEntry(Log log,
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
                                      Connector coreConnector,
                                      OUCHFactory adaptorFactory,
                                      @Param(name = "Name") String name,
                                      @Param(name = "Port") int port,
                                      @Param(name = "Username") String username,
                                      @Param(name = "Password") String password,
                                      @Param(name = "Contributors") String contributorList,
                                      @Param(name = "Accounts") String accountList
                                        ) throws IOException {

        super(log,sender);

        this.calendar = new HolidayCalendar(log);

        Set<String >contribSet =  new UnifiedSet<String>(Arrays.asList(contributorList.split(",")));

        Set<String> acctSet= (accountList.equals(ACCOUNT_WILDCARD)? null:new UnifiedSet<String>(Arrays.asList(accountList.split(","))));
        this.ordersRepository= adaptorFactory.getOUCHRepository();
        this.accounts = accounts;
        this.traders = traders;
        this.adaptorFactory=adaptorFactory;
        this.ouchAdapter = adaptorFactory.getOUCH2Adaptor(
                name,
                log,
                fileFactory,
                tcpFactory,
                timers,
                port,
                username,
                password);
        this.securities = securities;
        this.ouchAdaptorOpened =false;
        ouchAdapter.addConnectionListener(this);

        Dispatcher ouchDispatcher = ouchAdapter.getOUCHDispatcher();

        this.systemEventService = systemEventService;
        systemEventService.addListener(this);
        ouchDispatcher.subscribe(this);

        this.coreMessages = coreMessages;
        this.orderService = new OrderServiceWithRejectsContribIDFiltered<>(
                OUCHOrder.class,
                log,
                dispatcher,
                10000);
        this.riskService = new RiskService<>(
                this.accounts,
                this.traders,
                this.securities,this.log);
        this.orderService.addListener(riskService);
        this.accounts.addListener(riskService);

        sender.addContributorDefinedListener(this.orderService);
        this.addAllCommandsClearedListener(this);
        this.securities.addListener(this);
        this.orderService.addListener(this);
        this.orderService.addRejectListener(this);

        coreConnector.addSessionSourceListener(ouchAdapter::setSession);

        contribIDSet.add(this.getContribID());
        contributorService.addListener((contributor, msg, isNew) -> {
                    if(isNew){
                        todayTradeDate = TimeUtils.toDateInt(msg.getTimestampAsTime().toLocalDate());
                        if(contribSet.contains(contributor.getName())){
                            orderService.onContributorDefined(contributor.getID(),contributor.getName());
                            log.info(log.log().add("Added Contributor:").add(contributor.getName()));

                        }
                    }
                }
        );
        this.accounts.addListener((account, msg, isNew) -> {
            if(acctSet==null){
                if(isNew) {
					addAccount(account);
				}
            }else{
                if(isNew && acctSet.contains(account.getName())){
                    addAccount(account);
                }
            }
        });

        this.traders.addListener((tempTrader, msg, isNew) -> {
            if(isNew && fixedAccounts.containsKey(tempTrader.getAccountID())){
                addTrader(tempTrader);
            }
        });
        connected=false;
        riskValidator=new OrderRiskValidator(log,riskService);
        quantityValidator=new OrderQuantityValidator(log);

    }

    public void addTrader(Trader trader) {
        ByteStringBuffer temp = new ByteStringBuffer();
        temp.clear();
        temp.add(trader.getName());
        ByteBuffer buffer = temp.getUnderlyingBuffer();
        this.fixedTraders.put(buffer, trader);
    }

    public void addAccount(Account account) {
        this.fixedAccounts.put(account.getID(), account);
    }



    @Override
    protected void beforeActive() {

        if (fixedTraders.size() == 0)
        {
            throw new CommandException(
                    "Trader not defined: ");
        }
        if (fixedAccounts.size() ==0)
        {
            throw new CommandException(
                    "Accountd not defined: ");
        }
    }

    @Override
    protected void onActive()
    {
        this.ouchSoupConnectionController= adaptorFactory.getOUCHConnectionController(ouchAdapter,log,this);

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

    @Override
    protected void onPassive()
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
                    "Could not close OUCH adapter");
        }
    }



    @Override
    public void onAddHeartbeatFields(HeartbeatFieldRegister fieldRegister) {
        liveOrders = fieldRegister.addNumberField("Stats", HeartBeatFieldIDEnum.LiveOrders);
        ordersSent = fieldRegister.addNumberField("Stats", HeartBeatFieldIDEnum.SendOrders);
        cancelsSent = fieldRegister.addNumberField("Stats", HeartBeatFieldIDEnum.Cancels);
        replacesSent = fieldRegister.addNumberField("Stats", HeartBeatFieldIDEnum.Replaces);
        rejectsSent = fieldRegister.addNumberField("Stats", HeartBeatFieldIDEnum.Rejects);
        cancelReplaceRejectsSent = fieldRegister.addNumberField("Stats", HeartBeatFieldIDEnum.CnlRplRejects);
        fillsSent = fieldRegister.addNumberField("Stats", HeartBeatFieldIDEnum.Fills);
        loggedInHeartbeatField = fieldRegister.addBoolField("Stats", HeartBeatFieldIDEnum.LoggedIn);
        connectedHeartbeatField = fieldRegister.addBoolField("Stats", HeartBeatFieldIDEnum.Connected);
        activeHeartbeatField = fieldRegister.addBoolField("Stats", HeartBeatFieldIDEnum.Active);
    }

    @Override
    public void onUpdateHeartbeatFields(HeartbeatFieldUpdater fieldUpdater) {
        liveOrders.set( ordersRepository.getNumberOfLiveOrders());
        loggedInHeartbeatField.set(ouchAdapter.isLoggedIn());
        connectedHeartbeatField.set(connected);
        activeHeartbeatField.set(canSend());
    }

    private void rejectOrder(char c, long clOrdId, BaseSecurity security, boolean isBuy, Trader trader) {
        Account account = null;
        if (trader != null) {
            account = fixedAccounts.get(trader.getAccountID());
        }
        MatchClientOrderRejectCommand command = coreMessages.getMatchClientOrderRejectCommand();
        command.setReason(c);
        command.setTrader(account != null ? account.getName() : "");
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
    public void onOrder(OUCHOrder order, MatchOrderEvent msg) {

        log.debug(log.log().add("TX Ouch Accpt: ").add(msg.getClOrdIDAsString()));
        log.debug(log.log().add("Received OUCH Accepted: ").add(System.nanoTime()));
        ordersSent.inc();
        ordersRepository.add(order,msg);


        OUCH2AcceptedCommand cmd = ouchAdapter.getOUCHAccepted();
        cmd.setTimestamp(msg.getTimestamp());
        cmd.setClOrdID(order.getClOrdID());
        cmd.setExternalOrderID(msg.getExternalOrderID());
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
        ordersRepository.removeOrder(order);
        // outgoing order canceled to client
        OUCH2CanceledCommand cmd = ouchAdapter.getOUCHCanceled();
        cmd.setTimestamp(msg.getTimestamp());
        cmd.setClOrdID(order.getClOrdID());
        cmd.setReason(OUCHConstants.CanceledReason.UserRequest);
        ouchAdapter.send(cmd);
    }

    @Override
    public void onReplace(OUCHOrder order, MatchReplaceEvent msg, ReplaceUpdates updates) {
        replacesSent.inc();
        ordersRepository.replaceAndRefresh(order,msg);

        OUCH2ReplacedCommand cmd = ouchAdapter.getOUCHReplaced();
        cmd.setTimestamp(msg.getTimestamp());
        cmd.setClOrdID(order.getClOrdID());
        cmd.setOldClOrdId(msg.getOrigClOrdID().getLong());
        cmd.setQty(msg.getQty() * QTY_MULTIPLIER);
        cmd.setPrice(msg.getPrice());
        cmd.setExternalOrderID(msg.getExternalOrderID());
        ouchAdapter.send(cmd);
    }

    @Override
    public void onFill(OUCHOrder order, MatchFillEvent msg) {
        fillsSent.inc();
        if (order.isFilled()){
            ordersRepository.removeOrder(order);
        }
        // outgoing order executed to client
        OUCH2FillCommand cmd = ouchAdapter.getOUCHFillCommand();
        cmd.setTimestamp(msg.getTimestamp());
        long clOrdId = order.getClOrdID();
        cmd.setClOrdID(clOrdId);
        cmd.setExecutionPrice(msg.getPrice());
        cmd.setExecutionQty(msg.getQty() * QTY_MULTIPLIER);
        int matchID = msg.getMatchID();
        cmd.setMatchID(matchID);
        ouchAdapter.send(cmd);
    }

    @Override
    public void onAllCommandsCleared() {
        ouchAdapter.enableRead(true);
        cancelAllLiveOrdersOnDisconnect();
    }

    @Override
    public void onOUCH2Replace(OUCH2ReplaceEvent msg) {
        // incoming replace from the client
        long newClOrdID = msg.getNewClOrdID();
        long oldClOrdID = msg.getClOrdID();

        if(isPassive()) {
            dropMessage(newClOrdID);
            return;
        }

        OUCHOrder ouchOrder = ordersRepository.getByClientOrderID(oldClOrdID);
        if (ouchOrder == null)
        {
            rejectCancelOrReplace(OUCHConstants.RejectReason.UnknownClOrdID, newClOrdID, 0, true, 0);
            return;
        }

        try{
            stringBuffer.clear();
            stringBuffer.add(this.traders.get(ouchOrder.getTraderID()).getName());
            Trader storedTrader = this.fixedTraders.get(stringBuffer.getUnderlyingBuffer());
            Account storedAccount= fixedAccounts.get(storedTrader.getAccountID());
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

            if (!riskValidator.validateDV01Risk(security,storedAccount,newLimitPrice,qtyDifference,ouchOrder.isBuy()))
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
    public void onOrderReject(MatchOrderRejectEvent msg) {
        rejectsSent.inc();
        long clOrdId = formatToLong(msg.getClOrdID());

        ordersRepository.removeOrder(clOrdId);

        OUCH2RejectedCommand cmd = ouchAdapter.getOUCHRejected();
        cmd.setTimestamp(msg.getTimestamp());
        cmd.setClOrdID(clOrdId);
        cmd.setReason(OUCHRejectReasonMap.matchToOUCH(msg.getReason()));
        ouchAdapter.send(cmd);
    }

    @Override
    public void onClientOrderReject(MatchClientOrderRejectEvent msg) {
        OUCH2RejectedCommand cmd = ouchAdapter.getOUCHRejected();
        cmd.setReason(msg.getReason());

        if(msg.hasClOrdID()) {
            long clOrdId = formatToLong(msg.getClOrdID());
            cmd.setClOrdID(clOrdId);
        }
        ouchAdapter.send(cmd);

    }
    @Override
	public boolean isActive(){
        return canSend();
    }

    @Override
    public void onCancelReplaceReject(OUCHOrder order, MatchCancelReplaceRejectEvent msg) {
        cancelReplaceRejectsSent.inc();

        if (msg.getIsReplace())
        {
            OUCH2RejectedCommand cmd = ouchAdapter.getOUCHRejected();
            cmd.setTimestamp(msg.getTimestamp());
            cmd.setClOrdID(msg.getClOrdID().getLong());
            cmd.setReason(OUCHRejectReasonMap.matchToOUCH(msg.getReason()));
            ouchAdapter.send(cmd);
        }
        else
        {
            OUCH2CancelRejectedCommand cmd = ouchAdapter.getOUCHCancelRejected();
            cmd.setTimestamp(msg.getTimestamp());
            if (msg.hasClOrdID()){
                cmd.setClOrdID(msg.getClOrdID().getLong());
            }
            else if (order != null) {
                cmd.setClOrdID(order.getClOrdID());
            }
            cmd.setReason(OUCHRejectReasonMap.matchToOUCH(msg.getReason()));
            ouchAdapter.send(cmd);
        }

    }

    @Override
    public void onClientCancelReplaceReject(OUCHOrder order, MatchClientCancelReplaceRejectEvent msg) {
        if (msg.getIsReplace())
        {
            OUCH2RejectedCommand cmd = ouchAdapter.getOUCHRejected();
            cmd.setTimestamp(msg.getTimestamp());
            cmd.setReason(msg.getReason());
            cmd.setClOrdID(formatToLong(msg.getClOrdID()));
            ouchAdapter.send(cmd);
        }
        else
        {
            OUCH2CancelRejectedCommand cmd = ouchAdapter.getOUCHCancelRejected();
            cmd.setTimestamp(msg.getTimestamp());
            cmd.setReason(msg.getReason());
            cmd.setClOrdID(formatToLong(msg.getClOrdID()));
            ouchAdapter.send(cmd);
            if ( !ouchAdapter.isConnected()) {
                if (order != null) {
                    order.setTriedDisconnect(true);
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
        log.warn(log.log().add("Adding Multileg security to map"));
        stringBuffer.clear();
        stringBuffer.add(security.getName());
        ByteBuffer buffer = ByteBuffer.allocate(security.getName().length());
        BinaryUtils.copy(buffer, stringBuffer.getUnderlyingBuffer());
        buffer.flip();
        securitiesByLongName.put(buffer, security);
    }



    @Override
    public void onOUCH2Cancel(OUCH2CancelEvent msg) {
        long clOrdId = msg.getClOrdID();

        OUCHOrder order = ordersRepository.getByClientOrderID(clOrdId);
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

    private void dropMessage(long clOrdId) {
        ouchAdapter.sendSessionPassiveDebug(clOrdId);
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

    @Override
    public void onOUCH2Order(OUCH2OrderEvent msg) {
        long clOrdId = msg.getClOrdID();

        //This whole block accounts for dropping order when the application is passive
        if(isPassive()){
            ouchAdapter.sendSessionPassiveDebug(clOrdId);
            //TODO
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


        BaseSecurity security = securitiesByLongName.get(msg.getSecurity());
        if (security == null )
        {
            rejectOrder(OUCHConstants.RejectReason.InvalidSecurity, clOrdId, null, isBuy, null);
            return;
        }

        Trader storedTrader;

        storedTrader = this.fixedTraders.get(msg.getTrader());
        if (storedTrader == null)
        {
            log.error(log.log().add("Trader is null"));
            rejectOrder(OUCHConstants.RejectReason.InvalidTrader, clOrdId, security, isBuy, null);
            return;
        }

        if (!systemEventService.isOpen())
        {
            log.debug(log.log().add("Rejected: system not open"));

            rejectOrder(OUCHConstants.RejectReason.TradingSystemClosed, clOrdId, security, isBuy, storedTrader);
            return;
        }

        if (ordersRepository.contains(clOrdId))
        {
            log.debug(log.log().add("Rejected: no clorid "));

            // duplicate
            rejectOrder(OUCHConstants.RejectReason.DuplicateClOrdID, clOrdId, security, isBuy, storedTrader);
            return;
        }
        int qty = msg.getQty() / QTY_MULTIPLIER;

        if(!quantityValidator.validateQuantity(security,msg.getQty())){
            rejectOrder(OUCHConstants.RejectReason.InvalidQuantity, clOrdId, security, isBuy, storedTrader);
            return;
        }

        long limitPrice = msg.getPrice();
        if (security.isBond() &&limitPrice <= 0 || limitPrice % security.getTickSize() != 0)
        {
            log.debug(log.log().add("Rejected: Invalid price").add(limitPrice));
            rejectOrder(OUCHConstants.RejectReason.InvalidPrice, clOrdId, security, isBuy, storedTrader);
            return;
        }



        if (!riskValidator.validateFatFingerRisk(security,storedTrader,qty))
        {
            rejectOrder(OUCHConstants.RejectReason.RiskViolation, clOrdId, security, isBuy, storedTrader);
            return;
        }

        Account account = fixedAccounts.get(storedTrader.getAccountID());

        if (!riskValidator.validateDV01Risk(security,account,limitPrice,qty,isBuy))
        {
            rejectOrder(OUCHConstants.RejectReason.RiskViolation, clOrdId, security, isBuy, storedTrader);
            return;
        }

        MatchOrderCommand cmd = coreMessages.getMatchOrderCommand();
        cmd.setSecurityID(security.getID());
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
    public void cancelAllLiveOrdersOnDisconnect() {
        if(!ouchAdapter.isConnected() && orderService.size() > 0){
            Iterator<OUCHOrder> iterator = orderService.getOrders().iterator();
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
    public void onConnect() {
        if (systemEventService.isOpen()) {
            ouchAdapter.sendDebugMessage("open");
        } else if (systemEventService.isClosed()) {
            ouchAdapter.sendDebugMessage(getClosedMessage(System.nanoTime()));
        } else {
            ouchAdapter.sendDebugMessage("preopen");
        }
    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onOpen(long timestamp) {
        ouchAdapter.sendDebugMessage("open");
    }

    @Override
    public void onClose(long timestamp) {
        ouchAdapter.sendDebugMessage(getClosedMessage(timestamp));
    }

    private String getClosedMessage(long nanos) {
        LocalDate closeDate = tradeDateUtils.getTradeDate(nanos);
        // indicate end of week if the next two days are non-trading days
        if (!calendar.isTradingDate(closeDate.plusDays(1)) && !calendar.isTradingDate(closeDate.plusDays(2))) {
            return "closedEndOfWeek";
        }
        return "closed";
    }

    @Override
    public boolean isInterested(MatchOrderEvent msg) {
        return contribIDSet.contains(msg.getContributorID());
    }

    protected ByteBuffer wrapLong(long aLong) {
        temp.clear();
        temp.putLong(aLong);
        temp.flip();
        return temp;
    }

    private long formatToLong(ByteBuffer byteBuffer){
        clientOrderIdBuffer.clear();
        BinaryUtils.copy(clientOrderIdBuffer,byteBuffer);
        clientOrderIdBuffer.flip();

        return clientOrderIdBuffer.getLong();
    }
}
