package com.core.match.itch;


import com.core.match.itch.msgs.ITCHCommonCommand;
import com.core.match.itch.msgs.ITCHConstants;
import com.core.match.itch.msgs.ITCHOrderCancelCommand;
import com.core.match.itch.msgs.ITCHOrderCancelEvent;
import com.core.match.itch.msgs.ITCHOrderCommand;
import com.core.match.itch.msgs.ITCHOrderEvent;
import com.core.match.itch.msgs.ITCHOrderExecutedCommand;
import com.core.match.itch.msgs.ITCHOrderExecutedEvent;
import com.core.match.itch.msgs.ITCHSecurityCommand;
import com.core.match.itch.msgs.ITCHSecurityEvent;
import com.core.match.itch.msgs.ITCHSystemCommand;
import com.core.match.itch.msgs.ITCHSystemEvent;
import com.core.match.itch.msgs.ITCHTestMessages;
import com.core.match.msgs.MatchConstants;
import com.core.match.services.order.DisplayedOrder;
import com.core.match.services.order.DisplayedOrderService;
import com.core.match.services.order.DisplayedOrderServiceTestBase;
import com.core.util.PriceUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jgreco on 6/15/15.
 */
public class ITCHMessageServiceTest extends DisplayedOrderServiceTestBase {
    private StubITCHListener listener;

	@SuppressWarnings("unused")
	@Override
	@Before
    public void before() {
        listener = new StubITCHListener();

        DisplayedOrderService<DisplayedOrder> orderService = new DisplayedOrderService<>(orders, log);

        new ITCHMessageService(
                new ITCHTestMessages(),
                orderService,
                securities,
                systemEventService,
                listener);

        dispatcher.subscribe(orders);
    }

    @After
    public void after() {
        verifyNone();
    }

    @Test
    public void testOpen() {
        openMarket();

        ITCHSystemEvent se = listener.pop(ITCHSystemCommand.class).toEvent();
        Assert.assertEquals(0, se.getSecurityID());
        Assert.assertEquals(ITCHConstants.EventCode.StartOfTradingSession, se.getEventCode());
    }

    @Test
    public void testClose() {
        openMarket();
        listener.pop(ITCHCommonCommand.class);

        closeMarket();
        ITCHSystemEvent se = listener.pop(ITCHSystemCommand.class).toEvent();
        Assert.assertEquals(0, se.getSecurityID());
        Assert.assertEquals(ITCHConstants.EventCode.EndOfTradingSession, se.getEventCode());
    }

    @Test
    public void testSecurity() {
        sendSecurity("10Y", 2.5, 20250815, 20150815);
        verifySecurity("10Y");
    }

    @Test
    public void testBuy() {
        setupStatics();

        int orderID = sendPassiveOrder("TRADER", true, 10, "10Y", 100.0);
        verifyOrder(1, true, 10, "10Y", 100.0);
    }

    @Test
    public void testSell() {
        setupStatics();

        int orderID = sendPassiveOrder("TRADER", false, 5, "10Y", 101.0);
        verifyOrder(orderID, false, 5, "10Y", 101.0);
    }

    @Test
    public void testAggressiveOrder() {
        setupStatics();

        sendAggressiveOrder("TRADER", true, 10, "10Y", 100.0, false);
        verifyNone();
    }

    @Test
    public void testIOC() {
        setupStatics();

        sendAggressiveOrder("TRADER", true, 10, "10Y", 100.0, true);
        verifyNone();
    }

    @Test
    public void testCancel() {
        setupStatics();

        int orderID = sendPassiveOrder("TRADER", false, 5, "10Y", 101.0);
        verifyOrder(1, false, 5, "10Y", 101.0);

        sendCancel(orderID);
        verifyCancel(1, 5);
    }

