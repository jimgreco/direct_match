package com.core.match.fix.mdclient;

import com.core.fix.FixMessage;
import com.core.fix.msgs.FixMsgTypes;
import com.core.fix.msgs.FixTags;
import com.core.match.fix.FixOrder;
import com.core.match.fix.GenericFIXAppTest;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchInboundCommand;
import com.core.match.msgs.MatchQuoteCommand;
import com.core.match.msgs.MatchQuoteEvent;
import com.core.match.msgs.MatchSecurityCommand;
import com.core.match.util.MatchPriceUtils;
import com.core.util.PriceUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by johnlevidy on 8/31/15.
 */
public class FIXMarketDataClientTest extends GenericFIXAppTest<FixOrder> {

    private FIXMarketDataClient client;

    public FIXMarketDataClientTest() {
        super(FixOrder.class, 0, "SEND01", "TARGET01");
    }

    @Override
	@Before
    public void before() throws IOException
    {
        this.client = new FIXMarketDataClient(this.log, this.timeSource, this.timers, this.dispatcher, this.sender, this.msgs, this.fixConnector, this.fixParser, this.fixWriter, this.fixStore, this.fixDispatcher, this.fixInfo);
        
        
        MatchSecurityCommand matchSecurityCommand = this.msgs.getMatchSecurityCommand();
        matchSecurityCommand.setTickSize(PriceUtils.getQuarter(MatchConstants.IMPLIED_DECIMALS));
        matchSecurityCommand.setSecurityID((short) 1);
        matchSecurityCommand.setName("2Y");
        matchSecurityCommand.setType(MatchConstants.SecurityType.TreasuryBond);
        matchSecurityCommand.setCUSIP("2YCUSIP");
        matchSecurityCommand.setMaturityDate(20161015);
        matchSecurityCommand.setIssueDate(20150915);
        matchSecurityCommand.setCouponFrequency((byte) 4);
        
        this.dispatcher.dispatch(matchSecurityCommand);
        
        matchSecurityCommand = this.msgs.getMatchSecurityCommand();
        matchSecurityCommand.setTickSize(PriceUtils.getPlus(MatchConstants.IMPLIED_DECIMALS));
        matchSecurityCommand.setSecurityID((short) 2);
        matchSecurityCommand.setName("10Y");
        matchSecurityCommand.setCUSIP("10YCUSIP");
        matchSecurityCommand.setMaturityDate(20161015);
        matchSecurityCommand.setIssueDate(20150915);
        matchSecurityCommand.setCouponFrequency((byte) 4);
        matchSecurityCommand.setType(MatchConstants.SecurityType.TreasuryBond);


        this.dispatcher.dispatch(matchSecurityCommand);
        client.setActive();
    }
    
