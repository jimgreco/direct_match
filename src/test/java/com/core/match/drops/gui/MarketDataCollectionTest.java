package com.core.match.drops.gui;

import com.core.match.GenericAppTest;
import com.core.match.drops.DropCollection;
import com.core.match.drops.LinearCounter;
import com.core.match.drops.gui.msgs.GUIPrice;
import com.core.match.services.book.MatchDisplayedPriceLevelBookService;
import com.core.match.services.book.MatchDisplayedPriceLevelBookServiceRoundingType;
import com.core.match.services.order.DisplayedOrder;
import com.core.match.services.order.DisplayedOrderService;
import com.core.match.services.security.Bond;
import com.core.match.util.MatchPriceUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jgreco on 2/18/16.
 */
public class MarketDataCollectionTest extends GenericAppTest<DisplayedOrder> {
    private MarketDataCollection collection;
    private Bond sec1;
    private Bond sec2;
    private Bond sec3;
    private Bond sec4;
    private MatchDisplayedPriceLevelBookService book;
    private int maxVersion;

    public MarketDataCollectionTest() {
        super(DisplayedOrder.class);
    }

    @Before
    public void setup() {

        DisplayedOrderService<DisplayedOrder> disp=new DisplayedOrderService<DisplayedOrder>(orders,log);

        book=new MatchDisplayedPriceLevelBookService(disp,securities,log, MatchDisplayedPriceLevelBookServiceRoundingType.NONE);
        collection = new MarketDataCollection(securities,book, new LinearCounter(), new LinearCounter(), 5, false);

        sendAccount("DM");
        sendTrader("JIM", "DM");

        sendBond("2Y");
        sendBond("5Y");
        sendBond("10Y");
        sendBond("30Y");

        sec1 = securities.getBond((short)1); // 1, 2, 3
        sec2 = securities.getBond((short)2); // 4, 5, 6
        sec3 = securities.getBond((short)3); // 7, 8, 9
        sec4 = securities.getBond((short)4); // 10, 11, 12

        DropCollection.DropIterator iterator = collection.getIterator(maxVersion);

        maxVersion = verifyFull(iterator, sec1, maxVersion + 1);
        maxVersion = verifyFull(iterator, sec2, maxVersion + 1);
        maxVersion = verifyFull(iterator, sec3, maxVersion + 1);
        maxVersion = verifyFull(iterator, sec4, maxVersion + 1);

        Assert.assertFalse(iterator.hasNext());
        Assert.assertNull(iterator.next());
    }

    private int verifyFull(DropCollection.DropIterator iterator, Bond sec, int start) {
        for (int i=0; i<5; i++) {
            Assert.assertTrue(iterator.hasNext());
            GUIPrice next = (GUIPrice)iterator.next();
            Assert.assertEquals(sec.getName(), next.getSec());
            Assert.assertEquals(2*i + start, next.getVer());
            Assert.assertTrue(next.getSide());
            Assert.assertEquals("price", next.getType());

            Assert.assertTrue(iterator.hasNext());
            next = (GUIPrice)iterator.next();
            Assert.assertEquals(sec.getName(), next.getSec());
            Assert.assertEquals(2*i + start + 1, next.getVer());
            Assert.assertFalse(next.getSide());
            Assert.assertEquals("price", next.getType());
        }

        return iterator.getMaxVersion();
    }

    @Test
    public void testIterateUpdatesAllWithZero() {
        DropCollection.DropIterator iterator = collection.getIterator(0);

        verifyFull(iterator, sec1, 1);
        verifyFull(iterator, sec2, 11);
        verifyFull(iterator, sec3, 21);
        verifyFull(iterator, sec4, 31);
        // 32

        Assert.assertFalse(iterator.hasNext());
        Assert.assertNull(iterator.next());
    }

