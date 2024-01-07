package com.core.match.services.book;

import com.core.match.GenericAppTest;
import com.core.match.services.order.DisplayedOrder;
import com.core.match.services.order.DisplayedOrderService;
import com.core.match.util.MatchPriceUtils;
import com.core.services.price.PriceLevel;
import com.core.services.price.PriceLevelBook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jgreco on 3/6/16.
 */
public class MatchDisplayedPriceLevelBookServiceTest extends GenericAppTest<DisplayedOrder> {
    private DisplayedOrderService<DisplayedOrder> displayed;
    private MatchDisplayedPriceLevelBookService prices;

    public MatchDisplayedPriceLevelBookServiceTest() {
        super(DisplayedOrder.class);
    }

    @Before
    public void setup() {
        displayed = new DisplayedOrderService<>(orders, log);
        prices = new MatchDisplayedPriceLevelBookService(displayed, securities, log, MatchDisplayedPriceLevelBookServiceRoundingType.NONE);

        sendAccount("DM");
        sendTrader("JIM", "DM");
        sendBond("5Y");
        sendBond("10Y");
    }

    @Test
    public void testAddMultiple() {
        sendPassiveOrder("JIM", true, 1, "10Y", 100.0);
        sendPassiveOrder("JIM", true, 2, "10Y", 100.0);

        PriceLevelBook book = prices.getBook(getSecurityID("10Y"));
        PriceLevelBook.PriceLevelIterator it = book.getBidsIterator();
        PriceLevel next = it.next();
        Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), next.getPrice());
        Assert.assertEquals(3, next.getQty());
        Assert.assertEquals(2, next.getOrders());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testCancelPartOfPriceLevel() {
        int orderID1 = sendPassiveOrder("JIM", true, 1, "10Y", 100.0);
        int orderID2 = sendPassiveOrder("JIM", true, 2, "10Y", 100.0);

        sendCancel(orderID2);

        PriceLevelBook book = prices.getBook(getSecurityID("10Y"));
        PriceLevelBook.PriceLevelIterator it = book.getBidsIterator();
        PriceLevel next = it.next();
        Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), next.getPrice());
        Assert.assertEquals(1, next.getQty());
        Assert.assertEquals(1, next.getOrders());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testCancelFullPriceLevel() {
        int orderID1 = sendPassiveOrder("JIM", true, 1, "10Y", 100.0);
        int orderID2 = sendPassiveOrder("JIM", true, 2, "10Y", 100.0);

        sendCancel(orderID1);
        sendCancel(orderID2);

        PriceLevelBook book = prices.getBook(getSecurityID("10Y"));
        PriceLevelBook.PriceLevelIterator it = book.getBidsIterator();
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testCancelFirstFullPriceLevel() {
        int orderID1 = sendPassiveOrder("JIM", true, 1, "10Y", 100.0);
        int orderID2 = sendPassiveOrder("JIM", true, 2, "10Y", 100.0);
        int orderID3 = sendPassiveOrder("JIM", true, 3, "10Y", 101.0);

        sendCancel(orderID1);
        sendCancel(orderID2);

        PriceLevelBook book = prices.getBook(getSecurityID("10Y"));
        PriceLevelBook.PriceLevelIterator it = book.getBidsIterator();
        PriceLevel next = it.next();
        Assert.assertEquals(101 * MatchPriceUtils.getPriceMultiplier(), next.getPrice());
        Assert.assertEquals(3, next.getQty());
        Assert.assertEquals(1, next.getOrders());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testCancelLastFullPriceLevel() {
        int orderID1 = sendPassiveOrder("JIM", true, 1, "10Y", 100.0);
        int orderID2 = sendPassiveOrder("JIM", true, 2, "10Y", 100.0);
        int orderID3 = sendPassiveOrder("JIM", true, 3, "10Y", 101.0);

        sendCancel(orderID3);

        PriceLevelBook book = prices.getBook(getSecurityID("10Y"));
        PriceLevelBook.PriceLevelIterator it = book.getBidsIterator();
        PriceLevel next = it.next();
        Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), next.getPrice());
        Assert.assertEquals(3, next.getQty());
        Assert.assertEquals(2, next.getOrders());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testReplace() {
        int orderID = sendPassiveOrder("JIM", true, 1, "10Y", 50.0);

        PriceLevelBook book = prices.getBook(getSecurityID("10Y"));
        PriceLevelBook.PriceLevelIterator it = book.getBidsIterator();
        Assert.assertEquals(50 * MatchPriceUtils.getPriceMultiplier(), it.next().getPrice());
        Assert.assertFalse(it.hasNext());

        sendPassiveReplace(orderID, 2, 100.0);
        it = book.getBidsIterator();
        Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), it.next().getPrice());
        Assert.assertFalse(it.hasNext());
    }
}
