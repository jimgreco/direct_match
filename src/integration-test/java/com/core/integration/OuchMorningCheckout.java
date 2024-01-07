package com.core.integration;

import com.core.match.msgs.MatchConstants;
import com.core.match.ouch.client.OUCHClientOrder;
import com.core.nio.SelectorService;
import com.core.testharness.ouch.CountDownLatchEventCounter;
import com.core.testharness.ouch.EventCounter;
import com.core.testharness.ouch.OuchTestClient;
import com.core.testharness.ouch.OuchTestClient.OUCHFillHolder;
import com.core.util.PriceUtils;
import com.core.util.log.SystemOutLog;
import com.core.util.time.SystemTimeSource;
import com.gs.collections.api.iterator.LongIterator;
import com.gs.collections.impl.list.mutable.primitive.LongArrayList;
import org.junit.*;

import java.io.IOException;

/**
 * Created by hli on 11/9/15.G
 */

public class OuchMorningCheckout
{
	private static OuchTestClient testClient;
	private static String trader = "HLI";
	private static String security="2Y";

	@BeforeClass
	public static void setUp() throws IOException
	{
		SystemTimeSource timeSource = new SystemTimeSource();
		SystemOutLog log = new SystemOutLog("VM01-1", "TEST01", timeSource, 10);
		SelectorService select = new SelectorService(log, timeSource);
		EventCounter counter = new CountDownLatchEventCounter();
		log.setDebug(true);
		testClient = new OuchTestClient(
				"10.9.11.27",
				6005,
				"OUCHUN",
				"OUCHPW",
				1000000,
				select,
				timeSource,
				log,
				counter);
		Thread thread = new Thread(select::run);
		thread.start();
	}

	@Before
	public void setupTest() throws InterruptedException
	{

		testClient.waitLogin();
	}
	
