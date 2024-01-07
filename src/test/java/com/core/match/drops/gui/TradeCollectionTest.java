package com.core.match.drops.gui;

import com.core.match.GenericAppTest;
import com.core.match.drops.DropCollection;
import com.core.match.drops.LinearCounter;
import com.core.match.drops.gui.msgs.GUITrade;
import com.core.match.services.order.DisplayedOrder;
import com.core.match.util.MatchPriceUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jgreco on 2/20/16.
 */
public class TradeCollectionTest extends GenericAppTest<DisplayedOrder> {
    private TradeCollection collection;

    public TradeCollectionTest() {
        super(DisplayedOrder.class);
    }

    @Before
    public void setup() {
        collection = new TradeCollection(securities, trades, new LinearCounter(), new LinearCounter());

        sendBond("5Y");
        sendBond("10Y");

        GUITrade next;

        DropCollection.DropIterator iterator = collection.getIterator(0);
        Assert.assertTrue(iterator.hasNext());
        next = (GUITrade) iterator.next();
        Assert.assertEquals("5Y", next.getSec());
        Assert.assertEquals("trade", next.getType());
        Assert.assertEquals(0, next.getTime());
        Assert.assertEquals(1, next.getVer());
        Assert.assertEquals(0, next.getQty());
        Assert.assertEquals(0, next.getPx());

        Assert.assertTrue(iterator.hasNext());
        next = (GUITrade) iterator.next();
        Assert.assertEquals("10Y", next.getSec());
        Assert.assertEquals("trade", next.getType());
        Assert.assertEquals(0, next.getTime());
        Assert.assertEquals(2, next.getVer());
        Assert.assertEquals(0, next.getQty());
        Assert.assertEquals(0, next.getPx());

        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testAddMultipleTrades() {
        int orderID = sendPassiveOrder("JIM", true, 100, "5Y", 99);
        sendFill(true, orderID, 10, 99);
        sendFill(true, orderID, 5, 99);
        sendFill(true, orderID, 3, 99);

        DropCollection.DropIterator iterator = collection.getIterator(2);
        Assert.assertTrue(iterator.hasNext());
        GUITrade next = (GUITrade) iterator.next();
        Assert.assertEquals("5Y", next.getSec());
        Assert.assertEquals("trade", next.getType());
        Assert.assertEquals(5, next.getVer());
        Assert.assertEquals(3, next.getQty());
        Assert.assertEquals(99 * MatchPriceUtils.getPriceMultiplier(), next.getPx());

        Assert.assertTrue(iterator.hasNext());
        Assert.assertNull(iterator.next());
    }

    @Test
    public void testMultipleSecuritiesTraded() {
        int orderID1 = sendPassiveOrder("JIM", true, 100, "5Y", 99);
        int orderID2 = sendPassiveOrder("JIM", true, 99, "10Y", 99);
        sendFill(true, orderID1, 10, 100);
        sendFill(true, orderID2, 5, 99);
        sendFill(true, orderID1, 3, 98);

        DropCollection.DropIterator iterator = collection.getIterator(2);
        Assert.assertTrue(iterator.hasNext());
        GUITrade next = (GUITrade) iterator.next();
        Assert.assertEquals("5Y", next.getSec());
        Assert.assertEquals("trade", next.getType());
        Assert.assertEquals(5, next.getVer());
        Assert.assertEquals(3 , next.getQty());
        Assert.assertEquals(98 * MatchPriceUtils.getPriceMultiplier(), next.getPx());

        next = (GUITrade) iterator.next();
        Assert.assertEquals("10Y", next.getSec());
        Assert.assertEquals("trade", next.getType());
        Assert.assertEquals(4, next.getVer());
        Assert.assertEquals(5, next.getQty());
        Assert.assertEquals(99 * MatchPriceUtils.getPriceMultiplier(), next.getPx());

        Assert.assertFalse(iterator.hasNext());
        Assert.assertNull(iterator.next());
    }
}