    @Test
    public void testIterateUpdateIndividualQuotes() {
        collection.updateQuote(sec1, 0, true, 0, 1, 100, 1, 0); // 41
        collection.updateQuote(sec2, 0, true, 3, 4, 101, 1, 0); // 42
        collection.updateQuote(sec2, 0, true, 2, 3, 102, 1, 0); // 43
        collection.updateQuote(sec1, 0, false, 1, 4, 103, 1, 0); // 44

        DropCollection.DropIterator iterator = collection.getIterator(40);
        GUIPrice next;

        Assert.assertTrue(iterator.hasNext());
        next = (GUIPrice) iterator.next();
        Assert.assertEquals(sec1.getName(), next.getSec());
        Assert.assertEquals(41, next.getVer());
        Assert.assertTrue(next.getSide());
        Assert.assertEquals("price", next.getType());

        Assert.assertTrue(iterator.hasNext());
        next = (GUIPrice) iterator.next();
        Assert.assertEquals(sec1.getName(), next.getSec());
        Assert.assertEquals(44, next.getVer());
        Assert.assertFalse(next.getSide());
        Assert.assertEquals("price", next.getType());

        Assert.assertTrue(iterator.hasNext());
        next = (GUIPrice) iterator.next();
        Assert.assertEquals(sec2.getName(), next.getSec());
        Assert.assertEquals(43, next.getVer());
        Assert.assertTrue(next.getSide());
        Assert.assertEquals("price", next.getType());

        Assert.assertTrue(iterator.hasNext());
        next = (GUIPrice) iterator.next();
        Assert.assertEquals(sec2.getName(), next.getSec());
        Assert.assertEquals(42, next.getVer());
        Assert.assertTrue(next.getSide());
        Assert.assertEquals("price", next.getType());

        Assert.assertTrue(iterator.hasNext());
        Assert.assertNull(iterator.next());
    }

    @Test
    public void testMultipleUpdates() {
        collection.updateQuote(sec1, 0, true, 0, 1, 100, 1, 0); // 41
        collection.updateQuote(sec1, 0, true, 0, 2, 101, 1, 0); // 42
        collection.updateQuote(sec1, 0, true, 0, 3, 102, 1, 0); // 43
        collection.updateQuote(sec1, 0, true, 0, 4, 103, 1, 0); // 44

        DropCollection.DropIterator iterator = collection.getIterator(41);
        GUIPrice next;

        Assert.assertTrue(iterator.hasNext());
        next = (GUIPrice) iterator.next();
        Assert.assertEquals(sec1.getName(), next.getSec());
        Assert.assertEquals(44, next.getVer());
        Assert.assertTrue(next.getSide());
        Assert.assertEquals("price", next.getType());

        Assert.assertTrue(iterator.hasNext());
        Assert.assertNull(iterator.next());
    }

    @Test
    public void testUpdatePriceLevel() {
        bid(1, 100);
        bid(2, 99);
        bid(3, 98);

        DropCollection.DropIterator iterator = collection.getIterator(maxVersion);
        verifyBid(iterator, 0, 1, 100);
        verifyBid(iterator, 1, 2, 99);
        verifyBid(iterator, 2, 3, 98);
        verifyBid(iterator, 3, 0, 0);
        int version = verifyBid(iterator, 4, 0, 0);
        verifyDone(iterator);

        bid(5, 99);
        iterator = collection.getIterator(version);
        verifyBid(iterator, 1, 7, 99);
    }

    @Test
    public void testAddPriceLevelAtStart() {
        bid(1, 100);
        bid(2, 99);
        bid(3, 98);

        DropCollection.DropIterator iterator = collection.getIterator(maxVersion);
        verifyBid(iterator, 0, 1, 100);
        verifyBid(iterator, 1, 2, 99);
        verifyBid(iterator, 2, 3, 98);
        verifyBid(iterator, 3, 0, 0);
        int version = verifyBid(iterator, 4, 0, 0);
        verifyDone(iterator);

        bid(5, 101);
        iterator = collection.getIterator(version);
        verifyBid(iterator, 0, 5, 101);
        verifyBid(iterator, 1, 1, 100);
        verifyBid(iterator, 2, 2, 99);
        verifyBid(iterator, 3, 3, 98);
        verifyBid(iterator, 4, 0, 0);
        verifyDone(iterator);
    }

    @Test
    public void testAddPriceLevelInMiddle() {
        bid(1, 100);
        bid(2, 99);
        bid(4, 97);
        bid(5, 96);
        bid(6, 95);

        DropCollection.DropIterator iterator = collection.getIterator(maxVersion);
        verifyBid(iterator, 0, 1, 100);
        verifyBid(iterator, 1, 2, 99);
        verifyBid(iterator, 2, 4, 97);
        verifyBid(iterator, 3, 5, 96);
        int version = verifyBid(iterator, 4, 6, 95);
        verifyDone(iterator);

        bid(3, 98);
        iterator = collection.getIterator(version);
        verifyBid(iterator, 2, 3, 98);
        verifyBid(iterator, 3, 4, 97);
        verifyBid(iterator, 4, 5, 96);
        verifyDone(iterator);
    }