	@AfterClass
	public static void cleanUp()
	{
		try
		{
			testClient.close();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	@Test
	public void sendOrder_receiveAcceptMessage() throws IOException, InterruptedException
	{
		testClient.expectAccepted(1);
		long id = testClient.sendNewOrder(security, true, 10, 10, trader, false);
		testClient.waitAccept();
		
		OUCHClientOrder orderAccept = testClient.getAcceptedOrder(id);
		Assert.assertNotNull(orderAccept);
		Assert.assertEquals(trader, orderAccept.getTrader());
		Assert.assertEquals(orderAccept.getQty(), 10 * MatchConstants.QTY_MULTIPLIER);
		
		testClient.expectCanceled(1);
		testClient.cancelOrder(id);
		testClient.waitCancel();
		OUCHClientOrder cancelOrder = testClient.getCancelOrder(id);
		Assert.assertNotNull(cancelOrder);
	}

	@Ignore(value = "till we add spread instrument into the system")
	@Test
	public void sendTwoBuySellOrder_spread_receiveAcceptMessageAndFills() throws IOException, InterruptedException
	{
		testClient.expectAccepted(1);
		long id = testClient.sendNewOrder("2Y10Y", true, 10, -0.95, trader, false);
		testClient.waitAccept();

		testClient.expectAccepted(1);
		long sell = testClient.sendNewOrder("2Y10Y", false, 10, -0.95, trader, false);
		testClient.waitAccept();

		OUCHClientOrder orderAccept = testClient.getAcceptedOrder(id);
		Assert.assertNotNull(orderAccept);
		Assert.assertEquals(trader, orderAccept.getTrader());
		Assert.assertEquals(orderAccept.getQty(), 10 * MatchConstants.QTY_MULTIPLIER);
		Assert.assertEquals(PriceUtils.toLong(-0.95,MatchConstants.IMPLIED_DECIMALS), orderAccept.getPrice());

		testClient.expectFills(2);
		testClient.waitFill();
		OUCHFillHolder fill = testClient.getFill();
		Assert.assertEquals(10 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQuantity());
		fill = testClient.getFill();
		Assert.assertEquals(10 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQuantity());
	}

	@Ignore(value = "till we add spread instrument into the system")
	@Test
	public void sendOrder_spread_receiveAcceptMessage() throws IOException, InterruptedException
	{
		testClient.expectAccepted(1);
		long id = testClient.sendNewOrder("2Y10Y", true, 10, -0.95, trader, false);
		testClient.waitAccept();

		OUCHClientOrder orderAccept = testClient.getAcceptedOrder(id);
		Assert.assertNotNull(orderAccept);
		Assert.assertEquals(trader, orderAccept.getTrader());
		Assert.assertEquals(orderAccept.getQty(), 10 * MatchConstants.QTY_MULTIPLIER);
		Assert.assertEquals(PriceUtils.toLong(-0.95,MatchConstants.IMPLIED_DECIMALS), orderAccept.getPrice());
	}
	
	@Test
	public void testIONScenarioAggressingWorksLikeResting() throws IOException, InterruptedException
	{
		testClient.expectAccepted(1);
		long id = testClient.sendNewOrder(security, true, 5, 40, trader, false);
		testClient.waitAccept();
		
		OUCHClientOrder orderAccept = testClient.getAcceptedOrder(id);
		Assert.assertNotNull(orderAccept);
		Assert.assertEquals(trader, orderAccept.getTrader());
		Assert.assertEquals(orderAccept.getQty(), 5 * MatchConstants.QTY_MULTIPLIER);
		
		testClient.getFillQueue().clear();
		testClient.expectAccepted(1);
		long id2 = testClient.sendNewOrder(security, false, 8, 40, trader, false);
		testClient.waitAccept();
		
		testClient.expectFills(4);
		testClient.waitFill();
		OUCHFillHolder fill = testClient.getFill();
		Assert.assertEquals(5 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQuantity());
		fill = testClient.getFill();
		Assert.assertEquals(5 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQuantity());

		testClient.expectCanceled(1);
		testClient.cancelOrder(id2);
		testClient.waitCancel();
		
		OUCHClientOrder cancelOrder = testClient.getCancelOrder(id2);
		Assert.assertNotNull(cancelOrder);
	}
	
	@Test
	public void send100Order_receiveAcceptMessage() throws IOException, InterruptedException
	{
		LongArrayList list = new LongArrayList(); 
		
		testClient.expectAccepted(100);
		for (int i=0;i<100;i++)
		{
			long id = testClient.sendNewOrder(security, true, 10, 10, trader, false);
			list.add(id); 
		}

		testClient.waitAccept();
		
		LongIterator longIterator = list.longIterator();
		
		while( longIterator.hasNext() )
		{
			long id = longIterator.next(); 
			OUCHClientOrder orderAccept = testClient.getAcceptedOrder(id);
			Assert.assertNotNull(orderAccept);	
		}
		
		testClient.expectCanceled(100);
		longIterator = list.longIterator();
		while( longIterator.hasNext())
		{
			long id = longIterator.next(); 
			testClient.cancelOrder(id);
		}

		testClient.waitCancel();
		longIterator = list.longIterator();
		while( longIterator.hasNext()) 
		{
			long id = longIterator.next();
			OUCHClientOrder cancelOrder = testClient.getCancelOrder(id);
			Assert.assertNotNull(cancelOrder);
		}
	}

	@Test
	public void sendNewOrder_sendOppositeSideSmallerSize_smallerOrderFullyFilled() throws IOException, InterruptedException
	{
		testClient.expectAccepted(1);
		long id = testClient.sendNewOrder(security, true, 10, 60, trader, false);

		testClient.waitAccept();

		OUCHClientOrder orderAccept = testClient.getAcceptedOrder(id);
		Assert.assertNotNull(orderAccept);
		Assert.assertEquals(trader, orderAccept.getTrader());

		testClient.expectAccepted(1);
		testClient.expectFills(4);
		testClient.getFillQueue().clear();
		testClient.sendNewOrder(security, false, 7, 60, trader, false);

		testClient.waitAccept();
		testClient.waitFill();
		
		OUCHFillHolder fill = testClient.getFill();
		Assert.assertEquals(7 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQuantity());
		fill = testClient.getFill();
		Assert.assertEquals(7 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQuantity());

		testClient.expectCanceled(1);
		testClient.cancelOrder(id);
		testClient.waitCancel();
		OUCHClientOrder cancelOrder = testClient.getCancelOrder(id);
		Assert.assertNotNull(cancelOrder);
	}

	@Test
	public void sendReplace_twoReplaces_receiveCorrectReplacedMessages() throws IOException, InterruptedException
	{
		testClient.expectAccepted(1);
		long clOrdID = testClient.sendNewOrder(security, true, 10, 30, trader, false);
		testClient.waitAccept();
		
		OUCHClientOrder orderAccept = testClient.getAcceptedOrder(clOrdID);
		Assert.assertNotNull(orderAccept);

		testClient.expectReplaced(1);
		long replaceId = testClient.replaceOrder(clOrdID, 5, 30);
		testClient.waitReplace();
		
		OUCHClientOrder orderReplaced = testClient.getReplaceOrder(replaceId);
		Assert.assertNotNull(orderReplaced);

		testClient.expectReplaced(1);
		replaceId = testClient.replaceOrder(replaceId, 4, 30);
		testClient.waitReplace();
		
		orderReplaced = testClient.getReplaceOrder(replaceId);
		Assert.assertNotNull(orderReplaced);
		
		testClient.expectAccepted(1);
		testClient.expectFills(2);
		
		long id = testClient.sendNewOrder(security, false, 10, 30, trader, false);
		testClient.getFillQueue().clear();
		testClient.waitAccept();
		testClient.waitFill();
		
		// should see fills of 4, 4, 2
		OUCHFillHolder fill = testClient.getFill();
		Assert.assertEquals(4 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQuantity());
		fill = testClient.getFill();
		Assert.assertEquals(4 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQuantity());
		fill = testClient.getFill();
		
		testClient.expectCanceled(1);
		testClient.cancelOrder(id);
		testClient.waitCancel();
		OUCHClientOrder cancelOrder = testClient.getCancelOrder(id);
		Assert.assertNotNull(cancelOrder);
	}

	@Test
	public void sendNewOrder_crossed_generateTwoFills() throws IOException, InterruptedException
	{
		testClient.expectAccepted(1);
		long id = testClient.sendNewOrder(security, true, 10, 20, trader, false);
		testClient.waitAccept();
		
		OUCHClientOrder orderAccept = testClient.getAcceptedOrder(id);
		Assert.assertNotNull(orderAccept);
		Assert.assertEquals(trader, orderAccept.getTrader());
		
		testClient.expectAccepted(1);
		testClient.expectFills(10);
		testClient.getFillQueue().clear();
		testClient.sendNewOrder(security, false, 5, 20, trader, false);
		testClient.waitAccept();
		testClient.waitFill();
		
		OUCHFillHolder fill = testClient.getFill();

		Assert.assertEquals(5 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQuantity());
		fill = testClient.getFill();
		Assert.assertEquals(5 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQuantity());
		fill = testClient.getFill();

		
		testClient.expectCanceled(1);
		testClient.cancelOrder(id);
		testClient.waitCancel();
		
		OUCHClientOrder cancelOrder = testClient.getCancelOrder(id);
		Assert.assertNotNull(cancelOrder);
	}

	
	@Test
	public void testLosingQueuePriorityOnSizeIncrease() throws IOException, InterruptedException
	{
		testClient.expectAccepted(1);
		long clOrdID = testClient.sendNewOrder(security, true, 10, 10, trader, false);
		testClient.waitAccept();
		
		OUCHClientOrder orderAccept = testClient.getAcceptedOrder(clOrdID);
		Assert.assertNotNull(orderAccept);

		testClient.expectAccepted(1);
		long clOrdID2 = testClient.sendNewOrder(security, true, 10, 10, trader, false);
		testClient.waitAccept();
		
		orderAccept = testClient.getAcceptedOrder(clOrdID2);
		Assert.assertNotNull(orderAccept);

		testClient.expectReplaced(1);
		long replaceId = testClient.replaceOrder(clOrdID, 20,  10);
		testClient.waitReplace();

		OUCHClientOrder orderReplaced = testClient.getReplaceOrder(replaceId);
		Assert.assertNotNull(orderReplaced);
		
		testClient.expectAccepted(1);
		testClient.expectFills(2);
		testClient.sendNewOrder(security, false, 6, 10, trader, false);
		
		testClient.waitAccept();
		testClient.waitFill();

		OUCHFillHolder fill = testClient.getFill();
		Assert.assertEquals(6 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQuantity());
		fill = testClient.getFill();
		Assert.assertEquals(6 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQuantity());
		
		testClient.expectCanceled(1);
		testClient.cancelOrder(replaceId);
		testClient.waitCancel();
		OUCHClientOrder cancelOrder = testClient.getCancelOrder(replaceId);
		Assert.assertNotNull(cancelOrder);
	}


	@Test
	public void sendNewOrders_receiveOrderAcceptMessage() throws IOException, InterruptedException
	{
		testClient.expectAccepted(1);
		int qty = 14;
		long id = testClient.sendNewOrder(security, true, qty, 10, trader, false);
		testClient.waitAccept();

		OUCHClientOrder orderAccept = testClient.getAcceptedOrder(id);
		Assert.assertNotNull(orderAccept);
		Assert.assertEquals(trader, orderAccept.getTrader());
		
		testClient.expectCanceled(1);
		testClient.cancelOrder(id);
		testClient.waitCancel();
		OUCHClientOrder cancelOrder = testClient.getCancelOrder(id);
		Assert.assertNotNull(cancelOrder);


	}

	@Test
	public void sendNewOrderAndReplaceIt_receiveOrderAcceptMessage() throws IOException, InterruptedException
	{
		testClient.expectAccepted(1);
		int qty = 1;
		long id = testClient.sendNewOrder(security, true, qty, 50, trader, false);
		testClient.waitAccept();
		
		OUCHClientOrder orderAccept = testClient.getAcceptedOrder(id);
		Assert.assertNotNull(orderAccept);

		testClient.expectReplaced(1);
		int newQty = 2;
		long replaceId = testClient.replaceOrder(id, newQty, 100);
		testClient.waitReplace();
		
		OUCHClientOrder orderReplaced = testClient.getReplaceOrder(replaceId);
		Assert.assertNotNull(orderReplaced);
		
		testClient.expectCanceled(1);
		testClient.cancelOrder(replaceId);
		testClient.waitCancel();
		OUCHClientOrder cancelOrder = testClient.getCancelOrder(replaceId);
		Assert.assertNotNull(cancelOrder);
		

	}

	@Test
	public void sendNewOrderAndCancelIt_receiveOrderAcceptMessageAndCancelMessage() throws IOException, InterruptedException
	{
		testClient.expectAccepted(1);
		int qty = 1;
		// Need to set a ridic low price so as to minimize chance of a fill so that we can cancel
		long id = testClient.sendNewOrder(security, true, qty, 1, trader, false);
		testClient.waitAccept();
		
		OUCHClientOrder orderAccept = testClient.getAcceptedOrder(id);
		Assert.assertNotNull(orderAccept);

		testClient.expectCanceled(1);
		testClient.cancelOrder(id);
		testClient.waitCancel();
		
		OUCHClientOrder cancelOrder = testClient.getCancelOrder(id);
		Assert.assertNotNull(cancelOrder);
	}

	@Test
	public void sendNewOrders_QtyExceedsFFCheck_receiveOrderRejectMessage() throws IOException, InterruptedException
	{
		testClient.expectRejected(1);
		int qty = 50000000;
		long id = testClient.sendNewOrder(security, true, qty, 100, trader, false);
		testClient.waitReject();
		boolean receiveRejects = testClient.getRejectedOrder(id);
		Assert.assertTrue(receiveRejects);
	}

	@Test
	public void sendNewIOCOrders_receiveCancel_willNotGetFilledBecausePriceRidicLow() throws IOException, InterruptedException
	{
		testClient.getFillQueue().clear();
		testClient.expectAccepted(1);
		// Because price is so low and order is cancelled right away. So get a Accept and Cancel
		int qty = 1;
		long id = testClient.sendNewOrder(security, true, qty, 5, trader, true);
		testClient.waitAccept();
				
		OUCHClientOrder orderAccept = testClient.getAcceptedOrder(id);
		Assert.assertNotNull(orderAccept);
	}

}
