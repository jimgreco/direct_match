package com.core.match.services.order;

import com.core.match.GenericAppTest;
import com.core.match.msgs.MatchConstants;
import com.core.util.PriceUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jgreco on 10/11/15.
 */
public class DisplayedOrderServiceConvertedTest extends GenericAppTest<DisplayedOrder> {
    private StubDisplayedOrderServiceListener listener;

    public DisplayedOrderServiceConvertedTest() {
        super(DisplayedOrder.class);
    }

    @SuppressWarnings("unused")
    @Override
    @Before
    public void before() {
        listener = new StubDisplayedOrderServiceListener();

        DisplayedOrderService<DisplayedOrder> displayed = new DisplayedOrderService<>(DisplayedOrder.class, log, dispatcher, 100);
        displayed.addListener(listener);

        sendSecurity("10Y", 2.5, 20250815, 20150815);
        sendAccount("ACCOUNT");
        sendTrader("TRADER", "ACCOUNT");
    }

    @After
    public void after() {
        verifyNone();
    }

    @Test
    public void testRestingBuy() {
        rest(1, true, 100, "10Y", 100.0);
        verifyNone();
    }

    @Test
    public void testRestingSell() {
        rest(1, false, 100, "10Y", 100.0);
        verifyNone();
    }

    @Test
    public void testAggressNoOrder() {
        aggress(true, 100, "10Y", 100.0);
        aggress(false, 100, "10Y", 100.0);
        verifyNone();
    }

    @Test
    public void testCancelResting() {
        int orderID = rest(1, false, 100, "10Y", 100.0);
        sendCancel(orderID);
        verifyCancel(orderID, 1, 100);
    }

    @Test
    public void testIOCCancelsImmediately() {
        int internalOrderID = aggress(false, 100, "10Y", 100.0);
        sendCancel(internalOrderID);
        verifyNone();
    }

    @Test
    public void testIOCWithFillsCancelsAggressiveImmediately() {
        int orderID = ioc(false, 100, "10Y", 100.0);
        sendFill(false, orderID, 50, 100.0);
        sendFill(true, orderID, 20, 100.0);
        sendCancel(orderID);
        verifyNone();
    }

    @Test
    public void testIOCFillAll() {
        int orderID = ioc(false, 100, "10Y", 100.0);
        sendFill(false, orderID, 50, 100.0);
        sendFill(true, orderID, 50, 100.0);
        verifyNone();
    }

    @Test
    public void testMultipleReplaceDowns() {
        int internalOrderID = rest(1, true, 100, "10Y", 100.0);

        sendPassiveReplace(internalOrderID, 50, 100.0);
        verifyReduced(internalOrderID, 1, 50);

        sendPassiveReplace(internalOrderID, 25, 100.0);
        verifyReduced(internalOrderID, 1, 25);

        sendCancel(internalOrderID);
        verifyCancel(internalOrderID, 1, 25);
    }

    @Test
    public void testReplaceReduceQty() {
        int orderID = rest(1, false, 100, "10Y", 100.0);

        sendPassiveReplace(orderID, 25, 100.0);
        verifyReduced(orderID, 1, 75);
    }

    @Test
    public void testReplaceChangePrice() {
        int orderID = rest(1, false, 100, "10Y", 100.0);

        sendPassiveReplace(orderID, 100, 101.0);
        verifyCancel(orderID, 1, 100);
        verifyOrder(orderID, 2, false, "10Y", 101.0);
    }

    @Test
    public void testReplaceIncreaseDisplay() {
        int orderID = rest(1, false, 100, "10Y", 100.0);

        sendPassiveReplace(orderID, 150, 100.0);
        verifyCancel(orderID, 1, 100);
        verifyOrder(orderID, 2, false, "10Y", 100.0);
    }

    @Test
    public void testReplaceDecreaseDisplay() {
        int orderID = rest(1, false, 100, "10Y", 100.0);

        sendPassiveReplace(orderID, 150, 100.0);
        verifyCancel(orderID, 1, 100);
        verifyOrder(orderID, 2, false, "10Y", 100.0);
    }

