package com.core.latencytesting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.LogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.Message;
import quickfix.MessageFactory;
import quickfix.RejectLogon;
import quickfix.RuntimeError;
import quickfix.ScreenLogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.StringField;
import quickfix.ThreadedSocketInitiator;
import quickfix.UnsupportedMessageType;
import quickfix.field.Account;
import quickfix.field.ClOrdID;
import quickfix.field.ExecID;
import quickfix.field.MDEntryPx;
import quickfix.field.MDEntrySize;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MDUpdateAction;
import quickfix.field.MDUpdateType;
import quickfix.field.MarketDepth;
import quickfix.field.MsgType;
import quickfix.field.NoMDEntries;
import quickfix.field.NoMDEntryTypes;
import quickfix.field.NoRelatedSym;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.SecurityListRequestType;
import quickfix.field.SecurityReqID;
import quickfix.field.Side;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix44.MarketDataIncrementalRefresh;
import quickfix.fix44.MarketDataRequest;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;
import quickfix.fix44.SecurityListRequest;

import com.core.match.msgs.MatchConstants;
import com.core.match.util.MatchPriceUtils;
import com.core.testharness.ouch.EventCounter;
import com.core.testharness.ouch.LatencyMeasurer;
import com.core.testharness.ouch.NullEventCounter;
import com.gs.collections.api.map.primitive.MutableLongObjectMap;
import com.gs.collections.impl.map.mutable.primitive.LongObjectHashMap;
import com.gs.collections.impl.set.mutable.UnifiedSet;

/**
 * User: jgreco
 */
public class QuickFixApp implements Application {
    private final Map<String, TreeSet<Level>> bids = new HashMap<>();
    private final Map<String, TreeSet<Level>> offers = new HashMap<>();
    private final EventCounter counter;
    private String account;
    private final Random random = new Random();
    private LatencyMeasurer latencyMeasurer;
    private SessionID sessionId;
    private boolean loggedIn;
    //sets to hold client ids of responses
    private Set<String> acceptMap= new UnifiedSet<>();
    private Set<String> cancelMap= new UnifiedSet<>();
    private Set<String> fillMap= new UnifiedSet<>();
    private Set<String> replaceMap= new UnifiedSet<>();
    private Set<String> rejectMap= new UnifiedSet<>();
    private Set<String> cancelRejectMap= new UnifiedSet<>();


    public void setAccount(String account) {
        this.account = account;
        System.out.println("Account/Trader(DM):"+account);
    }

    public void addLatencyMeasurer(LatencyMeasurer genericLatencyMeasurer) {
        latencyMeasurer=genericLatencyMeasurer;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public boolean getFixReplaceAccept(String s) {
        return replaceMap.contains(s);
    }

    public boolean getFixRejectMessage(String s) {
        return rejectMap.contains(s);
    }
    public boolean getFixCancelRejectMessage(String s) {
        return cancelRejectMap.contains(s);
    }



    private class Level implements Comparable<Level>
    {
        private long price;
        private int qty;
        private boolean bid;

        public Level()
		{
			// TODO Auto-generated constructor stub
		}

		@Override
        public String toString() {
            return "Level{" +
                    "price=" + MatchPriceUtils.to32ndPrice(price) +
                    ", qty=" + qty +
                    '}';
        }

        public long getPrice() {
            return price;
        }

        public void setBid(boolean bid) {
            this.bid = bid;
        }

        public void setPrice(double price) {
            this.price = MatchPriceUtils.toLong(price);
        }

        public void setQty(int qty) {
            this.qty = qty / MatchConstants.QTY_MULTIPLIER;
        }

        @Override
        public int compareTo(Level o) {
            if (bid) {
                return Double.compare(o.price,price);
            }
			return Double.compare(price, o.price);
        }
    }
    private class Order {
        protected Order() { }
        boolean buy;
		int qty;
		double price;
        String security;
    }
    private MutableLongObjectMap<Order> map = new LongObjectHashMap<>();
	private ThreadedSocketInitiator initiator;
    
    public QuickFixApp(String fileName) throws ConfigError {

        this(new SessionSettings(fileName), new NullEventCounter());
    }

    public QuickFixApp(SessionSettings settings, EventCounter counter) throws ConfigError {

        LogFactory logFactory = new ScreenLogFactory(settings);
        MessageFactory messageFactory = new DefaultMessageFactory();
        this.counter=counter;
        this.initiator = new ThreadedSocketInitiator(this, new MemoryStoreFactory(), settings, logFactory, messageFactory);
    }

    public void stop() 
    {
    	this.initiator.stop();
    }
    
    public void start() throws RuntimeError, ConfigError
    {
    	this.initiator.start();
    }

    
    public void sendMarketDataRequest(SessionID session, String security) throws SessionNotFound
    {
        MarketDataRequest request = new MarketDataRequest();
        request.set(new MDReqID(security));
        request.set( new MarketDepth(5));
        request.set(new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));
        request.set(new MDUpdateType(MDUpdateType.INCREMENTAL_REFRESH));
        quickfix.fix44.MarketDataSnapshotFullRefresh.NoMDEntries group = new quickfix.fix44.MarketDataSnapshotFullRefresh.NoMDEntries();
        request.set(new NoMDEntryTypes(3));
        for( int i = 0; i < 3; i ++ )
        {
            group.set( new MDEntryType(( char ) ( i + '0')));
        }
        request.addGroup(group);

        request.set(new NoRelatedSym(1));
        quickfix.fix44.MarketDataRequest.NoRelatedSym symbolGroup = new quickfix.fix44.MarketDataRequest.NoRelatedSym();
        symbolGroup.set(new Symbol(security));
        request.addGroup(symbolGroup);
        Session.sendToTarget(request, session);
    }


