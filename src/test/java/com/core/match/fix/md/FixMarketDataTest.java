package com.core.match.fix.md;

import com.core.fix.FixMessage;
import com.core.fix.msgs.FixConstants;
import com.core.fix.msgs.FixTags;
import com.core.match.StubMatchCommandSender;
import com.core.match.fix.FixOrder;
import com.core.match.fix.GenericFIXAppTest;
import com.core.util.TimeUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.core.fix.msgs.FixConstants.MDEntryType.*;
import static com.core.fix.msgs.FixConstants.MDUpdateAction.*;
import static com.core.match.msgs.MatchConstants.QTY_MULTIPLIER;

/**
 * Created by johnlevidy on 6/3/15.
 */
public class FixMarketDataTest extends GenericFIXAppTest<FixOrder> {
    private FixMarketData marketData;

    public FixMarketDataTest() {
        super(FixOrder.class, 4, "SENDER", "TARGET");
    }

    @Before
    public void setup() {
        this.marketData = new FixMarketData(
                this.log,
                this.timeSource,
                this.select,
                connector,
                this.dispatcher,
                new StubMatchCommandSender("SIM01", (short)1, dispatcher),
                msgs,
                this.fixConnector,
                this.fixParser,
                this.fixWriter,
                this.fixStore,
                this.fixDispatcher,
                4,
                "SENDER",
                "TARGET",
                5);

        fixConnector.connect();
        log.setDebug(true);
    }

    public void makeSecurityListRequest()
    {
        fixParser.writeTag(FixTags.MsgType, 'x');
        fixParser.writeTag(FixTags.SenderCompID, "SENDER");
        fixParser.writeTag(FixTags.TargetCompID, "TARGET");
        fixParser.writeTag(FixTags.SecurityReqID, 1);
        fixParser.writeTag(FixTags.SecurityListRequestType, FixConstants.SecurityListRequestType.Symbol);
    }

    public void makeMarketDataRequest()
    {
        fixParser.writeTag(FixTags.MDReqID, 1);
        fixParser.writeTag(FixTags.SubscriptionRequestType, 1);
        fixParser.writeTag(FixTags.MarketDepth, 5);
        fixParser.writeTag(FixTags.MDUpdateType, 1);
        fixParser.writeTag(FixTags.NoMDEntryTypes, 3);
        fixParser.writeTag(FixTags.MDEntryType, "012");
        fixParser.writeTag(FixTags.NoRelatedSym, 1);
        fixParser.writeTag(FixTags.Symbol, "2Y");
        fixParser.writeTag(FixTags.MsgType, "V");
    }

