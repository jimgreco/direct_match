package com.core.match.sequencer;

import com.core.GenericTest;
import com.core.match.msgs.MatchByteBufferMessages;
import com.core.match.msgs.MatchMessages;
import com.core.match.msgs.MatchOrderCommand;
import com.core.match.msgs.MatchReplaceCommand;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.services.limit.LimitBook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * User: jgreco
 */

public class SequencerHiddenQtyLimitBookServiceTest extends GenericTest {
    private SequencerLimitBookService books;
    private SequencerBookServiceListener listener;
    private SequencerOrderService orders;
    private short securityID = 1;
    private LimitBook<SequencerOrder> book;
    private MatchMessages msgs = new MatchByteBufferMessages();

    @Before
    public void before() {
        listener = Mockito.mock(SequencerBookServiceListener.class);
        orders = new SequencerOrderService(log);
        books = new SequencerLimitBookService(this.log, orders);
        books.addBook();
        book = books.getBook(securityID);
        books.setListener(listener);
    }
    
    @Test
    public void testAggressing() {
        SequencerOrder offer = insertOrder(false, 10, 100, true);
        insertOrder(true, 80, 100, false);
        
        Mockito.verify(listener).onMatch(1, 1, 2, 10, 100, 1, true, false, true);
        Assert.assertEquals(70, books.getBook(securityID).getBestBid().getRemainingQty());
    }
    
    @Test
    public void testReplaceQtyDownTwice() {
        SequencerOrder offer = insertOrder(false, 3, 100, true);

        boolean reinsert = books.buildReplace(offer, getReplace(100, 2, true));
        Assert.assertFalse(reinsert);
        books.replaceOrder(offer, reinsert);

        reinsert = books.buildReplace(offer, getReplace(100, 1, true));
        Assert.assertFalse(reinsert);
        books.replaceOrder(offer, false);

        insertOrder(true, 10, 100, false);

        Mockito.verify(listener).onMatch(1, 1, 2, 1, 100, 1, true, false, true);
        Assert.assertNull(books.getBook(securityID).getBestOffer());
    }
    
    @Test
    public void testReplaceQtyDown() {
        SequencerOrder offer = insertOrder(false, 2, 100, true);
        boolean reinsert = books.buildReplace(offer, getReplace(100, 1, true));
        Assert.assertFalse(reinsert);
        books.replaceOrder(offer, reinsert);

        insertOrder(true, 2, 100, false);
        Mockito.verify(listener).onMatch(1, 1, 2, 1, 100, 1, true, false, true);
        Assert.assertNull(books.getBook(securityID).getBestOffer());
    }

    @Test
    public void testSellMatchFillPartial() {
    	// bid 1
        insertOrder(true, 2, 100, true);
        // offer 1
        SequencerOrder offer = insertOrder(false, 3, 99, false);

        Mockito.verify(listener).onMatch(1, 1, 2, 2, 100, 1, true, false, true);
        Assert.assertEquals(1, offer.getRemainingQty());
    }

    @Test
    public void testSellMatchFillMultipleAtSamePriceLevels() {
        insertOrder(true, 1, 100, true);
        insertOrder(true, 2, 100, true);
        SequencerOrder offer = insertOrder(false, 5, 99, false);

        Mockito.verify(listener).onMatch(1, 1, 3, 1, 100, 1, false, false, false);
        Mockito.verify(listener).onMatch(2, 2, 3, 2, 100, 2, true, false, true);
        Assert.assertEquals(2, offer.getRemainingQty());
    }

    @Test
    public void testBuyMatchFillAll() {
        insertOrder(false, 2, 99, true);
        insertOrder(true, 1, 100, false);

        Mockito.verify(listener).onMatch(1, 1, 2, 1, 99, 1, true, true, false);
    }

    @Test
    public void testBuyMatchFillPartial() {
        insertOrder(false, 2, 99, true);
        SequencerOrder bid = insertOrder(true, 3, 100, false);

        Mockito.verify(listener).onMatch(1, 1, 2, 2, 99, 1, true, false, true);
        Assert.assertEquals(1, bid.getRemainingQty());
    }

    @Test
    public void testBuyMatchMultipleAtDifferentPriceLevels() {
        insertOrder(false, 1, 101, true);
        insertOrder(false, 2, 100, true);
        SequencerOrder bid = insertOrder(true, 5, 101, false);

        Mockito.verify(listener).onMatch(1, 2, 3, 2, 100, 1, false, false, false);
        Mockito.verify(listener).onMatch(2, 1, 3, 1, 101, 2, true, false, true);
        Assert.assertEquals(2, bid.getRemainingQty());
    }