    public void sendSecurityListRequest(SessionID session) throws SessionNotFound
    {
        SecurityListRequest request = new SecurityListRequest();
        request.set(new SecurityReqID("1"));
        request.set(new SecurityListRequestType(SecurityListRequestType.ALL_SECURITIES));
        Session.sendToTarget(request, session);
    }

    public long sendOrder(SessionID sessionID, boolean buy, int qty, String security, double price) throws SessionNotFound {
        Order order1 = new Order();
        order1.buy = buy;
        order1.qty = qty*MatchConstants.QTY_MULTIPLIER;

        order1.security = security;
        order1.price = price;
        String clOrderID= createClOrdID();

        map.put(Long.valueOf(clOrderID),order1);
        NewOrderSingle order = new NewOrderSingle();
        order.set(new ClOrdID(clOrderID));
        order.set(new OrdType('2'));

        order.set(new Side(buy ? Side.BUY : Side.SELL));
        order.set(new OrderQty(order1.qty));
        order.set(new Symbol(security));
        order.set(new Price(price));
        order.set(new Account(account));
        if(Session.sendToTarget(order, sessionID)){
            return Long.valueOf(clOrderID);

        }else{
            return 0;

        }
    }
    private String createClOrdID() {
        int clOrdId = Math.abs(random.nextInt());
        String stringID = String.format("%08d", clOrdId);
        return stringID;
    }

    public boolean sendCancel(SessionID sessionID, long orderId) throws SessionNotFound {
        OrderCancelRequest cancel = new OrderCancelRequest();
        cancel.set(new ClOrdID(String.valueOf(orderId)));
        return Session.sendToTarget(cancel, sessionID);
    }

    public long sendReplace(SessionID sessionID, long orderId, int qty, double price) throws SessionNotFound {
        String nextOrderId = createClOrdID();

        Order order = map.get(orderId);
        if (order == null) {
            System.out.println("No order found: " + orderId);
            return 0;
        }

        order.qty = qty;
        order.price = price;

        OrderCancelReplaceRequest replace = new OrderCancelReplaceRequest();
        replace.set(new OrigClOrdID(String.valueOf(orderId)));
        replace.set(new ClOrdID(String.valueOf(nextOrderId)));
        replace.set(new OrderQty(qty*MatchConstants.QTY_MULTIPLIER));
        replace.set(new Price(price));
        replace.set(new Side(order.buy ? Side.BUY : Side.SELL));
        replace.set(new Symbol(order.security));

        Boolean result=Session.sendToTarget(replace, sessionID);
        if(result){
            return Long.valueOf(nextOrderId);

        }else{
            //return 0 to indicate that send request was not successful
            return 0;
        }
    }

    @Override
    public void onCreate(SessionID sessionID) {
        System.out.println("Create: " + sessionID);
    }

    @Override
    public void onLogon(SessionID sessionID) {

        this.sessionId = sessionID;
        loggedIn=true;
    }

    @Override
    public void onLogout(SessionID sessionID) {
        //System.out.println("Logout: " + sessionID);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionID) {
        //System.out.println("To Admin: " + message);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        System.out.println("From Admin: " + message);
    }

    @Override
    public void toApp(Message message, SessionID sessionID) throws DoNotSend {
        //System.out.println("To App: " + message);
    }

    @Override
    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        MsgType type = new MsgType();
        message.getHeader().getField(type);
        if( type.getValue().equals(MsgType.MARKET_DATA_INCREMENTAL_REFRESH))
        {
            handleMarketDataIncrementalUpdate(message);
            System.out.println( bids );
            System.out.println( offers );
        }else if(type.getValue().equals(MsgType.EXECUTION_REPORT)){
            //in our system, all replies are execution reports
            onOrderEntryResponses(message);
        }

