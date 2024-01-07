package com.core.match.services.book;

import com.core.match.GenericAppTest;
import com.core.match.services.order.DisplayedOrderService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by jgreco on 2/23/16.
 */
public class BookPositionServiceTest extends GenericAppTest<MatchLimitOrder> {
    private BookPositionService books;
    private BookPositionServiceListener listener;
    private int levels;

    public BookPositionServiceTest() {
        super(MatchLimitOrder.class);
    }

    @Before
    public void setup() {
        levels = 5;
        DisplayedOrderService<MatchLimitOrder> displayedOS = new DisplayedOrderService<>(orders, log);
        books = new BookPositionService(displayedOS, securities, levels);

        listener = Mockito.mock(BookPositionServiceListener.class);
        books.addListener(listener);

        sendAccount("DM");
        sendTrader("JIM", "DM");
        sendBond("5Y");
        sendBond("10Y");

        Mockito.verify(listener).onBookDefined(securities.get("5Y"), levels);
        Mockito.verify(listener).onBookDefined(securities.get("10Y"), levels);

        timeSource.setTimestamp(0);
    }

    @Test
    public void testAddOne() {
        int orderID0 = sendPassiveOrder("JIM", true, 10, "10Y", 100.0);
        Mockito.verify(listener).onOrderChange(0,  orders.get(orderID0), 0);
        long ts=0;
        short secID=2;
        for (int i=1; i<levels; i++) {

            Mockito.verify(listener).onNoOrder(i,  true, secID,ts);
        }

        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testAddMultiple() {
        int orderID1 = sendPassiveOrder("JIM", true, 3, "10Y", 100.0);
        int orderID0 = sendPassiveOrder("JIM", true, 2, "10Y", 101.0);
        int orderID3 = sendPassiveOrder("JIM", true, 7, "10Y", 99.0);
        int orderID2 = sendPassiveOrder("JIM", true, 5, "10Y", 100.0);
        int orderID4 = sendPassiveOrder("JIM", true, 3, "10Y", 98);

        Mockito.reset(listener);

        orderID4 = orderID3;
        orderID3 = sendPassiveOrder("JIM", true, 5, "10Y", 100.0);

        Mockito.verify(listener).onOrderChange(3,  orders.get(orderID3), 0);
        Mockito.verify(listener).onOrderChange(4,  orders.get(orderID4), 0);
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testAddOutside() {
        int orderID1 = sendPassiveOrder("JIM", true, 3, "10Y", 100.0);
        int orderID0 = sendPassiveOrder("JIM", true, 2, "10Y", 101.0);
        int orderID3 = sendPassiveOrder("JIM", true, 7, "10Y", 99.0);
        int orderID2 = sendPassiveOrder("JIM", true, 5, "10Y", 100.0);
        int orderID4 = sendPassiveOrder("JIM", true, 3, "10Y", 98);

        Mockito.reset(listener);

        int orderID5 = sendPassiveOrder("JIM", true, 5, "10Y", 97);
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testRemoveOutside() {
        int orderID1 = sendPassiveOrder("JIM", true, 3, "10Y", 100.0);
        int orderID0 = sendPassiveOrder("JIM", true, 2, "10Y", 101.0);
        int orderID3 = sendPassiveOrder("JIM", true, 7, "10Y", 99.0);
        int orderID2 = sendPassiveOrder("JIM", true, 5, "10Y", 100.0);
        int orderID4 = sendPassiveOrder("JIM", true, 3, "10Y", 98);
        int orderID5 = sendPassiveOrder("JIM", true, 5, "10Y", 97);

        Mockito.reset(listener);

        sendCancel(orderID5);
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testReplaceDownOutside() {
        int orderID1 = sendPassiveOrder("JIM", true, 3, "10Y", 100.0);
        int orderID0 = sendPassiveOrder("JIM", true, 2, "10Y", 101.0);
        int orderID3 = sendPassiveOrder("JIM", true, 7, "10Y", 99.0);
        int orderID2 = sendPassiveOrder("JIM", true, 5, "10Y", 100.0);
        int orderID4 = sendPassiveOrder("JIM", true, 3, "10Y", 98);
        int orderID5 = sendPassiveOrder("JIM", true, 5, "10Y", 97);

        Mockito.reset(listener);

        sendPassiveReplace(orderID5, 3, 97);
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testReplaceDownAffectsSingleOrder() {
        int orderID1 = sendPassiveOrder("JIM", true, 3, "10Y", 100.0);
        int orderID0 = sendPassiveOrder("JIM", true, 2, "10Y", 101.0);
        int orderID2 = sendPassiveOrder("JIM", true, 7, "10Y", 99.0);

        Mockito.reset(listener);

        sendPassiveReplace(orderID1, 1, 100.0);
        Mockito.verify(listener).onOrderChange(1,  orders.get(orderID1), 0);
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testRemoveInside() {
        int orderID1 = sendPassiveOrder("JIM", true, 3, "10Y", 100.0);
        int orderID0 = sendPassiveOrder("JIM", true, 2, "10Y", 101.0);
        int orderID3 = sendPassiveOrder("JIM", true, 7, "10Y", 99.0);
        int orderID2 = sendPassiveOrder("JIM", true, 5, "10Y", 100.0);
        int orderID4 = sendPassiveOrder("JIM", true, 3, "10Y", 98);
        int orderID5 = sendPassiveOrder("JIM", true, 5, "10Y", 97);

        Mockito.reset(listener);

        sendCancel(orderID3);

        Mockito.verify(listener).onOrderChange(3,  orders.get(orderID4), 0);
        Mockito.verify(listener).onOrderChange(4,  orders.get(orderID5), 0);

        Mockito.verifyNoMoreInteractions(listener);
    }
}
