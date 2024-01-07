package com.core.match.services.order;

import com.core.match.msgs.MatchConstants;
import com.core.util.PriceUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jgreco on 10/11/15.
 */
public class DisplayedOrderServiceTest extends DisplayedOrderServiceTestBase {
    private StubDisplayedOrderServiceListener listener;

    @SuppressWarnings("unused")
    @Override
    @Before
    public void before() {
        listener = new StubDisplayedOrderServiceListener();

        DisplayedOrderService<DisplayedOrder> displayed = new DisplayedOrderService<>(DisplayedOrder.class, log, dispatcher, 100);
        displayed.addListener(listener);

        sendSecurity("10Y", 2.5, 20250815, 20150815);
        sendSecurity("5Y", 1.5, 20200830, 20150830);
        sendAccount("ACCOUNT");
        sendTrader("TRADER", "ACCOUNT");
    }

    @Test
    public void testNotDisplayed() {
        sendAggressiveOrder("TRADER", true, 10, "10Y", 100, false);
        verifyNone();
    }

    @Test
    public void testDisplayedOrder() {
        int orderID = sendPassiveOrder("TRADER", true, 5, "10Y", 100.0);
        verifyOrder(orderID, 1, true, "10Y", 100.0);
    }

    @Test
    public void testMultipleDisplayedOrder() {
        sendAggressiveOrder("FOO", true, 5, "10Y", 99.0, false);
        verifyNone();

        int orderID1 = sendPassiveOrder("TRADER", true, 10, "10Y", 100.0);
        verifyOrder(orderID1, 2, true, "10Y", 100.0);

        sendAggressiveOrder("FOO", false, 2, "5Y", 101.0, false);
        verifyNone();

        int orderID2 = sendPassiveOrder("TRADER", false, 3, "5Y", 100.0);
        verifyOrder(orderID2, 4, false, "5Y", 100.0);
    }

    @Test
    public void testCancelNotDisplayed() {
        int orderID = sendAggressiveOrder("TRADER", true, 10, "10Y", 100.0, false);
        sendCancel(orderID);
        verifyNone();
    }

    @Test
    public void testCancelDisplayed() {
        int orderID = sendPassiveOrder("TRADER", true, 10, "10Y", 100.0);
        verifyOrder(orderID, 1, true, "10Y", 100.0);

        sendCancel(orderID);
        verifyCancel(1, 1, 10);
    }

    @Test
    public void testMultipleCancelsDoesNotResendCancel() {
        int orderID = sendPassiveOrder("TRADER", true, 5, "10Y", 100.0);
        verifyOrder(orderID, 1, true, "10Y", 100.0);

        sendCancel(orderID);
        verifyCancel(1, 1, 5);

        sendCancel(orderID);
        verifyNone();
    }

    @Test
    public void testReplaceDown() {
        int orderID = sendPassiveOrder("TRADER", true, 5, "10Y", 100.0);
        verifyOrder(orderID, 1, true, "10Y", 100.0);

        sendPassiveReplace(orderID, 3, 100.0);
        verifyReduced(orderID, 1, 2);
    }

    @Test
    public void testReplaceUpPassive() {
        int orderID = sendPassiveOrder("TRADER", true, 5, "10Y", 100.0);
        verifyOrder(orderID, 1, true, "10Y", 100.0);

        sendPassiveReplace(orderID, 10, 100.0);
        verifyCancel(orderID, 1, 5);
        verifyOrder(orderID, 2, true, "10Y", 100.0);
    }

    @Test
    public void testReplaceUpAggressive() {
        int orderID = sendPassiveOrder("TRADER", true, 5, "10Y", 100.0);
        verifyOrder(orderID, 1, true, "10Y", 100.0);

        sendAggressiveReplace(orderID, 10, 100.0);
        verifyCancel(orderID, 1, 5);
        verifyNone();
    }

    @Test
    public void testReplaceThenCancel() {
        int orderID = sendPassiveOrder("TRADER", true, 5, "10Y", 100.0);
        verifyOrder(orderID, 1, true, "10Y", 100.0);

        sendPassiveReplace(orderID, 10, 100.0);
        verifyCancel(orderID, 1, 5);
        verifyOrder(orderID, 2, true, "10Y", 100.0);

        sendCancel(orderID);
        verifyCancel(orderID, 2, 10);
    }