    @Test
    public void testBasicLogon() {
    	this.sender.setDontDispatch(true);
        this.client.onConnect();
        FixMessage fixMessage = this.fixConnector.get();
        assertNull(fixMessage);
        this.sender.dequeue();
        
        // should see an attempt at writing a fix message
        fixMessage = this.fixConnector.get(0);
        Assert.assertEquals(60, fixMessage.getValueAsInt(FixTags.HeartBtInt));
        Assert.assertEquals('Y', fixMessage.getValueAsChar(FixTags.ResetSeqNumFlag));
        Assert.assertEquals(0, fixMessage.getValueAsInt(FixTags.EncryptMethod));
        Assert.assertEquals('A', fixMessage.getValueAsChar(FixTags.MsgType));

//        this.client.onFixLogon();
        
        MatchInboundCommand msg = msgs.getMatchInboundCommand(); 
        msg.setFixMsgType(FixMsgTypes.Logon);
        msg.setContributorID((short) 0);
        this.client.onMatchInbound(msg.toEvent());

        fixMessage = this.fixConnector.get(1);
        Assert.assertEquals('V', fixMessage.getValueAsChar(FixTags.MsgType));
        Assert.assertEquals("2Y", fixMessage.getValueAsString(FixTags.MDReqID));
        Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.SubscriptionRequestType));
        Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.MarketDepth));
        Assert.assertEquals(2, fixMessage.getValueAsInt(FixTags.NoMDEntryTypes));
        Assert.assertEquals('0', fixMessage.getValueAsChar(FixTags.MDEntryType, 0));
        Assert.assertEquals('1', fixMessage.getValueAsChar(FixTags.MDEntryType, 1));
        Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.NoRelatedSym));
        
        fixMessage = this.fixConnector.get(2);
        Assert.assertEquals('V', fixMessage.getValueAsChar(FixTags.MsgType));
        Assert.assertEquals("10Y", fixMessage.getValueAsString(FixTags.MDReqID));
        Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.SubscriptionRequestType));
        Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.MarketDepth));
        Assert.assertEquals(2, fixMessage.getValueAsInt(FixTags.NoMDEntryTypes));
        Assert.assertEquals('0', fixMessage.getValueAsChar(FixTags.MDEntryType, 0));
        Assert.assertEquals('1', fixMessage.getValueAsChar(FixTags.MDEntryType, 1));
        Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.NoRelatedSym));

    }
    
    @Test
    public void testMarketDataRequestMessageAfterDisconnect()
	{
    	this.sender.setDontDispatch(true);
        this.client.onConnect();
        FixMessage fixMessage = this.fixConnector.get();
        assertNull(fixMessage);
        this.sender.dequeue();
        
        fixMessage = this.fixConnector.get(0);
        Assert.assertEquals(60, fixMessage.getValueAsInt(FixTags.HeartBtInt));
        Assert.assertEquals('Y', fixMessage.getValueAsChar(FixTags.ResetSeqNumFlag));
        Assert.assertEquals(0, fixMessage.getValueAsInt(FixTags.EncryptMethod));
        Assert.assertEquals('A', fixMessage.getValueAsChar(FixTags.MsgType));
        
        MatchInboundCommand msg = msgs.getMatchInboundCommand(); 
        msg.setFixMsgType(FixMsgTypes.Logon);
        msg.setContributorID((short)0);
        this.client.onDisconnect();
        this.client.onMatchInbound(msg.toEvent());
       
        fixMessage = this.fixConnector.get(1);
        
        assertNull(fixMessage);
	}
    @Test
    public void testLogonMatchMessageReceivedAfterDisconnect()
	{
    	this.sender.setDontDispatch(true);
    	this.client.onConnect();
    	this.client.onDisconnect();
    	this.sender.dequeue();
    	
    	FixMessage fixMessage = this.fixConnector.get(0);
    	Assert.assertEquals('A', fixMessage.getMsgType());
    	
    	fixMessage = this.fixConnector.get(1);
    	assertNull(fixMessage);
	}

    @Test
    public void testGenerateQuote() 
    {
    	this.fixParser.writeTag(FixTags.MDReqID, "2Y");
        //this.fixParser.writeTag(FixTags.Symbol, "2Y");
        this.fixParser.writeTag(FixTags.SecurityID, "2YCUSIP");
        this.fixParser.writeTag(FixTags.SecurityIDSource, 1);
        this.fixParser.writeTag(FixTags.NoMDEntries, 2);
        this.fixParser.writeTag(FixTags.MDEntryType, '0');
        this.fixParser.writeTag(FixTags.MDEntryPx, "100");
        this.fixParser.writeTag(FixTags.MDEntryType, '1');
        this.fixParser.writeTag(FixTags.MDEntryTime, "10:30:45.222");
        this.fixParser.writeTag(FixTags.MDEntryPx, "101");
        this.fixParser.writeTag(FixTags.MDEntryTime, "10:30:45.222");

        this.client.onFixMarketDataSnapshot();
        
        MatchQuoteEvent message = this.sender.getMessage(MatchQuoteCommand.class).toEvent();
        
        long bidPrice = message.getBidPrice();
        Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), bidPrice);
        long offerPrice = message.getOfferPrice();
        Assert.assertEquals(101 * MatchPriceUtils.getPriceMultiplier(), offerPrice);
        Assert.assertEquals(1, message.getSecurityID());
        
        this.fixParser.writeTag(FixTags.MDReqID, "10Y");
        //this.fixParser.writeTag(FixTags.Symbol, "10Y");
        this.fixParser.writeTag(FixTags.SecurityID, "10YCUSIP");
        this.fixParser.writeTag(FixTags.SecurityIDSource, 1);
        this.fixParser.writeTag(FixTags.NoMDEntries, 2);
        this.fixParser.writeTag(FixTags.MDEntryType, '0');
        this.fixParser.writeTag(FixTags.MDEntryPx, "101");
        this.fixParser.writeTag(FixTags.MDEntryType, '1');
        this.fixParser.writeTag(FixTags.MDEntryTime, "10:30:46.333");
        this.fixParser.writeTag(FixTags.MDEntryPx, "102");
        this.fixParser.writeTag(FixTags.MDEntryTime, "10:30:46.333");
        
        this.client.onFixMarketDataSnapshot();        
        message = this.sender.getMessage(MatchQuoteCommand.class).toEvent();
        bidPrice = message.getBidPrice();
        Assert.assertEquals(101 * MatchPriceUtils.getPriceMultiplier(), bidPrice);
        offerPrice = message.getOfferPrice();
        Assert.assertEquals(102 * MatchPriceUtils.getPriceMultiplier(), offerPrice);
        Assert.assertEquals(2, message.getSecurityID());
    }

    @Test
    public void testBadQuote_WithEqualBidOffer_Dropped() {
        String price="100";
        this.fixParser.writeTag(FixTags.MDReqID, "2Y");
        //this.fixParser.writeTag(FixTags.Symbol, "2Y");
        this.fixParser.writeTag(FixTags.SecurityID, "2YCUSIP");
        this.fixParser.writeTag(FixTags.SecurityIDSource, 1);
        this.fixParser.writeTag(FixTags.NoMDEntries, 2);
        this.fixParser.writeTag(FixTags.MDEntryType, '0');
        this.fixParser.writeTag(FixTags.MDEntryPx, price);
        this.fixParser.writeTag(FixTags.MDEntryType, '1');
        this.fixParser.writeTag(FixTags.MDEntryTime, "10:30:45.222");
        this.fixParser.writeTag(FixTags.MDEntryPx, price);
        this.fixParser.writeTag(FixTags.MDEntryTime, "10:30:45.222");

        this.client.onFixMarketDataSnapshot();


        assertTrue(sender.senderSize()==0);

    }

    @Test
    public void testBadQuote_BidGreaterOffer_Dropped() {
        this.fixParser.writeTag(FixTags.MDReqID, "2Y");
        //this.fixParser.writeTag(FixTags.Symbol, "2Y");
        this.fixParser.writeTag(FixTags.SecurityID, "2YCUSIP");
        this.fixParser.writeTag(FixTags.SecurityIDSource, 1);
        this.fixParser.writeTag(FixTags.NoMDEntries, 2);
        this.fixParser.writeTag(FixTags.MDEntryType, '0');
        this.fixParser.writeTag(FixTags.MDEntryPx, "101");
        this.fixParser.writeTag(FixTags.MDEntryType, '1');
        this.fixParser.writeTag(FixTags.MDEntryTime, "10:30:45.222");
        this.fixParser.writeTag(FixTags.MDEntryPx, "99");
        this.fixParser.writeTag(FixTags.MDEntryTime, "10:30:45.222");

        this.client.onFixMarketDataSnapshot();


        assertTrue(sender.senderSize()==0);

    }

    }