    @Test
    public void testCancelAfterPartiallyFilled() {
        setupStatics();

        int orderID = sendPassiveOrder("TRADER", false, 5, "10Y", 101.0);
        verifyOrder(1, false, 5, "10Y", 101.0);

        sendFill(false, orderID, 2, 101.0);
        verifyFill(1, 2, 101.0);

        sendFill(false, orderID, 1, 101.0);
        verifyFill(1, 1, 101.0);

        sendCancel(orderID);
        verifyCancel(1, 2);
    }

    @Test
    public void testCancelAfterFullyFilled() {
        setupStatics();

        int orderID = sendPassiveOrder("TRADER", false, 5, "10Y", 101.0);
        verifyOrder(1, false, 5, "10Y", 101.0);

        sendFill(false, orderID, 3, 101.0);
        verifyFill(1, 3, 101.0);

        sendFill(false, orderID, 2, 101.0);
        verifyFill(1, 2, 101.0);

        sendCancel(orderID);
        verifyNone();
    }

    @Test
    public void testOverfill() {
        setupStatics();

        int orderID = sendPassiveOrder("TRADER", false, 10, "10Y", 101.0);
        verifyOrder(1, false, 10, "10Y", 101.0);

        // TODO: NOT SURE IF I LIKE THIS BEHAVIOR!
        sendFill(false, orderID, 11, 101.0);
        verifyFill(1, 11, 101.0);
    }

    @Test
    public void testReplaceDown() {
        setupStatics();

        int orderID = sendPassiveOrder("TRADER", false, 10, "10Y", 100.0);
        verifyOrder(1, false, 10, "10Y", 100.0);

        sendPassiveReplace(orderID, 3, 100.0);
        verifyCancel(1, 7);
    }

    private int verifyOrder(int id, boolean side, int qty, String security, double price) {
        ITCHOrderEvent se = listener.pop(ITCHOrderCommand.class).toEvent();
        Assert.assertEquals(id, se.getOrderID());
        Assert.assertEquals(getSecurityID(security), se.getSecurityID());
        Assert.assertEquals(PriceUtils.toLong(price, MatchConstants.IMPLIED_DECIMALS), se.getPrice());
        Assert.assertEquals(MatchConstants.QTY_MULTIPLIER * qty, se.getQty());
        Assert.assertEquals(side ? 'B' : 'S', se.getSide());
        return se.getOrderID();
    }
    
    private void verifyCancel(int id, int qtyCanceled) {
        ITCHOrderCancelEvent se = listener.pop(ITCHOrderCancelCommand.class).toEvent();
        Assert.assertEquals(id, se.getOrderID());
        Assert.assertEquals(MatchConstants.QTY_MULTIPLIER * qtyCanceled, se.getQtyCanceled());
    }

    private void verifyFill(int id, int qty, double price) {
        ITCHOrderExecutedEvent se = listener.pop(ITCHOrderExecutedCommand.class).toEvent();
        Assert.assertEquals(id, se.getOrderID());
        Assert.assertEquals(MatchConstants.QTY_MULTIPLIER * qty, se.getQty());
        Assert.assertEquals(PriceUtils.toLong(price, MatchConstants.IMPLIED_DECIMALS), se.getPrice());
    }

    private void setupStatics() {
        sendSecurity("10Y", 2.5, 20250815, 20150815);
        verifySecurity("10Y");


        sendAccount("ACCOUNT");
        sendTrader("TRADER", "ACCOUNT");
    }

    private void verifySecurity(String name) {
        ITCHSecurityEvent se = listener.pop(ITCHSecurityCommand.class).toEvent();
        Assert.assertEquals(1, se.getSecurityID());
        Assert.assertEquals(name, se.getNameAsString());
        Assert.assertEquals(2.5, se.getCouponAsDouble(), 0.001);
        Assert.assertEquals(20250815, se.getMaturityDate());
        Assert.assertEquals(ITCHConstants.SecurityType.TreasuryNote, se.getSecurityType());
        Assert.assertEquals("10Y_CUSIP", se.getSecurityReferenceAsString());
    }

    private void verifyNone() {
        Assert.assertEquals(0, listener.size());
    }
}