    private void verifySingle(char action, char side, double price, int qty) {
        marketData.onTimer(0, 0);

        FixMessage fixMessage = fixConnector.get();
        Assert.assertEquals('X', fixMessage.getMsgType());
        Assert.assertEquals("2Y", fixMessage.getValueAsString(FixTags.Symbol));
        Assert.assertEquals("1", fixMessage.getValueAsString(FixTags.MDReqID));
        Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.NoMDEntries));
        Assert.assertEquals(action, fixMessage.getValueAsChar(FixTags.MDUpdateAction));
        Assert.assertEquals(side, fixMessage.getValueAsChar(FixTags.MDEntryType));
        Assert.assertEquals(price, fixMessage.getValueAsPrice(FixTags.MDEntryPx), .0001);
        Assert.assertEquals(qty * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize));
    }

    private FixMessage verifyMultiple(int bids, int offers, int trades) {
        int num = bids + offers + trades;
        FixMessage fixMessage = fixConnector.get();

        Assert.assertEquals('X', fixMessage.getMsgType());
        Assert.assertEquals("1", fixMessage.getValueAsString(FixTags.MDReqID));

        int totalNum = fixMessage.getValueAsInt(FixTags.NoMDEntries);

        int nbids = 0, noffers = 0, ntrades = 0;
        for (int i=0; i<totalNum; i++) {
            Assert.assertEquals("2Y", fixMessage.getValueAsString(FixTags.Symbol, i));

            switch (fixMessage.getValueAsChar(FixTags.MDEntryType, i)) {
                case Bid:
                    nbids++;
                    break;
                case Offer:
                    noffers++;
                    break;
                case Trade:
                    ntrades++;
                    break;
            }
        }

        Assert.assertEquals(bids, nbids);
        Assert.assertEquals(offers, noffers);
        Assert.assertEquals(trades, ntrades);
        Assert.assertEquals(num, totalNum);

        return fixMessage;
    }

    
    @Test
    public void testSecurityList()
    {
        this.makeSecurityListRequest();
        this.timeSource.setTimestamp(1433368583 * TimeUtils.NANOS_PER_MILLI);

        this.marketData.onFixSecurityListRequest();
        FixMessage fixMessage = fixConnector.get();
        Assert.assertEquals('y', fixMessage.getMsgType());

        sendBond("2Y");
        this.marketData.onFixSecurityListRequest();
        fixMessage = fixConnector.get();
        Assert.assertEquals('y', fixMessage.getMsgType());
        Assert.assertEquals("2Y", fixMessage.getValueAsString(FixTags.Symbol));
    }

    
    @Test
    public void testMarketDataRequestReject()
    {
        makeMarketDataRequest();
        this.marketData.onFixMarketDataRequest();

        FixMessage fixMessage = fixConnector.get();
        Assert.assertEquals('Y', fixMessage.getMsgType());
    }

    @Test
    public void testCancelQty()
    {
        makeMarketDataRequest();
        sendBond("2Y");
        this.marketData.onFixMarketDataRequest();

        int orderId = sendPassiveOrder("TRADER", true, 10, "2Y", 100);

        verifySingle(New, Bid, 100, 10);

        sendCancel(orderId);
        verifySingle(Delete, Bid, 100, 10);
    }

    @Test
    public void testReplaceThenFill()
    {
    	makeMarketDataRequest();
    	sendBond("2Y");
    	this.marketData.onFixMarketDataRequest();

    	int orderId = sendPassiveOrder("TRADER", true, 10, "2Y", 100);
    	verifySingle(New, Bid, 100, 10);
    	sendPassiveReplace(orderId, 12, 100);
    	verifySingle(Change, Bid, 100, 12);
    }

    @Test
    public void testFillThenCancelQty()
    {
    	makeMarketDataRequest();
    	sendBond("2Y");
    	this.marketData.onFixMarketDataRequest();
    	
    	int orderId = sendPassiveOrder("TRADER", true, 10, "2Y", 100);
    	
    	verifySingle(New, Bid, 100, 10);

    	sendFill(true, orderId, 5, 100);
    	
    	verifySingle(Change, Bid, 100, 5);
    	
    	sendCancel(orderId);
    	verifySingle(Delete, Bid, 100, 5);
    }

    @Test
    public void testChangeQty()
    {
        makeMarketDataRequest();
        sendBond("2Y");

        sendPassiveOrder("TRADER", true, 100, "2Y", 100);

        this.marketData.onFixMarketDataRequest();

        verifySingle(New, Bid, 100, 100);

        sendPassiveOrder("TRADER", false, 100, "2Y", 101);
        verifySingle(New, Offer, 101, 100);

        sendPassiveOrder("TRADER", false, 50, "2Y", 101);
        verifySingle(Change, Offer, 101, 150);

        sendPassiveOrder("TRADER", false, 20, "2Y", 101);
        verifySingle(Change, Offer, 101, 170);
    }
    
    @Test
    public void testMarketDataRequestAggregation()
    {
        makeMarketDataRequest();
        sendBond("2Y");

        this.marketData.onFixMarketDataRequest();

        // new. B 100 @ 100.0
        // 100 - 100.0 x 0
        sendPassiveOrder("TRADER", true, 100, "2Y", 100);
        verifySingle(New, Bid, 100, 100);

        // new. S 100 @ 101.0
        // 100 - 100.0 x 101 - 100
        sendPassiveOrder("TRADER", false, 100, "2Y", 101);
        verifySingle(New, Offer, 101, 100);

        // update. S 150 @ 101.0
        // 100 - 100.0 x 101 - 150
        sendPassiveOrder("TRADER", false, 50, "2Y", 101);
        verifySingle(Change, Offer, 101, 150);

        // new. B 100 @ 99.0
        // 100 - 100.0 x 101.0 - 150
        // 100 - 99.0
        sendPassiveOrder("TRADER", true, 100, "2Y", 99);
        verifySingle(New, Bid, 99, 100);

        // 100 - 100.0 x 100.5 - 100
        // 100 -  99.0 x 101.0 - 150
        sendPassiveOrder("TRADER", false, 100, "2Y", 100.5);
        verifySingle(New, Offer, 100.5, 100);

        // 100 - 100.0 x 100.5 - 150
        // 100 -  99.0 x 101.0 - 150
        sendPassiveOrder("TRADER", false, 50, "2Y", 100.5);
        verifySingle(Change, Offer, 100.5, 150);

        // 100 - 100.0 x 100.5 - 170
        // 100 -  99.0 x 101.0 - 150
        sendPassiveOrder("TRADER", false, 20, "2Y", 100.5);
        verifySingle(Change, Offer, 100.5, 170);

        // 100 - 100.0 x 100.5 - 170
        // 120 -  99.0 x 101.0 - 150
        sendPassiveOrder("TRADER", true, 20, "2Y", 99);
        verifySingle(Change, Bid, 99, 120);
    }

    @Test
    public void testAggressive()
    {
        makeMarketDataRequest();
        sendBond("2Y");
        this.marketData.onFixMarketDataRequest();

        // passive order
        sendPassiveOrder("TRADER", true, 100, "2Y", 100);

        verifySingle(New, Bid, 100, 100);

        // aggressive order
        sendAggressiveOrder("TRADER", false, 10, "2Y", 100, false);
    }
    
    @Test
    public void testFixIssue() {
        makeMarketDataRequest();
        sendBond("2Y");

        this.marketData.onFixMarketDataRequest();

        // 100 - 101.0 x 0
        sendPassiveOrder("TRADER", true, 100, "2Y", 101);
        marketData.onTimer(0, 0);
        FixMessage msg = fixConnector.get();
        Assert.assertEquals(1, msg.getValueAsInt(FixTags.NoMDEntries));

        // 100 - 101.0 x 0
        // 100 -  99.0 x 0
        sendPassiveOrder("TRADER", true, 100, "2Y", 99);
        marketData.onTimer(0, 0);
        msg = fixConnector.get();
        Assert.assertEquals(1, msg.getValueAsInt(FixTags.NoMDEntries));

        // 100 - 101.0 x 0
        // 100 - 100.0 x 0
        // 100 -  99.0 x 0
        sendPassiveOrder("TRADER", true, 100, "2Y", 100);
        marketData.onTimer(0, 0);
        msg = fixConnector.get();
        Assert.assertEquals(1, msg.getValueAsInt(FixTags.NoMDEntries));

        // 100 - 102.0 x 0
        // 100 - 101.0 x 0
        // 100 - 100.0 x 0
        // 100 -  99.0 x 0
        sendPassiveOrder("TRADER", true, 100, "2Y", 102);
        marketData.onTimer(0, 0);
        msg = fixConnector.get();
        Assert.assertEquals(1, msg.getValueAsInt(FixTags.NoMDEntries));

        // 100 - 102.0 x 0
        // 100 - 101.0 x 0
        // 100 - 100.0 x 0
        // 100 -  99.0 x 0
        // 100 -  98.0 x 0
        sendPassiveOrder("TRADER", true, 100, "2Y", 98);
        marketData.onTimer(0, 0);
        msg = fixConnector.get();
        Assert.assertEquals(1, msg.getValueAsInt(FixTags.NoMDEntries));

        // 100 - 102.0 x 0
        // 100 - 101.0 x 0
        // 100 - 100.0 x 0
        // 100 -  99.0 x 0
        // 100 -  98.0 x 0
        sendPassiveOrder("TRADER", true, 100, "2Y", 94);
        marketData.onTimer(0, 0);

        // 100 - 102.0 x 0
        // 100 - 101.0 x 0
        // 100 - 100.0 x 0
        // 100 -  99.0 x 0
        // 100 -  98.0 x 0
        sendPassiveOrder("TRADER", true, 100, "2Y", 92);
        marketData.onTimer(0, 0);
    }
    
    @Test
    public void testAddAndDelete()
	{
    	makeMarketDataRequest();
        sendBond("2Y");

        this.marketData.onFixMarketDataRequest();

        sendPassiveOrder("TRADER", true, 10, "2Y", 1);
        verifySingle(New, Bid, 1, 10);

        sendPassiveOrder("TRADER", true, 11, "2Y", 2);
        verifySingle(New, Bid, 2, 11);

        sendPassiveOrder("TRADER", true, 12, "2Y", 3);
        verifySingle(New, Bid, 3, 12);
        
        sendPassiveOrder("TRADER", true, 13, "2Y", 4);
        verifySingle(New, Bid, 4, 13);

        sendPassiveOrder("TRADER", true, 14, "2Y", 5);
        verifySingle(New, Bid, 5, 14);
        
        sendPassiveOrder("TRADER", true, 15, "2Y", 6);
        marketData.onTimer(0, 0);

        FixMessage msg = fixConnector.get();
        Assert.assertEquals(1, msg.getValueAsInt(FixTags.MDReqID));
        Assert.assertEquals(2, msg.getValueAsInt(FixTags.NoMDEntries));

        Assert.assertEquals(New, msg.getValueAsChar(FixTags.MDUpdateAction, 0));
        Assert.assertEquals(Bid, msg.getValueAsChar(FixTags.MDEntryType, 0));
        Assert.assertEquals("2Y", msg.getValueAsString(FixTags.Symbol, 0));
        Assert.assertEquals(6, msg.getValueAsPrice(FixTags.MDEntryPx, 0), 0.01);
        Assert.assertEquals(15 * QTY_MULTIPLIER, msg.getValueAsPrice(FixTags.MDEntrySize, 0), 0.01);

        Assert.assertEquals(Delete, msg.getValueAsChar(FixTags.MDUpdateAction, 1));
        Assert.assertEquals(Bid, msg.getValueAsChar(FixTags.MDEntryType, 1));
        Assert.assertEquals("2Y", msg.getValueAsString(FixTags.Symbol, 1));
        Assert.assertEquals(1, msg.getValueAsPrice(FixTags.MDEntryPx, 1), 0.01);
        Assert.assertEquals(10 * QTY_MULTIPLIER, msg.getValueAsPrice(FixTags.MDEntrySize, 1), 0.01);
	}
    
    @Test
    public void testMarketDataAggregated()
    {
        makeMarketDataRequest();
        sendBond("2Y");
        this.marketData.onFixMarketDataRequest();

        marketData.onTimer(0, 0);

        // new. B 100 @ 100.0
        // 100 - 100.0 x 0
        sendPassiveOrder("TRADER", true, 100, "2Y", 100);

        // new. S 100 @ 101.0
        // 100 - 100.0 x 101 - 100
        sendPassiveOrder("TRADER", false, 100, "2Y", 101);

        // update. S 150 @ 101.0
        // 100 - 100.0 x 101 - 150
        sendPassiveOrder("TRADER", false, 50, "2Y", 101);

        // new. B 100 @ 99.0
        // 100 - 100.0 x 101.0 - 150
        // 100 - 99.0
        sendPassiveOrder("TRADER", true, 100, "2Y", 99);

        // 100 - 100.0 x 100.5 - 100
        // 100 -  99.0 x 101.0 - 150
        sendPassiveOrder("TRADER", false, 100, "2Y", 100.5);


        // 100 - 100.0 x 100.5 - 150
        // 100 -  99.0 x 101.0 - 150
        sendPassiveOrder("TRADER", false, 50, "2Y", 100.5);

        marketData.onTimer(0, 0);
        FixMessage fixMessage = verifyMultiple(2, 2, 0);

        Assert.assertEquals(New, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 0));
        Assert.assertEquals(Bid, fixMessage.getValueAsChar(FixTags.MDEntryType, 0));
        Assert.assertEquals(100 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 0));
        Assert.assertEquals(100, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 0), 0.001);

        Assert.assertEquals(New, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 1));
        Assert.assertEquals(Bid, fixMessage.getValueAsChar(FixTags.MDEntryType, 1));
        Assert.assertEquals(100 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 1));
        Assert.assertEquals(99, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 1), 0.001);

        Assert.assertEquals(New, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 2));
        Assert.assertEquals(Offer, fixMessage.getValueAsChar(FixTags.MDEntryType, 2));
        Assert.assertEquals(150 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 2));
        Assert.assertEquals(100.5, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 2), 0.001);

        Assert.assertEquals(New, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 3));
        Assert.assertEquals(Offer, fixMessage.getValueAsChar(FixTags.MDEntryType, 2));
        Assert.assertEquals(150 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 3));
        Assert.assertEquals(101, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 3), 0.001);

        // 100 - 100.0 x 100.5 - 170
        // 100 -  99.0 x 101.0 - 150
        sendPassiveOrder("TRADER", false, 20, "2Y", 100.5);

        // 100 - 100.0 x 100.5 - 170
        // 120 -  99.0 x 101.0 - 150
        sendPassiveOrder("TRADER", true, 20, "2Y", 99);

        marketData.onTimer(0, 0);

        fixMessage = verifyMultiple(1, 1, 0);

        Assert.assertEquals(Change, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 0));
        Assert.assertEquals(Bid, fixMessage.getValueAsChar(FixTags.MDEntryType, 0));
        Assert.assertEquals(120 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 0));
        Assert.assertEquals(99, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 0), 0.001);

        Assert.assertEquals(Change, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 1));
        Assert.assertEquals(Offer, fixMessage.getValueAsChar(FixTags.MDEntryType, 1));
        Assert.assertEquals(170 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 1));
        Assert.assertEquals(100.5, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 1), 0.001);
    }

    
    @Test
    public void testTrade()
    {
        makeMarketDataRequest();
        sendBond("2Y");
        this.marketData.onFixMarketDataRequest();

        // passive order
        int orderID1 = sendPassiveOrder("TRADER", true, 100, "2Y", 100);

        verifySingle(New, Bid, 100, 100);

        // aggressive order
        int orderID2 = sendAggressiveOrder("TRADER", true, 50, "2Y", 100, false);

        sendMatch(true, orderID1, orderID2, 50, 100);

        marketData.onTimer(0, 0);
        FixMessage fixMessage = verifyMultiple(1, 0, 1);

        // bid
        Assert.assertEquals(Change, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 0));
        Assert.assertEquals(Bid, fixMessage.getValueAsChar(FixTags.MDEntryType, 0));
        Assert.assertEquals(50 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 0));
        Assert.assertEquals(100, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 0), 0.001);

        // trade
        Assert.assertEquals(New, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 1));
        Assert.assertEquals(Trade, fixMessage.getValueAsChar(FixTags.MDEntryType, 1));
        Assert.assertEquals(50 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 1));
        Assert.assertEquals(100, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 1), 0.001);
    }

    
    @Test
    public void testLotsOfTradesButOnlySend5()
    {
        makeMarketDataRequest();
        sendBond("2Y");
        this.marketData.onFixMarketDataRequest();

        // passive order
        int orderID1 = sendPassiveOrder("TRADER", true, 10, "2Y", 100);
        verifySingle(New, Bid, 100, 10);

        int orderID2 = sendPassiveOrder("TRADER", true, 20, "2Y", 100);
        verifySingle(Change, Bid, 100, 30);

        int orderID3 = sendPassiveOrder("TRADER", true, 30, "2Y", 100);
        verifySingle(Change, Bid, 100, 60);

        int orderID4 = sendPassiveOrder("TRADER", true, 40, "2Y", 100);
        verifySingle(Change, Bid, 100, 100);

        int orderID5 = sendPassiveOrder("TRADER", true, 50, "2Y", 100);
        verifySingle(Change, Bid, 100, 150);

        int orderID6 = sendPassiveOrder("TRADER", true, 60, "2Y", 100);
        verifySingle(Change, Bid, 100, 210);

        // aggressive order
        int orderIDAgg = sendAggressiveOrder("TRADER", false, 310, "2Y", 100, false);

        sendMatch(false, orderID1, orderIDAgg, 10, 100);
        sendMatch(false, orderID2, orderIDAgg, 20, 100);
        sendMatch(false, orderID3, orderIDAgg, 30, 100);
        sendMatch(false, orderID4, orderIDAgg, 40, 100);
        sendMatch(false, orderID5, orderIDAgg, 50, 100);
        sendMatch(true, orderID6, orderIDAgg, 60, 100);

        marketData.onTimer(0, 0);
        FixMessage fixMessage = verifyMultiple(1, 1, 5);

        Assert.assertEquals(Delete, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 0));
        Assert.assertEquals(Bid, fixMessage.getValueAsChar(FixTags.MDEntryType, 0));
        Assert.assertEquals(210 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 0));
        Assert.assertEquals(100, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 0), 0.001);

        Assert.assertEquals(New, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 1));
        Assert.assertEquals(Offer, fixMessage.getValueAsChar(FixTags.MDEntryType, 1));
        Assert.assertEquals(100 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 1));
        Assert.assertEquals(100, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 1), 0.001);

        Assert.assertEquals(New, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 2));
        Assert.assertEquals(Trade, fixMessage.getValueAsChar(FixTags.MDEntryType, 2));
        Assert.assertEquals(20 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 2));
        Assert.assertEquals(100, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 2), 0.001);

        Assert.assertEquals(New, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 3));
        Assert.assertEquals(Trade, fixMessage.getValueAsChar(FixTags.MDEntryType, 3));
        Assert.assertEquals(30 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 3));
        Assert.assertEquals(100, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 3), 0.001);

        Assert.assertEquals(New, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 4));
        Assert.assertEquals(Trade, fixMessage.getValueAsChar(FixTags.MDEntryType, 4));
        Assert.assertEquals(40 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 4));
        Assert.assertEquals(100, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 4), 0.001);

        Assert.assertEquals(New, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 5));
        Assert.assertEquals(Trade, fixMessage.getValueAsChar(FixTags.MDEntryType, 5));
        Assert.assertEquals(50 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 5));
        Assert.assertEquals(100, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 5), 0.001);

        Assert.assertEquals(New, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 6));
        Assert.assertEquals(Trade, fixMessage.getValueAsChar(FixTags.MDEntryType, 6));
        Assert.assertEquals(60 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 6));
        Assert.assertEquals(100, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 6), 0.001);
    }

    
    @Test
    public void testLastFillInSeparatePacket()
    {
        makeMarketDataRequest();
        sendBond("2Y");
        this.marketData.onFixMarketDataRequest();

        // passive order
        int orderID1 = sendPassiveOrder("TRADER", true, 10, "2Y", 100);
        verifySingle(New, Bid, 100, 10);

        int orderID2 = sendPassiveOrder("TRADER", true, 20, "2Y", 100);
        verifySingle(Change, Bid, 100, 30);

        int orderID3 = sendPassiveOrder("TRADER", true, 30, "2Y", 100);
        verifySingle(Change, Bid, 100, 60);

        int orderID4 = sendPassiveOrder("TRADER", true, 40, "2Y", 100);
        verifySingle(Change, Bid, 100, 100);

        int orderID5 = sendPassiveOrder("TRADER", true, 50, "2Y", 100);
        verifySingle(Change, Bid, 100, 150);

        int orderID6 = sendPassiveOrder("TRADER", true, 60, "2Y", 100);
        verifySingle(Change, Bid, 100, 210);

        // aggressive order
        int orderIDA = sendAggressiveOrder("TRADER", false, 310, "2Y", 100, false);
        sendMatch(false, orderID1, orderIDA, 10, 100);
        sendMatch(false, orderID2, orderIDA, 20, 100);
        sendMatch(false, orderID3, orderIDA, 30, 100);
        marketData.onTimer(0, 0);

        FixMessage fixMessage = verifyMultiple(1, 0, 3);

        Assert.assertEquals(Change, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 0));
        Assert.assertEquals(Bid, fixMessage.getValueAsChar(FixTags.MDEntryType, 0));
        Assert.assertEquals(150 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 0));
        Assert.assertEquals(100, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 0), 0.001);

        Assert.assertEquals(New, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 1));
        Assert.assertEquals(Trade, fixMessage.getValueAsChar(FixTags.MDEntryType, 1));
        Assert.assertEquals(10 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 1));
        Assert.assertEquals(100, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 1), 0.001);

        Assert.assertEquals(New, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 2));
        Assert.assertEquals(Trade, fixMessage.getValueAsChar(FixTags.MDEntryType, 2));
        Assert.assertEquals(20 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 2));
        Assert.assertEquals(100, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 2), 0.001);

        Assert.assertEquals(New, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 3));
        Assert.assertEquals(Trade, fixMessage.getValueAsChar(FixTags.MDEntryType, 3));
        Assert.assertEquals(30 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 3));
        Assert.assertEquals(100, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 3), 0.001);

        //match(100, 40, orderID4, orderIDA, false);
        sendMatch(false, orderID4, orderIDA, 40, 100);
        sendMatch(false, orderID5, orderIDA, 50, 100);
        sendMatch(true, orderID6, orderIDA, 60, 100);
        marketData.onTimer(0, 0);

        fixMessage = verifyMultiple(1, 1, 3);

        Assert.assertEquals(Delete, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 0));
        Assert.assertEquals(Bid, fixMessage.getValueAsChar(FixTags.MDEntryType, 0));
        Assert.assertEquals(150 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 0));
        Assert.assertEquals(100, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 0), 0.001);

        Assert.assertEquals(New, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 1));
        Assert.assertEquals(Offer, fixMessage.getValueAsChar(FixTags.MDEntryType, 1));
        Assert.assertEquals(100 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 1));
        Assert.assertEquals(100, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 1), 0.001);

        Assert.assertEquals(New, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 2));
        Assert.assertEquals(Trade, fixMessage.getValueAsChar(FixTags.MDEntryType, 2));
        Assert.assertEquals(40 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 2));
        Assert.assertEquals(100, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 2), 0.001);

        Assert.assertEquals(New, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 3));
        Assert.assertEquals(Trade, fixMessage.getValueAsChar(FixTags.MDEntryType, 3));
        Assert.assertEquals(50 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 3));
        Assert.assertEquals(100, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 3), 0.001);

        Assert.assertEquals(New, fixMessage.getValueAsChar(FixTags.MDUpdateAction, 4));
        Assert.assertEquals(Trade, fixMessage.getValueAsChar(FixTags.MDEntryType, 4));
        Assert.assertEquals(60 * QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.MDEntrySize, 4));
        Assert.assertEquals(100, fixMessage.getValueAsPrice(FixTags.MDEntryPx, 4), 0.001);
    }
}

