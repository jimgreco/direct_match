package com.core.match.drops.gui;

import com.core.match.GenericAppTest;
import com.core.match.drops.DropCollection;
import com.core.match.drops.LinearCounter;
import com.core.match.drops.gui.msgs.GUIOrder;
import com.core.match.services.book.BookPositionService;
import com.core.match.services.book.MatchLimitOrder;
import com.core.match.services.order.DisplayedOrderService;
import com.core.match.util.MatchPriceUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jgreco on 2/24/16.
 */
public class OrderBookCollectionTest extends GenericAppTest<MatchLimitOrder> {
    private OrderBookCollection collection;
    private LinearCounter versions;

    public OrderBookCollectionTest() {
        super(MatchLimitOrder.class);
    }

    @Before
    public void setup() {
        DisplayedOrderService<MatchLimitOrder> displayed = new DisplayedOrderService<>(orders, log);
        BookPositionService service = new BookPositionService(displayed, securities, 5);
        versions = new LinearCounter();
        collection = new OrderBookCollection(versions, new LinearCounter(), service);

        sendAccount("DM");
        sendTrader("JIM", "DM");
        sendBond("10Y");

        DropCollection.DropIterator it = collection.getIterator(0);

        match(it, true, 0, 0, 0);
        match(it, true, 1, 0, 0);
        match(it, true, 2, 0, 0);
        match(it, true, 3, 0, 0);
        match(it, true, 4, 0, 0);

        match(it, false, 0, 0, 0);
        match(it, false, 1, 0, 0);
        match(it, false, 2, 0, 0);
        match(it, false, 3, 0, 0);
        match(it, false, 4, 0, 0);
        done(it);
    }

    @Test
    public void testOne() {
        sendPassiveOrder("JIM", true, 10, "10Y", 100);

        DropCollection.DropIterator it = collection.getIterator(10);
        match(it, true, 0, 100, 10);
        match(it, true, 1, 0, 0);
        match(it, true, 2, 0, 0);
        match(it, true, 3, 0, 0);
        match(it, true, 4, 0, 0);
        done(it);
    }

    @Test
    public void testLayer() {
        sendPassiveOrder("JIM", true, 5, "10Y", 100);
        sendPassiveOrder("JIM", true, 3, "10Y", 100);
        sendPassiveOrder("JIM", true, 5, "10Y", 99);

        DropCollection.DropIterator it = collection.getIterator(10);
        match(it, true, 0, 100, 5);
        match(it, true, 1, 100, 3);
        match(it, true, 2, 99, 5);
        match(it, true, 3, 0, 0);
        match(it, true, 4, 0, 0);
        done(it);
    }

    @Test
    public void testInsert() {
        sendPassiveOrder("JIM", true, 5, "10Y", 100);
        sendPassiveOrder("JIM", true, 3, "10Y", 100);
        sendPassiveOrder("JIM", true, 5, "10Y", 99);
        sendPassiveOrder("JIM", true, 5, "10Y", 98);
        sendPassiveOrder("JIM", true, 2, "10Y", 99.5);
        sendPassiveOrder("JIM", true, 3, "10Y", 100);

        DropCollection.DropIterator it = collection.getIterator(10);
        match(it, true, 0, 100, 5);
        match(it, true, 1, 100, 3);
        match(it, true, 2, 100, 3);
        match(it, true, 3, 99.5, 2);
        match(it, true, 4, 99, 5);
        done(it);
    }

    @Test
    public void testDeleteTop() {
        int orderID1 = sendPassiveOrder("JIM", true, 10, "10Y", 100);
        sendPassiveOrder("JIM", true, 3, "10Y", 100);
        sendPassiveOrder("JIM", true, 5, "10Y", 99);
        sendPassiveOrder("JIM", true, 5, "10Y", 98);
        sendPassiveOrder("JIM", true, 2, "10Y", 99.5);
        sendPassiveOrder("JIM", true, 3, "10Y", 100);

        int version = versions.getVersion();
        sendCancel(orderID1);

        DropCollection.DropIterator it = collection.getIterator(version);
        match(it, true, 0, 100, 3);
        match(it, true, 1, 100, 3);
        match(it, true, 2, 99.5, 2);
        match(it, true, 3, 99, 5);
        match(it, true, 4, 98, 5);
        done(it);
    }

    @Test
    public void testDeleteMiddle() {
        sendPassiveOrder("JIM", true, 5, "10Y", 100);
        sendPassiveOrder("JIM", true, 3, "10Y", 100);
        sendPassiveOrder("JIM", true, 5, "10Y", 99);
        sendPassiveOrder("JIM", true, 5, "10Y", 98);
        int orderID3 = sendPassiveOrder("JIM", true, 2, "10Y", 99.5);
        sendPassiveOrder("JIM", true, 3, "10Y", 100);

        // 5 x 100
        // 3 x 100
        // 3 x 100
        // 2 x  99.5
        // 5 x  99.0
        // 5 x  98.0

        int version = versions.getVersion();
        sendCancel(orderID3);

        DropCollection.DropIterator it = collection.getIterator(version);
        match(it, true, 3, 99, 5);
        match(it, true, 4, 98, 5);
        done(it);
    }

    @Test
    public void testDeleteNotInVisibleQueue() {
        sendPassiveOrder("JIM", true, 5, "10Y", 100);
        sendPassiveOrder("JIM", true, 3, "10Y", 100);
        sendPassiveOrder("JIM", true, 5, "10Y", 99);
        int orderID5 = sendPassiveOrder("JIM", true, 5, "10Y", 98);
        sendPassiveOrder("JIM", true, 2, "10Y", 99.5);
        sendPassiveOrder("JIM", true, 3, "10Y", 100);

        // 5 x 100
        // 3 x 100
        // 3 x 100
        // 2 x  99.5
        // 5 x  99.0
        // 5 x  98.0

        int version = versions.getVersion();
        sendCancel(orderID5);

        DropCollection.DropIterator it = collection.getIterator(version);
        done(it);
    }

    @Test
    public void testDeleteNotFilled() {
        sendPassiveOrder("JIM", true, 5, "10Y", 100);
        int orderID1 = sendPassiveOrder("JIM", true, 3, "10Y", 100);
        sendPassiveOrder("JIM", true, 5, "10Y", 99);

        int version = versions.getVersion();
        sendCancel(orderID1);

        DropCollection.DropIterator it = collection.getIterator(10);
        match(it, true, 0, 100, 5);
        match(it, true, 1, 99, 5);
        match(it, true, 2, 0, 0);
        match(it, true, 3, 0, 0);
        match(it, true, 4, 0, 0);
        done(it);
    }

    private void done(DropCollection.DropIterator it) {
        Assert.assertNull(it.next());
    }

    private void match(DropCollection.DropIterator it, boolean bid, int pos, double px, int qty) {
        Assert.assertTrue(it.hasNext());
        GUIOrder next = (GUIOrder) it.next();

        Assert.assertEquals(pos, next.getPos());
        Assert.assertEquals(bid, next.getSide());
        Assert.assertEquals("10Y", next.getSec());
        Assert.assertEquals((long) (px * MatchPriceUtils.getPriceMultiplier()), next.getPx());
        Assert.assertEquals(qty, next.getQty());
    }
}
