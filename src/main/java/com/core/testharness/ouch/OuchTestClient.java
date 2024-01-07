package com.core.testharness.ouch;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import com.core.connector.soup.SoupBinTCPClientAdapter;
import com.core.connector.soup.msgs.SoupLoginAcceptedEvent;
import com.core.connector.soup.msgs.SoupLoginEventListener;
import com.core.connector.soup.msgs.SoupLoginRejectedEvent;
import com.core.match.msgs.MatchConstants;
import com.core.match.ouch.client.OUCHClientOrder;
import com.core.match.ouch.client.OUCHClientOrderService;
import com.core.match.ouch.client.OUCHClientOrderServiceListener;
import com.core.match.ouch.msgs.OUCHAcceptedEvent;
import com.core.match.ouch.msgs.OUCHByteBufferDispatcher;
import com.core.match.ouch.msgs.OUCHByteBufferMessages;
import com.core.match.ouch.msgs.OUCHCancelCommand;
import com.core.match.ouch.msgs.OUCHCancelRejectedEvent;
import com.core.match.ouch.msgs.OUCHCanceledEvent;
import com.core.match.ouch.msgs.OUCHConstants;
import com.core.match.ouch.msgs.OUCHFillEvent;
import com.core.match.ouch.msgs.OUCHOrderCommand;
import com.core.match.ouch.msgs.OUCHRejectedEvent;
import com.core.match.ouch.msgs.OUCHReplaceCommand;
import com.core.match.ouch.msgs.OUCHReplacedEvent;
import com.core.match.ouch.msgs.OUCHTradeConfirmationEvent;
import com.core.nio.SelectorService;
import com.core.util.PriceUtils;
import com.core.util.log.Log;
import com.core.util.time.TimeSource;
import com.gs.collections.impl.map.mutable.primitive.LongObjectHashMap;
import com.gs.collections.impl.set.mutable.primitive.LongHashSet;

/**
 * Created by hli on 11/9/15.
 */
public class OuchTestClient implements OUCHClientOrderServiceListener, SoupLoginEventListener, LatencyTestClient {
    private final Log log;
    private final String ouch_un;
    private final String ouch_pw;
    private final OUCHByteBufferMessages ouchMessages;
    private final SoupBinTCPClientAdapter ouchAdapter;
    
    private final LongObjectHashMap<OUCHClientOrder> acceptMgs = new LongObjectHashMap<>();
    private final Queue<OUCHFillHolder> fillQueue = new LinkedList<>();
    private final LongObjectHashMap<OUCHClientOrder> cancelMsgs = new LongObjectHashMap<>();
    private final LongObjectHashMap<OUCHClientOrder> replacedMsgs = new LongObjectHashMap<>();
    private final LongHashSet tradeMsgs = new LongHashSet(); 
    private final LongHashSet rejects = new LongHashSet();

    private final int multiplier;
    private final EventCounter counter;
    private final GenericLatencyMeasurer orderAcceptLatencyMeasurer;

    private final Random random = new Random();

    private final OUCHClientOrderService ouchClientOrderService;
    private boolean loggedIn;

    public OuchTestClient(String host,
                          int port,
                          String username,
                          String password,
                          int qtyMultiplier,
                          SelectorService select,
                          TimeSource timeSource,
                          Log log,
                          EventCounter counter) throws IOException {
        this.multiplier=qtyMultiplier;
        this.ouch_pw=password;
        this.ouch_un=username;
        this.log = log;
        this.counter = counter;
        counter.createCounter(OUCHCounterTypes.Login, 1);

        orderAcceptLatencyMeasurer = new GenericLatencyMeasurer(timeSource, log);

        ouchMessages = new OUCHByteBufferMessages();
        OUCHByteBufferDispatcher ouchDispatcher = new OUCHByteBufferDispatcher(ouchMessages);

        ouchAdapter = new SoupBinTCPClientAdapter(this.log, select, select, host, port, ouchDispatcher, true, ouch_un, ouch_pw);
        ouchAdapter.setLoginListener(this);

        ouchClientOrderService = new OUCHClientOrderService(this.log);
        ouchDispatcher.subscribe(ouchClientOrderService);
        
        ouchClientOrderService.addListener(this);
        ouchAdapter.setLastSeqNum(-1);
        ouchAdapter.open();
    }