        else if(type.getValue().equals(MsgType.ORDER_CANCEL_REJECT)){
            //in our system, all replies are execution reports
            onCancelReject(message);
        }
    }

    private void onCancelReject(Message message) {
        ClOrdID clOrdID=new ClOrdID();
        TransactTime transactTime = new TransactTime();
        //We can further deserialize this
        try {
            StringField clientID = message.getField(clOrdID);
            String clientOrderID = clientID.getValue();
            counter.countDown(FixCounterType.CancelReject);
            cancelRejectMap.add(clientOrderID);

        }
        catch(FieldNotFound fieldNotFound){
                fieldNotFound.printStackTrace();
        }
    }
    public void waitAccept() throws InterruptedException {
        counter.wait(FixCounterType.Accept);
    }

    public void waitCancel() throws InterruptedException {
        counter.wait(FixCounterType.Cancel);
    }

    public void waitReject() throws InterruptedException {
        counter.wait(FixCounterType.Reject);
    }

    public void waitReplace() throws InterruptedException {
        counter.wait(FixCounterType.Replace);
    }

    public void waitFill() throws InterruptedException {
        counter.wait(FixCounterType.Fill);
    }

    public void waitCancelReject() throws InterruptedException {
        counter.wait(FixCounterType.CancelReject);
    }

    public void waitLogin() throws InterruptedException {
        counter.wait(FixCounterType.Login);
    }

    public void expectCanceled(int number) {
        counter.createCounter(FixCounterType.Cancel, number);
    }

    public void expectAccepted(int number) {
        counter.createCounter(FixCounterType.Accept, number);
    }

    public void expectRejected(int number) {
        counter.createCounter(FixCounterType.Reject, number);
    }

    public void expectReplaced(int number) {
        counter.createCounter(FixCounterType.Replace, number);
    }

    public void expectFills(int number) {
        counter.createCounter(FixCounterType.Fill, number);
    }

    public void expectCancelReject(int number) {
        counter.createCounter(FixCounterType.CancelReject, number);
    }


    private void onOrderEntryResponses(Message message) {
        ClOrdID clOrdID=new ClOrdID();
        ExecID execID = new ExecID();
        //We can further deserialize this
        try {
            StringField clientID = message.getField(clOrdID);
            String clientOrderID= clientID.getValue();
            StringField execIDValue= message.getField(execID);
            if(execID.getValue().startsWith("D")){
                acceptMap.add(clientOrderID);
                System.out.print("GOT ACCEPT:"+clientOrderID);
                counter.countDown(FixCounterType.Accept);

            }else if(execID.getValue().startsWith("G")){
                replaceMap.add(clientOrderID);
                counter.countDown(FixCounterType.Replace);
            }else if(execID.getValue().startsWith("R")){
                rejectMap.add(clientOrderID);
                counter.countDown(FixCounterType.Reject);

            }else if(execID.getValue().startsWith("X")){
                fillMap.add(clientOrderID);
                counter.countDown(FixCounterType.Fill);
            }

        } catch (FieldNotFound fieldNotFound) {
            System.out.println("FieldNotFound: ");
            fieldNotFound.printStackTrace();
        }
        //tag 17="Prefix based on action"+currentSeq
    }

    public boolean getFixAcceptMessage(String id){
        System.out.println("getFixAcceptMessage: "+id);
       return acceptMap.contains(id);
    }

    private void handleMarketDataIncrementalUpdate(Message message) throws FieldNotFound {
        NoMDEntries entries = new NoMDEntries();
        message.getField(entries);

        MarketDataIncrementalRefresh.NoMDEntries group = new MarketDataIncrementalRefresh.NoMDEntries();

        for( int i = 1; i <= entries.getValue(); i ++ )
        {
            message.getGroup(i, group);
            Symbol symbol = new Symbol();
            group.getField( symbol );
            MDEntryPx price = new MDEntryPx();
            group.getField(price);
            MDEntrySize quantity = new MDEntrySize();
            group.getField(quantity);
            MDEntryType entryType = new MDEntryType();
            group.getField(entryType);
            MDUpdateAction action = new MDUpdateAction();
            group.getField(action);

            TreeSet<Level> levels;

            double doublePrice = price.getValue();
            long longPrice = MatchPriceUtils.toLong(doublePrice);
            String thirtySecondPrice = MatchPriceUtils.to32ndPrice(longPrice);

            char entryTypeVal = entryType.getValue();
            if(entryTypeVal == MDEntryType.BID )
            {
                levels = bids.get(symbol.getValue());
                if( levels == null ) {
                    levels = new TreeSet<>();
                    bids.put(symbol.getValue(), levels);
                }
            }
            else if (entryTypeVal == MDEntryType.OFFER)
            {
                levels = offers.get(symbol.getValue());
                if( levels == null ) {
                    levels = new TreeSet<>();
                    offers.put(symbol.getValue(), levels);
                }
            }
            else {
                System.out.println("TRADE " + symbol + " " + quantity + " @ " + thirtySecondPrice);
                // TRADE
                continue;
            }

            if(action.getValue() == MDUpdateAction.NEW) {
                Level level = new Level();
                level.setBid(entryTypeVal == MDEntryType.BID );
                level.setQty((int)quantity.getValue());
                level.setPrice(doublePrice);
                levels.add(level);
            }
            else {
                boolean found = false;
                Iterator<Level> iter = levels.iterator();
                while (iter.hasNext()) {
                    Level level = iter.next();
                    if (level.getPrice() == longPrice) {
                        found = true;
                        if (action.getValue() == MDUpdateAction.CHANGE) {
                            level.setQty((int) quantity.getValue());
                        }
                        else {
                            iter.remove();
                        }
                    }
                }

                if (!found) {
                    System.out.println("EXPECTED TO FIND LEVEL AT PRICE: " + thirtySecondPrice);
                }
            }
        }
    }
}