    @Test
    public void testRemovePriceLevelAtStart() {
        bid(1, 100);
        bid(2, 99);
        bid(3, 98);

        DropCollection.DropIterator iterator = collection.getIterator(maxVersion);
        verifyBid(iterator, 0, 1, 100);
        verifyBid(iterator, 1, 2, 99);
        verifyBid(iterator, 2, 3, 98);
        verifyBid(iterator, 3, 0, 0);
        int version = verifyBid(iterator, 4, 0, 0);
        verifyDone(iterator);

        sendCancel(1);
        iterator = collection.getIterator(version);
        verifyBid(iterator, 0, 2, 99);
        verifyBid(iterator, 1, 3, 98);
        verifyBid(iterator, 2, 0, 0);
        verifyBid(iterator, 3, 0, 0);
        verifyBid(iterator, 4, 0, 0);
        verifyDone(iterator);
    }

    @Test
    public void testRemovePriceLevelInMiddle() {
        bid(1, 100);
        bid(2, 99);
        bid(3, 98); // canceled
        bid(4, 97);
        bid(5, 96);
        bid(6, 95);

        DropCollection.DropIterator iterator = collection.getIterator(maxVersion);
        verifyBid(iterator, 0, 1, 100);
        verifyBid(iterator, 1, 2, 99);
        verifyBid(iterator, 2, 3, 98);
        verifyBid(iterator, 3, 4, 97);
        int version = verifyBid(iterator, 4, 5, 96);
        verifyDone(iterator);

        sendCancel(3);
        iterator = collection.getIterator(version);
        verifyBid(iterator, 2, 4, 97);
        verifyBid(iterator, 3, 5, 96);
        verifyBid(iterator, 4, 6, 95);
        verifyDone(iterator);
    }

    @Test
    public void testRemovePriceLevelOutside() {
        bid(1, 100);
        bid(2, 99);
        bid(3, 98);
        bid(4, 97);
        bid(5, 96);
        bid(6, 95); // canceled

        DropCollection.DropIterator iterator = collection.getIterator(maxVersion);
        verifyBid(iterator, 0, 1, 100);
        verifyBid(iterator, 1, 2, 99);
        verifyBid(iterator, 2, 3, 98);
        verifyBid(iterator, 3, 4, 97);
        int version = verifyBid(iterator, 4, 5, 96);
        verifyDone(iterator);

        sendCancel(6);
        iterator = collection.getIterator(version);
        verifyDone(iterator);
    }

    @Test
    public void testRemovePriceLevelAtEnd() {
        bid(1, 100);
        bid(2, 99);
        bid(3, 98);
        bid(4, 97);
        bid(5, 96); // canceled

        DropCollection.DropIterator iterator = collection.getIterator(maxVersion);
        verifyBid(iterator, 0, 1, 100);
        verifyBid(iterator, 1, 2, 99);
        verifyBid(iterator, 2, 3, 98);
        verifyBid(iterator, 3, 4, 97);
        int version = verifyBid(iterator, 4, 5, 96);
        verifyDone(iterator);

        sendCancel(5);
        iterator = collection.getIterator(version);
        verifyBid(iterator, 4, 0, 0);
        verifyDone(iterator);
    }

    @Test
    public void testReplacePrice() {
        int orderID = sendPassiveOrder("JIM", true, 1, "10Y", 50);
        sendPassiveReplace(orderID, 2, 100);

        DropCollection.DropIterator iterator = collection.getIterator(maxVersion);
        verifyBid(iterator, 0, 2, 100);
        verifyBid(iterator, 1, 0, 0);
        verifyBid(iterator, 2, 0, 0);
        verifyBid(iterator, 3, 0, 0);
        verifyBid(iterator, 4, 0, 0);
        verifyDone(iterator);
    }

    private int verifyBid(DropCollection.DropIterator iterator, int position, int qty, int price) {
        GUIPrice next = (GUIPrice) iterator.next();
        Assert.assertTrue(next.getSide());
        Assert.assertEquals(price * MatchPriceUtils.getPriceMultiplier(), next.getPx());
        Assert.assertEquals(qty, next.getQty());
        Assert.assertEquals(position, next.getPos());
        return iterator.getMaxVersion();
    }

    private void verifyDone(DropCollection.DropIterator iterator) {
        Assert.assertNull(iterator.next());
    }

    private int bid(int qty, int price) {
        return sendPassiveOrder("JIM", true, qty, "10Y", price);
    }
}