    @Override
    public long sendNewOrder(String security, Boolean isBuy, int qty, double price, String trader, boolean isIOC){
        OUCHOrderCommand order = ouchMessages.getOUCHOrderCommand();
        long id = createClOrdID();
        order.setClOrdID(id);
        order.setSecurity(security);
        order.setSide(isBuy ? OUCHConstants.Side.Buy : OUCHConstants.Side.Sell);
        order.setQty(qty * multiplier);
        order.setPrice(PriceUtils.toLong(price, MatchConstants.IMPLIED_DECIMALS));
        order.setTrader(trader);
        order.setTimeInForce(isIOC ? OUCHConstants.TimeInForce.IOC : OUCHConstants.TimeInForce.DAY);

        ByteBuffer rawBuffer = order.getRawBuffer().slice();
        rawBuffer.limit(order.getLength());

        ouchAdapter.send(rawBuffer);
        orderAcceptLatencyMeasurer.start(id);

        if (log.isDebugEnabled()) {
            log.debug(log.log().add("Order Send ").add(id));
        }
        return id;
    }

    public void cancelOrder(long id) {
        OUCHCancelCommand command = ouchMessages.getOUCHCancelCommand();
        command.setClOrdID(id);
        ByteBuffer rawBuffer = command.getRawBuffer().slice();
        rawBuffer.limit(command.getLength());

        ouchAdapter.send(rawBuffer);
        orderAcceptLatencyMeasurer.start(-id);

        if (log.isDebugEnabled()) {
            log.debug(log.log().add("Order Cancelled: ").add(id));
        }
        // TODO: Not a unique clOrdID
    }

    public long replaceOrder(long oldClOrderId, int newQty, double newPrice){
        OUCHReplaceCommand command = ouchMessages.getOUCHReplaceCommand();
        command.setClOrdID(oldClOrderId);
        command.setNewQty(newQty * multiplier);
        command.setNewPrice(PriceUtils.toLong(newPrice, MatchConstants.IMPLIED_DECIMALS));

        long replaceClOrId = createClOrdID();
        command.setNewClOrdID(replaceClOrId);
        ByteBuffer rawBuffer = command.getRawBuffer().slice();
        rawBuffer.limit(command.getLength());

        ouchAdapter.send(rawBuffer);



        if (log.isDebugEnabled()) {
            log.debug(log.log().add("Replace Send ").add(replaceClOrId));
        }
        return replaceClOrId;
    }

    @Override
    public void onOUCHAccepted(OUCHAcceptedEvent msg, OUCHClientOrder order) {
    	long id = order.getClOrdID();
        orderAcceptLatencyMeasurer.stop(id);
        acceptMgs.put(id, order);
        counter.countDown(OUCHCounterTypes.Accept);
    }

    @Override
    public void onOUCHRejected(OUCHRejectedEvent msg) {
        long id = msg.getClOrdID();
        rejects.add(id);
        counter.countDown(OUCHCounterTypes.Reject);
    }

    @Override
    public void onOUCHReplaced(OUCHReplacedEvent msg, OUCHClientOrder order) {
        long id = order.getClOrdID();
        replacedMsgs.put(id, order);
        counter.countDown(OUCHCounterTypes.Replace);
    }

    @Override
    public void onOUCHCanceled(OUCHCanceledEvent msg, OUCHClientOrder order) {
    	long id = msg.getClOrdID();
        // This order is going to be recycled though...
        cancelMsgs.put(id, order);
        counter.countDown(OUCHCounterTypes.Cancel);

    }

    @Override
    public void onOUCHCancelRejected(OUCHCancelRejectedEvent msg, OUCHClientOrder order) {
        long id = msg.getClOrdID();
        counter.countDown(OUCHCounterTypes.Reject);
    }

