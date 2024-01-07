package com.core.services.limit;

import com.core.match.msgs.MatchConstants;
import org.junit.Assert;
import org.junit.Test;

/**
 * User: jgreco
 */

@SuppressWarnings("static-method")
public class LimitBookTest {
	@Test
    public void testInsertOrder() {
        LimitBook<BookTestOrder> book = getBook();
        BookTestOrder order = insertOrder(book, true, 1, 100);
        BookTestOrder firstBid = book.getBestBid();

        Assert.assertEquals(order, firstBid);
        Assert.assertNull(firstBid.next());
        Assert.assertNull(book.getBestOffer());
    }

    @Test
    public void testLayerBids() {
        LimitBook<BookTestOrder> book = getBook();
        BookTestOrder order2 = insertOrder(book, true, 2, 100);
        BookTestOrder order0 = insertOrder(book, true, 1, 101);
        BookTestOrder order1 = insertOrder(book, true, 1, 101);
        BookTestOrder order4 = insertOrder(book, true, 4, 99);
        BookTestOrder order3 = insertOrder(book, true, 3, 100);
        BookTestOrder order5 = insertOrder(book, true, 1, 99);

        BookTestOrder next = book.getBestBid();
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
        System.out.println(book.toString());

    }

    @Test
    public void testLayerOffers() {
    	LimitBook<BookTestOrder> book = getBook();
        BookTestOrder order2 = insertOrder(book, false, 2, 100);
        BookTestOrder order0 = insertOrder(book, false, 1, 99);
        BookTestOrder order1 = insertOrder(book, false, 1, 99);
        BookTestOrder order4 = insertOrder(book, false, 4, 101);
        BookTestOrder order3 = insertOrder(book, false, 3, 100);
        BookTestOrder order5 = insertOrder(book, false, 3, 101);

        BookTestOrder next = book.getBestOffer();
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
        System.out.println(book.toString());

    }

	@Test
    public void testDeleteBids() {
    	LimitBook<BookTestOrder> book = getBook();
        BookTestOrder order2 = insertOrder(book, true, 2, 100);
        BookTestOrder order0 = insertOrder(book, true, 1, 101);
        BookTestOrder order1 = insertOrder(book, true, 1, 101);
        BookTestOrder order4 = insertOrder(book, true, 4, 99);
        BookTestOrder order3 = insertOrder(book, true, 3, 100);
        BookTestOrder order5 = insertOrder(book, true, 1, 99);

        book.removeOrder(order0);
        System.out.println(book.toString());

        BookTestOrder next = book.getBestBid();
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
        System.out.println(book.toString());

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
        System.out.println(book.toString());

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
        System.out.println(book.toString());

    }

	@Test
    public void testDeleteOffers() {
        LimitBook<BookTestOrder> book = getBook();
        BookTestOrder order2 = insertOrder(book, false, 2, 100);
        BookTestOrder order0 = insertOrder(book, false, 1, 99);
        BookTestOrder order1 = insertOrder(book, false, 1, 99);
        BookTestOrder order4 = insertOrder(book, false, 4, 101);
        BookTestOrder order3 = insertOrder(book, false, 3, 100);
        BookTestOrder order5 = insertOrder(book, false, 3, 101);

        book.removeOrder(order0);

        BookTestOrder next = book.getBestOffer();
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
    public void testFindQueueSpot() {
        LimitBook<BookTestOrder> book = getBook();
        BookTestOrder order2 = insertOrderCheckQSpot(0, book, true, 2, 100);
        BookTestOrder order0 = insertOrderCheckQSpot(0, book, true, 1, 101);
        BookTestOrder order1 = insertOrderCheckQSpot(1, book, true, 1, 101);
        BookTestOrder order4 = insertOrderCheckQSpot(3, book, true, 4, 99);
        BookTestOrder order3 = insertOrderCheckQSpot(3, book, true, 3, 100);
        BookTestOrder order5 = insertOrderCheckQSpot(5, book, true, 1, 99);

        Assert.assertEquals(0, book.getQueuePosition(order0, 10));
        Assert.assertEquals(1, book.getQueuePosition(order1, 10));
        Assert.assertEquals(2, book.getQueuePosition(order2, 10));
        Assert.assertEquals(3, book.getQueuePosition(order3, 10));
        Assert.assertEquals(4, book.getQueuePosition(order4, 10));
        Assert.assertEquals(5, book.getQueuePosition(order5, 10));
    }


    @Test
    public void testFindTooLargeQueueSpot() {
        LimitBook<BookTestOrder> book = getBook();
        BookTestOrder order2 = insertOrderCheckQSpot(0, book, true, 2, 100);
        BookTestOrder order0 = insertOrderCheckQSpot(0, book, true, 1, 101);
        BookTestOrder order1 = insertOrderCheckQSpot(1, book, true, 1, 101);
        BookTestOrder order4 = insertOrderCheckQSpot(3, book, true, 4, 99);
        BookTestOrder order3 = insertOrderCheckQSpot(3, book, true, 3, 100);
        BookTestOrder order5 = insertOrderCheckQSpot(5, book, true, 1, 99);

        Assert.assertEquals(0, book.getQueuePosition(order0, 3));
        Assert.assertEquals(1, book.getQueuePosition(order1, 3));
        Assert.assertEquals(2, book.getQueuePosition(order2, 3));
        Assert.assertEquals(3, book.getQueuePosition(order3, 3));
        Assert.assertEquals(3, book.getQueuePosition(order4, 3));
        Assert.assertEquals(3, book.getQueuePosition(order5, 3));
    }

    private static LimitBook<BookTestOrder> getBook() {
        return new LimitBook<>(MatchConstants.IMPLIED_DECIMALS, MatchConstants.QTY_MULTIPLIER);
    }

    private static BookTestOrder insertOrderCheckQSpot(int qspot, LimitBook<BookTestOrder> book, boolean buy, int qty, long price) {
        BookTestOrder order = new BookTestOrder();

        order.buy = buy;
        order.qty = qty;
        order.price = price;

        Assert.assertEquals(qspot, book.insertOrder(order));
        return order;
    }

    private static BookTestOrder insertOrder(LimitBook<BookTestOrder> book, boolean buy, int qty, long price) {
        BookTestOrder order = new BookTestOrder();

        order.buy = buy;
        order.qty = qty;
        order.price = price;

        book.insertOrder(order);
        return order;
    }

    public static class BookTestOrder implements LimitOrder<BookTestOrder> {
        boolean buy;
        long price;
        int qty;
        int cumQty;

        BookTestOrder prev;
        BookTestOrder next;

        @Override
        public boolean isBuy() {
            return buy;
        }

        @Override
        public long getPrice() {
            return price;
        }

        public int getQty() {
            return qty;
        }

        @Override
        public int getRemainingQty() {
            return qty-cumQty;
        }

        @Override
        public BookTestOrder next() {
            return next;
        }

        @Override
        public BookTestOrder prev() {
            return prev;
        }

        @Override
        public void setNext(BookTestOrder next) {
            this.next = next;
        }

        @Override
        public void setPrev(BookTestOrder prev) {
            this.prev = prev;
        }

        @Override
        public int compare(BookTestOrder item) {
            if (getPrice() == item.getPrice()) {
                return 0;
            }
            if (isBuy()) {
                return getPrice() < item.getPrice() ? 1 : -1;
            }
            return getPrice() > item.getPrice() ? 1 : -1;
        }
    }
}
