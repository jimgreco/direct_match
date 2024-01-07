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
public class SequencerLimitBookServiceTest extends GenericTest {
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
        books = new SequencerLimitBookService(log, orders);
        books.addBook();
        book = books.getBook(securityID);
        books.setListener(listener);
    }

    @Test
    public void testInsertOrder() {
        SequencerOrder order = insertOrder(true, 1, 100, true);
        SequencerOrder firstBid = book.getBestBid();

        Assert.assertEquals(order, firstBid);
        Assert.assertNull(firstBid.next());
        Assert.assertNull(book.getBestOffer());
        Assert.assertEquals(1, order.getExternalOrderID());
    }

    @Test
    public void testLayerBids() {
        SequencerOrder order2 = insertOrder(true, 2, 100, true);
        SequencerOrder order0 = insertOrder(true, 1, 101, true);
        SequencerOrder order1 = insertOrder(true, 1, 101, true);
        SequencerOrder order4 = insertOrder(true, 4, 99, true);
        SequencerOrder order3 = insertOrder(true, 3, 100, true);
        SequencerOrder order5 = insertOrder(true, 1, 99, true);

        Assert.assertEquals(2, order0.getExternalOrderID());
        Assert.assertEquals(3, order1.getExternalOrderID());
        Assert.assertEquals(1, order2.getExternalOrderID());
        Assert.assertEquals(5, order3.getExternalOrderID());
        Assert.assertEquals(4, order4.getExternalOrderID());
        Assert.assertEquals(6, order5.getExternalOrderID());

        SequencerOrder next = book.getBestBid();
        Assert.assertEquals(order0, next);
        Assert.assertEquals(2, order0.getID());

        next = next.next();
        Assert.assertEquals(order1, next);
        Assert.assertEquals(3, order1.getID());

        next = next.next();
        Assert.assertEquals(order2, next);

        next = next.next();
        Assert.assertEquals(order3, next);

        next = next.next();
        Assert.assertEquals(order4, next);

        next = next.next();
        Assert.assertEquals(order5, next);

        next = next.next();
        Assert.assertNull(next);
    }

    @Test
    public void testLayerOffers() {
        SequencerOrder order2 = insertOrder(false, 2, 100, true);
        SequencerOrder order0 = insertOrder(false, 1, 99, true);
        SequencerOrder order1 = insertOrder(false, 1, 99, true);
        SequencerOrder order4 = insertOrder(false, 4, 101, true);
        SequencerOrder order3 = insertOrder(false, 3, 100, true);
        SequencerOrder order5 = insertOrder(false, 3, 101, true);

        Assert.assertEquals(2, order0.getExternalOrderID());
        Assert.assertEquals(3, order1.getExternalOrderID());
        Assert.assertEquals(1, order2.getExternalOrderID());
        Assert.assertEquals(5, order3.getExternalOrderID());
        Assert.assertEquals(4, order4.getExternalOrderID());
        Assert.assertEquals(6, order5.getExternalOrderID());

        SequencerOrder next = book.getBestOffer();
        Assert.assertEquals(order0, next);

        next = next.next();
        Assert.assertEquals(order1, next);

        next = next.next();
        Assert.assertEquals(order2, next);

        next = next.next();
        Assert.assertEquals(order3, next);

        next = next.next();
        Assert.assertEquals(order4, next);

        next = next.next();
        Assert.assertEquals(order5, next);

        next = next.next();
        Assert.assertNull(next);
    }

	@Test
    public void testDeleteBids() {
        SequencerOrder order2 = insertOrder(true, 2, 100, true);
        SequencerOrder order0 = insertOrder(true, 1, 101, true);
        SequencerOrder order1 = insertOrder(true, 1, 101, true);
        SequencerOrder order4 = insertOrder(true, 4, 99, true);
        SequencerOrder order3 = insertOrder(true, 3, 100, true);
        SequencerOrder order5 = insertOrder(true, 1, 99, true);

        book.removeOrder(order0);

        SequencerOrder next = book.getBestBid();
        Assert.assertEquals(order1, next);
        next = next.next();
        Assert.assertEquals(order2, next);
        next = next.next();
        Assert.assertEquals(order3, next);
        next = next.next();
        Assert.assertEquals(order4, next);
        next = next.next();
        Assert.assertEquals(order5, next);
        next = next.next();
        Assert.assertNull(next);

        book.removeOrder(order5);

        next = book.getBestBid();
        Assert.assertEquals(order1, next);
        next = next.next();
        Assert.assertEquals(order2, next);
        next = next.next();
        Assert.assertEquals(order3, next);
        next = next.next();
        Assert.assertEquals(order4, next);
        next = next.next();
        Assert.assertNull(next);

        book.removeOrder(order3);

        next = book.getBestBid();
        Assert.assertEquals(order1, next);
        next = next.next();
        Assert.assertEquals(order2, next);
        next = next.next();
        Assert.assertEquals(order4, next);
        next = next.next();
        Assert.assertNull(next);

        book.removeOrder(order1);

        next = book.getBestBid();
        Assert.assertEquals(order2, next);
        next = next.next();
        Assert.assertEquals(order4, next);
        next = next.next();
        Assert.assertNull(next);

        // this is an error
        book.removeOrder(order1);

        book.removeOrder(order4);

        next = book.getBestBid();
        Assert.assertEquals(order2, next);
        next = next.next();
        Assert.assertNull(next);

        book.removeOrder(order2);

        next = book.getBestBid();
        Assert.assertNull(next);
    }

	@Test
    public void testDeleteOffers() {
        SequencerOrder order2 = insertOrder(false, 2, 100, true);
        SequencerOrder order0 = insertOrder(false, 1, 99, true);
        SequencerOrder order1 = insertOrder(false, 1, 99, true);
        SequencerOrder order4 = insertOrder(false, 4, 101, true);
        SequencerOrder order3 = insertOrder(false, 3, 100, true);
        SequencerOrder order5 = insertOrder(false, 3, 101, true);

        book.removeOrder(order0);

        SequencerOrder next = book.getBestOffer();
        Assert.assertEquals(order1, next);
        next = next.next();
        Assert.assertEquals(order2, next);
        next = next.next();
        Assert.assertEquals(order3, next);
        next = next.next();
        Assert.assertEquals(order4, next);
        next = next.next();
        Assert.assertEquals(order5, next);
        next = next.next();
        Assert.assertNull(next);

        book.removeOrder(order5);

        next = book.getBestOffer();
        Assert.assertEquals(order1, next);
        next = next.next();
        Assert.assertEquals(order2, next);
        next = next.next();
        Assert.assertEquals(order3, next);
        next = next.next();
        Assert.assertEquals(order4, next);
        next = next.next();
        Assert.assertNull(next);

        book.removeOrder(order3);

        next = book.getBestOffer();
        Assert.assertEquals(order1, next);
        next = next.next();
        Assert.assertEquals(order2, next);
        next = next.next();
        Assert.assertEquals(order4, next);
        next = next.next();
        Assert.assertNull(next);

        book.removeOrder(order1);

        next = book.getBestOffer();
        Assert.assertEquals(order2, next);
        next = next.next();
        Assert.assertEquals(order4, next);
        next = next.next();
        Assert.assertNull(next);

        // this is an error
        book.removeOrder(order1);

        book.removeOrder(order4);

        next = book.getBestOffer();
        Assert.assertEquals(order2, next);
        next = next.next();
        Assert.assertNull(next);

        book.removeOrder(order2);

        next = book.getBestOffer();
        Assert.assertNull(next);
    }

    @Test
    public void testSellMatchFillAll() {
        insertOrder(true, 2, 100, true);
        insertOrder(false, 1, 99, false);

        Mockito.verify(listener).onMatch(1, 1, 2, 1, 100, 1, true, true, false);
    }

    @Test
    public void testSellMatchFillPartial() {
        insertOrder(true, 2, 100, true);
        SequencerOrder offer = insertOrder(false, 3, 99, false);

        Mockito.verify(listener).onMatch(1, 1, 2, 2, 100, 1, true, false, true);
        Assert.assertEquals(1, offer.getRemainingQty());
    }

    @Test
    public void testSellMatchMultipleAtDifferentPriceLevels() {
        insertOrder(true, 1, 100, true);
        insertOrder(true, 2, 99, true);
        SequencerOrder offer = insertOrder(false, 5, 99, false);

        Mockito.verify(listener).onMatch(1, 1, 3, 1, 100, 1, false, false, false);
        Mockito.verify(listener).onMatch(2, 2, 3, 2, 99, 2, true, false, true);
        Assert.assertEquals(2, offer.getRemainingQty());
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
        boolean reinsert = books.buildReplace(offer1, getReplace(2, 101, true));
        books.replaceOrder(offer1, reinsert);

        Assert.assertEquals(2, offer1.getQty());
        Assert.assertEquals(101, offer1.getPrice());
    }

    @Test
    public void testReplaceMatchPartialFill() {
        SequencerOrder offer = insertOrder(false, 1, 100, true);
        SequencerOrder bid = insertOrder(true, 5, 99, true);

        Assert.assertEquals(1, offer.getExternalOrderID());
        Assert.assertEquals(2, bid.getExternalOrderID());

        boolean reinsert = books.buildReplace(bid, getReplace(5, 100, false));
        books.replaceOrder(bid, reinsert);

        Assert.assertEquals(3, bid.getExternalOrderID());

        Mockito.verify(listener).onMatch(1, 1, 2, 1, 100, 1, true, false, true);
        Assert.assertEquals(4, bid.getRemainingQty());
        Assert.assertNotNull(books.getBook(securityID).getBestBid());
    }

    @Test
    public void testReplaceMatchFullyFilled() {
        SequencerOrder offer = insertOrder(false, 2, 100, true);
        SequencerOrder bid = insertOrder(true, 1, 99, true);

        boolean reinsert = books.buildReplace(bid, getReplace(1, 100, false));
        books.replaceOrder(bid, reinsert);

        Mockito.verify(listener).onMatch(1, 1, 2, 1, 100, 1, true, true, false);
        Assert.assertNull(books.getBook(securityID).getBestBid());
    }

    @Test
    public void testReplaceDownNoChangeInExternalOrderID() {
        SequencerOrder offer = insertOrder(false, 2, 100, true);

        Assert.assertEquals(1, offer.getExternalOrderID());

        boolean reinsert = books.buildReplace(offer, getReplace(1, 100, false));
        books.replaceOrder(offer, reinsert);

        Assert.assertEquals(1, offer.getExternalOrderID());
    }

    @Test
    public void testReplacePriceChangeInExternalOrderID() {
        SequencerOrder offer = insertOrder(false, 2, 100, true);
        Assert.assertEquals(1, offer.getExternalOrderID());

        boolean reinsert = books.buildReplace(offer, getReplace(1, 101, false));
        books.replaceOrder(offer, reinsert);
        Assert.assertTrue(reinsert);
        Assert.assertEquals(2, offer.getExternalOrderID());

        reinsert = books.buildReplace(offer, getReplace(1, 102, false));
        books.replaceOrder(offer, reinsert);
        Assert.assertTrue(reinsert);
        Assert.assertEquals(3, offer.getExternalOrderID());
    }

    @Test
    public void testReplaceQtyUpChangeInExternalOrderID() {
        SequencerOrder offer = insertOrder(false, 1, 100, true);
        Assert.assertEquals(1, offer.getExternalOrderID());

        boolean reinsert = books.buildReplace(offer, getReplace(2, 100, false));
        books.replaceOrder(offer, reinsert);
        Assert.assertTrue(reinsert);
        Assert.assertEquals(2, offer.getExternalOrderID());

        reinsert = books.buildReplace(offer, getReplace(3, 100, false));
        books.replaceOrder(offer, reinsert);
        Assert.assertTrue(reinsert);
        Assert.assertEquals(3, offer.getExternalOrderID());
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

        Assert.assertEquals(1, order.getExternalOrderID());

        Assert.assertNull(book.getBestBid());
        Assert.assertNull(book.getBestOffer());
    }

    private SequencerOrder insertOrder(boolean buy, int qty, long price, boolean passive) {
        MatchOrderCommand msg = msgs.getMatchOrderCommand();
        msg.setBuy(buy);
        msg.setQty(qty);
        msg.setSecurityID(securityID);
        msg.setPrice(price);
        msg.setOrderID(1);
        msg.setInBook(passive);
        SequencerOrder order = books.buildOrder(msg.toEvent());
        books.addOrder(order);
        return order;
    }

    private MatchReplaceEvent getReplace(int newQty, long newPrice, boolean passive) {
        MatchReplaceCommand cmd = msgs.getMatchReplaceCommand();
        cmd.setPrice(newPrice);
        cmd.setQty(newQty);
        cmd.setInBook(passive);
        return cmd.toEvent();
    }
}