    @Override
    public void onOUCHFill(OUCHFillEvent msg, OUCHClientOrder order) {
    	long id = msg.getClOrdID();
		OUCHFillHolder createFillObject = createFillObject(msg);
		fillQueue.add(createFillObject);
        counter.countDown(OUCHCounterTypes.Fill);
    }

    @Override
    public void onOUCHTradeConfirm(OUCHTradeConfirmationEvent msg) {
        tradeMsgs.add(msg.getClOrdID());
    }

    @Override
    public void onSoupLoginAccepted(SoupLoginAcceptedEvent msg) {
        counter.countDown(OUCHCounterTypes.Login);
        loggedIn = true;
    }
    
	@Override
	public void onSoupLoginRejected(SoupLoginRejectedEvent msg) {
		// do nothing
	}

    public Queue<OUCHFillHolder> getFillQueue() {
        return fillQueue;
    }

    @Override
    public boolean isLoggedIn() {
        return loggedIn;
    }

    @Override
    public GenericLatencyMeasurer getOrderAcceptLatencyMeasurer() {
        return orderAcceptLatencyMeasurer;
    }


    public OUCHFillHolder createFillObject( OUCHFillEvent event )  {
    	OUCHFillHolder holder = new OUCHFillHolder();
    	holder.msgType = event.getMsgType(); 
    	holder.clOrdID = event.getClOrdID(); 
    	holder.executionQuantity = event.getExecutionQty(); 
    	holder.price = event.getExecutionPrice(); 
    	return holder;
    }

    public void close() throws InterruptedException {
        ouchAdapter.logOut();
        ouchAdapter.close();
        Thread.sleep(2000);
    }

    public OUCHFillHolder getFill() {
    	return fillQueue.poll();
    }

    public OUCHClientOrder getAcceptedOrder(long clOrdID){
        return acceptMgs.get(clOrdID);
    }

    public boolean getRejectedOrder(long id) {
        return rejects.contains(id);
    }

    public OUCHClientOrder getCancelOrder(long id) {
        return cancelMsgs.get(id);
    }

    public OUCHClientOrder getReplaceOrder(long id) {
        return replacedMsgs.get(id);
    }

    public void waitAccept() throws InterruptedException {
        counter.wait(OUCHCounterTypes.Accept);
    }

    public void waitCancel() throws InterruptedException {
        counter.wait(OUCHCounterTypes.Cancel);
    }

    public void waitReject() throws InterruptedException {
        counter.wait(OUCHCounterTypes.Reject);
    }

    public void waitReplace() throws InterruptedException {
        counter.wait(OUCHCounterTypes.Replace);
    }

    public void waitFill() throws InterruptedException {
        counter.wait(OUCHCounterTypes.Fill);
    }

    public void waitLogin() throws InterruptedException {
        counter.wait(OUCHCounterTypes.Login);
    }

    public void expectCanceled(int number) {
        counter.createCounter(OUCHCounterTypes.Cancel, number);
    }
    
	public void expectAccepted(int number) {
        counter.createCounter(OUCHCounterTypes.Accept, number);
	}
	
	public void expectRejected(int number) {
        counter.createCounter(OUCHCounterTypes.Reject, number);
	}
	
	public void expectReplaced(int number) {
        counter.createCounter(OUCHCounterTypes.Replace, number);
	}

    public void expectFills(int number) {
        counter.createCounter(OUCHCounterTypes.Fill, number);
    }


    private long createClOrdID() {
        int clOrdId = Math.abs(random.nextInt());
        String stringID = String.format("%08d", clOrdId);
        return ByteBuffer.wrap(stringID.getBytes()).getLong();
    }

    public class OUCHFillHolder  {
        char msgType;
        long clOrdID;
        long executionQuantity;
        long price;

        public char getMsgType()
        {
            return this.msgType;
        }

        public long getClOrdID()
        {
            return this.clOrdID;
        }

        public long getExecutionQuantity()
        {
            return this.executionQuantity;
        }

        public long getPrice()
        {
            return this.price;
        }
    }
}