    @Test
    public void testAggressiveOrderThatGetsReplacedShouldntHappen() {
        int orderID = aggress(false, 100, "10Y", 100.0);

        sendAggressiveReplace(orderID, 150, 100.0);
        verifyNone();
    }

    @Test
    public void testPassiveOrderAggressesOnReplace() {
        int orderID = rest(1, false, 100, "10Y", 100.0);

        sendAggressiveReplace(orderID, 150, 100.0);
        verifyCancel(orderID, 1, 100);
        verifyNone();

        sendFill(false, orderID, 50, 100.0);
        sendFill(false, orderID, 25, 100.0);
        sendFill(true, orderID, 25, 100.0);
        verifyOrder(orderID, 2, false, "10Y", 100.0);
    }

    @Test
    public void testLastFillAggressive() {
        int orderID = aggress(false, 100, "10Y", 100.0);

        sendFill(true, orderID, 50, 100.0);
        verifyOrder(orderID, 1, false, "10Y", 100.0);
    }

    @Test
    public void testAggressingOrder() {
        int orderID = aggress(false, 105, "10Y", 100.0);

        //sendFill(false, false, orderID, 50, 100.0, 0, 0);
        sendFill(false, orderID, 50, 100.0);
        sendFill(true, orderID, 50, 100.0);
        verifyOrder(orderID, 1, false, "10Y", 100.0);
    }

    @Test
    public void testFillAggressiveMultiple() {
        int orderID = aggress(false, 100, "10Y", 100.0);

        sendFill(false, orderID, 50, 100.0);
        sendFill(false, orderID, 10, 100.0);
        sendFill(true, orderID, 15, 100.0);
        verifyOrder(orderID, 1, false, "10Y", 100.0);
    }

    @Test
    public void testMatchAll() {
        int orderID1 = rest(1, false, 100, "10Y", 100.0);
        int orderID2 = aggress(true, 100, "10Y", 100.0);

        sendMatch(true, orderID1, orderID2, 100, 100.0);
        verifyFill(orderID1, 1, 100, 100.0);
        verifyNone();
    }

    @Test
    public void testMatchPartial() {
        int orderID1 = rest(1, false, 50, "10Y", 100.0);
        int orderID2 = aggress(true, 100, "10Y", 100.0);

        //sendMatch(true, orderID1, orderID2, 50, 100.0);
        sendFill(false, orderID1, 50, 100.0);
        sendFill(true, orderID2, 50, 100.0);
        verifyFill(orderID1, 1, 50, 100.0);
        verifyOrder(orderID2, 2, true, "10Y", 100.0);
    }

    private int rest(int displayedID, boolean side, int qty, String security, double price) {
        int orderID = sendPassiveOrder("TRADER", side, qty, security, price);
        verifyOrder(orderID, displayedID, side, "10Y", 100.0);
        return orderID;
    }

    private int aggress(boolean side, int qty, String security, double price) {
        return sendAggressiveOrder("TRADER", side, qty, security, price, false);
    }

    private int ioc(boolean side, int qty, String security, double price) {
        return sendAggressiveOrder("TRADER", side, qty, security, price, true);
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
    }

    private void verifyReduced(int internalID, int displayedID, int qtyCanceled) {
        StubDisplayedOrderServiceListener.StubDisplayedReduced e = listener.popReduced();
        DisplayedOrder se = e.order;
        Assert.assertEquals(displayedID, se.getExternalOrderID());
        Assert.assertEquals(internalID, se.getID());
        Assert.assertEquals(qtyCanceled, e.qtyReduced);
    }

    private void verifyFill(int internalID, int displayedID, int qty, double price) {
        StubDisplayedOrderServiceListener.StubDisplayedFill e = listener.popFill();
        DisplayedOrder se = e.order;
        Assert.assertEquals(displayedID, se.getExternalOrderID());
        Assert.assertEquals(internalID, se.getID());
        Assert.assertEquals(qty, e.fillQty);
        Assert.assertEquals(PriceUtils.toLong(price, MatchConstants.IMPLIED_DECIMALS), e.fillPrice);
    }

    private void verifyNone() {
        Assert.assertEquals(0, listener.size());
    }
}