    @Test
    public void testReplaceFillCancel() {
        int orderID = sendPassiveOrder("TRADER", true, 5, "10Y", 100.0);
        verifyOrder(orderID, 1, true, "10Y", 100.0);

        sendPassiveReplace(orderID, 10, 100.0);
        verifyCancel(orderID, 1, 5);
        verifyOrder(orderID, 2, true, "10Y", 100.0);

        sendFill(false, orderID, 3, 100.0);
        verifyFill(orderID, 3, 100.0, 2, 7);

        sendCancel(orderID);
        verifyCancel(orderID, 2, 7);
    }

    @Test
    public void testFill() {
        int orderID = sendPassiveOrder("TRADER", true, 5, "10Y", 100.0);
        verifyOrder(orderID, 1, true, "10Y", 100.0);

        sendFill(false, orderID, 3, 100.0);
        verifyFill(orderID, 3, 100.0, 1, 2);
    }

    @Test
    public void testMultipleFills() {
        int orderID = sendPassiveOrder("TRADER", true, 5, "10Y", 100.0);
        verifyOrder(orderID, 1, true, "10Y", 100.0);

        sendFill(false, orderID, 3, 100.0);
        verifyFill(orderID, 3, 100.0, 1, 2);

        sendFill(false, orderID, 1, 100.0);
        verifyFill(orderID, 1, 100.0, 1, 1);

        sendFill(false, orderID, 1, 100.0);
        verifyFill(orderID, 1, 100.0, 1, 0);
    }

    // TODO: Not sure if this is what we want
    @Test
    public void testOverfill() {
        int orderID = sendPassiveOrder("TRADER", true, 5, "10Y", 100.0);
        verifyOrder(orderID, 1, true, "10Y", 100.0);

        sendFill(false, orderID, 7, 100.0);
        // 5 was the shown qty
        verifyFill(orderID, 7, 100.0, 1, -2);
        //verifyOrder(orderID, 2, true, 3, "10Y", 100.0);
    }

    @After
    public void after() {
        verifyNone();
    }

    private void verifyNone() {
        Assert.assertEquals(0, listener.size());
    }

    private int verifyOrder(int internalID, int displayedID, boolean side, String security, double price) {
        StubDisplayedOrderServiceListener.StubDisplayedOrder e = listener.popOrder();
        DisplayedOrder se = e.order;
        Assert.assertEquals(getSecurityID(security), se.getSecurityID());
        Assert.assertEquals(PriceUtils.toLong(price, MatchConstants.IMPLIED_DECIMALS), se.getPrice());
        Assert.assertEquals(side, se.isBuy());
        Assert.assertEquals(internalID, se.getID());
        Assert.assertEquals(displayedID, se.getExternalOrderID());
        return se.getExternalOrderID();
    }

    private void verifyCancel(int internalID, int displayedID, int qtyCanceled) {
        StubDisplayedOrderServiceListener.StubDisplayedReduced e = listener.popReduced();
        DisplayedOrder se = e.order;
        Assert.assertEquals(displayedID, se.getExternalOrderID());
        Assert.assertEquals(internalID, se.getID());
        Assert.assertEquals(qtyCanceled, e.qtyReduced);
        //Assert.assertEquals(0, se.getRemainingQty());
    }

    private void verifyReduced(int internalID, int displayedID, int qtyCanceled) {
        StubDisplayedOrderServiceListener.StubDisplayedReduced e = listener.popReduced();
        DisplayedOrder se = e.order;
        Assert.assertEquals(displayedID, se.getExternalOrderID());
        Assert.assertEquals(internalID, se.getID());
        Assert.assertEquals(qtyCanceled, e.qtyReduced);
    }

    private void verifyFill(int internalID, int qty, double price, int displayedID, int displayedQty) {
        StubDisplayedOrderServiceListener.StubDisplayedFill e = listener.popFill();
        DisplayedOrder se = e.order;
        Assert.assertEquals(displayedID, se.getExternalOrderID());
        Assert.assertEquals(displayedQty, se.getRemainingQty());
        Assert.assertEquals(internalID, se.getID());
        Assert.assertEquals(qty, e.fillQty);
        Assert.assertEquals(PriceUtils.toLong(price, MatchConstants.IMPLIED_DECIMALS), e.fillPrice);
    }
}
