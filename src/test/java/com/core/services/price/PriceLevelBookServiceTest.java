package com.core.services.price;

import com.core.GenericTest;
import com.core.util.pool.ObjectPool;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jgreco on 7/30/15.
 */
public class PriceLevelBookServiceTest extends GenericTest {
    private PriceLevelBook book;
    
    @Before
    public void before() {
        book = new PriceLevelBook(new ObjectPool<>(log, "PLBOOK", PriceLevel::new, 100), 1, "10Y", 7, 1);
    }

    @Test
    public void testAddBid() {
        book.addLevel(0, true, 100, 1, 1, false);
        book.addLevel(0, true, 100, 1, 2, false);

        Assert.assertEquals(2, book.getBestBidQty());
        Assert.assertEquals(100, book.getBestBidPrice());
    }

    @Test
    public void testAddOffer() {
        book.addLevel(0, false, 101, 2, 1, false);
        book.addLevel(0, false, 101, 3, 2, false);

        Assert.assertEquals(5, book.getBestAskQty());
        Assert.assertEquals(101, book.getBestAskPrice());
    }

    @Test
    public void testAddBoth() {
        book.addLevel(0, true, 100, 3, 2, false);
        book.addLevel(0, false, 101, 2, 1, false);

        Assert.assertEquals(3, book.getBestBidQty());
        Assert.assertEquals(100, book.getBestBidPrice());
        Assert.assertEquals(2, book.getBestAskQty());
        Assert.assertEquals(101, book.getBestAskPrice());
    }

    @Test
    public void testLayerBids() {
        book.addLevel(0, true, 100, 1, 1, false);
        book.addLevel(0, true, 99, 2, 2, false);
        book.addLevel(0, true, 99, 3, 3, false);
        book.addLevel(0, true, 98, 4, 3, false);

        PriceLevelBook.PriceLevelIterator bidIt = book.getBidsIterator();
        PriceLevel next = bidIt.next();
        Assert.assertEquals(100, next.getPrice());
        Assert.assertEquals(1, next.getQty());

        next = bidIt.next();
        Assert.assertEquals(99, next.getPrice());
        Assert.assertEquals(5, next.getQty());

        next = bidIt.next();
        Assert.assertEquals(98, next.getPrice());
        Assert.assertEquals(4, next.getQty());

        Assert.assertFalse(bidIt.hasNext());
    }

    @Test
    public void testInsidePriceLevelRemove() {
        book.addLevel(0, true, 100, 1, 1, false);
        book.addLevel(0, true, 100, 1, 2, true);
        book.removeLevel(0, true, 100, 1, 2, true);

        PriceLevelBook.PriceLevelIterator bidIt = book.getBidsIterator();
        PriceLevel next = bidIt.next();
        Assert.assertEquals(100, next.getPrice());
        Assert.assertEquals(1, next.getQty());
        Assert.assertEquals(1, next.getOrders());
        Assert.assertEquals(0, next.getInsideOrders());

        Assert.assertFalse(bidIt.hasNext());
    }

}