    @Test
    public void testBuyMatchFillMultipleAtSamePriceLevels() {
        insertOrder(false, 1, 100, true);
        insertOrder(false, 2, 100, true);
        SequencerOrder bid = insertOrder(true, 5, 100, false);

        Mockito.verify(listener).onMatch(1, 1, 3, 1, 100, 1, false, false, false);
        Mockito.verify(listener).onMatch(2, 2, 3, 2, 100, 2, true, false, true);
        Assert.assertEquals(2, bid.getRemainingQty());
    }

    @Test
    public void testReplace() {
        SequencerOrder offer1 = insertOrder(false, 1, 100, true);
        boolean reinsert = books.buildReplace(offer1, getReplace(101, 2, true));
        Assert.assertTrue(reinsert);
        books.replaceOrder(offer1, reinsert);

        Assert.assertEquals(2, offer1.getQty());
        Assert.assertEquals(101, offer1.getPrice());
        Assert.assertEquals(1, offer1.getID());
        Assert.assertEquals(2, offer1.getExternalOrderID());
    }

    @Test
    public void testReplaceMatchPartialFill() {
        insertOrder(false, 1, 100, true);
        SequencerOrder bid = insertOrder(true, 5, 99, true);

        boolean reinsert = books.buildReplace(bid, getReplace(100, 5, false));
        Assert.assertTrue(reinsert);
        books.replaceOrder(bid, reinsert);

        Mockito.verify(listener).onMatch(1, 1, 2, 1, 100, 1, true, false, true);
        Assert.assertEquals(4, bid.getRemainingQty());
        Assert.assertNotNull(books.getBook(securityID).getBestBid());
    }

    @Test
    public void testReplaceMatchPartialFillRefresh() {
        insertOrder(false, 10, 100, true);

        SequencerOrder bid = insertOrder(true, 7, 99, true);

        boolean reinsert = books.buildReplace(bid, getReplace(100, 15, false));
        Assert.assertTrue(reinsert);
        books.replaceOrder(bid, reinsert);

        Mockito.verify(listener).onMatch(1, 1, 2, 10, 100, 1, true, false, true);
        Assert.assertEquals(5, bid.getRemainingQty());
        Assert.assertNotNull(books.getBook(securityID).getBestBid());
    }

    @Test
    public void testReplaceMatchFullyFilled() {
        insertOrder(false, 2, 100, true);
        SequencerOrder bid = insertOrder(true, 1, 99, true);

        boolean reinsert = books.buildReplace(bid, getReplace(100, 1, false));
        Assert.assertTrue(reinsert);
        books.replaceOrder(bid, reinsert);

        Mockito.verify(listener).onMatch(1, 1, 2, 1, 100, 1, true, true, false);
        Assert.assertNull(books.getBook(securityID).getBestBid());
    }

    @Test
    public void testIOC() {
        MatchOrderCommand msg = msgs.getMatchOrderCommand();
        msg.setBuy(true);
        msg.setQty(100);
        msg.setSecurityID(securityID);
        msg.setPrice(100);
        msg.setIOC(true);
        SequencerOrder order = books.buildOrder(msg.toEvent());
        books.addOrder(order);

        Assert.assertNull(book.getBestBid());
        Assert.assertNull(book.getBestOffer());
    }

    @Test
    public void testAggressingIOC() {
        SequencerOrder offer = insertOrder(false, 1, 100, true);

        MatchOrderCommand msg = msgs.getMatchOrderCommand();
        msg.setBuy(true);
        msg.setQty(2);
        msg.setSecurityID(securityID);
        msg.setPrice(100);
        msg.setIOC(true);
        SequencerOrder order = books.buildOrder(msg.toEvent());
        books.addOrder(order);

        Mockito.verify(listener).onMatch(1, 1, 2, 1, 100, 1, true, false, false);
    }

    private SequencerOrder insertOrder(boolean buy, int qty, long price, boolean passive) {
        MatchOrderCommand msg = msgs.getMatchOrderCommand();
        msg.setBuy(buy);
        msg.setQty(qty);
        msg.setSecurityID(securityID);
        msg.setPrice(price);
        msg.setInBook(passive);
        SequencerOrder order = books.buildOrder(msg.toEvent());
        books.addOrder(order);
        return order;
    }

    private MatchReplaceEvent getReplace(long newPrice, int newQty, boolean passive) {
        MatchReplaceCommand cmd = msgs.getMatchReplaceCommand();
        cmd.setPrice(newPrice);
        cmd.setQty(newQty);
        cmd.setInBook(passive);
        return cmd.toEvent();
    }
}
