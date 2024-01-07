package com.core.match.sequencer;

import com.core.GenericTest;
import com.core.match.msgs.MatchByteBufferMessages;
import com.core.match.msgs.MatchFillCommand;
import com.core.match.msgs.MatchMessages;
import com.core.match.msgs.MatchOrderCommand;
import com.core.match.msgs.MatchReplaceCommand;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertTrue;

/**
 * Created by hli on 2/6/16.
 */
public class SequencerBackupTests  extends GenericTest {
    private SequencerLimitBookService books;
    private SequencerLimitBookService booksBackup;

    private SequencerBookServiceListener listener;
    private SequencerBookServiceListener listenerBackup;

    private SequencerOrderService orders;
    private SequencerOrderService ordersBackup;

    private short securityID = 1;
    private MatchMessages msgs = new MatchByteBufferMessages();

    @Before
    public void before() {
        listener = Mockito.mock(SequencerBookServiceListener.class);
        listenerBackup = Mockito.mock(SequencerBookServiceListener.class);

        orders = new SequencerOrderService(log);
        ordersBackup = new SequencerOrderService(log);

        books = new SequencerLimitBookService( log,orders);
        books.addBook();

        books.setListener(listener);
        booksBackup = new SequencerLimitBookService( log,orders);
        booksBackup.addBook();
        booksBackup.setListener(listenerBackup);
    }

    @Test
    public void testAddOrderToActiveAndPassive_SameIDUpdated() {
        SequencerOrder order = insertOrder(true, 1, 100);
        int externalOrderID=order.getExternalOrderID();
        SequencerOrder orderbackup = insertOrderForBackupSequencer(true, 1, 100,order.getID(),externalOrderID);
        Assert.assertEquals(externalOrderID,orderbackup.getExternalOrderID());

        Assert.assertEquals(books.getNextMatchID(), booksBackup.getNextMatchID());
        Assert.assertEquals(books.getNextExternalOrderID(), booksBackup.getNextExternalOrderID());
        Assert.assertEquals(books.getNextOrderID(), booksBackup.getNextOrderID());
    }

    @Test
    public void testFill_ToActiveAndPassive_SameIDUpdated() {
        SequencerOrder order = insertOrder(true, 1, 100);
        int externalOrderID=order.getExternalOrderID();
        SequencerOrder orderbackup = insertOrderForBackupSequencer(true, 1, 100, order.getID(), externalOrderID);
        Assert.assertEquals(externalOrderID, orderbackup.getExternalOrderID());

        SequencerOrder aggressOrder = insertOrder(false, 1, 100);
        Mockito.verify(listener).onMatch(1, 1, 2, 1, 100, 1, true, false, false);

        SequencerOrder aggressOrderBackup = insertOrderForBackupSequencer(false, 1, 100, aggressOrder.getID(), aggressOrder.getExternalOrderID());//backup sees an aggressing order

        MatchFillCommand backupMatchFillEventPassive=msgs.getMatchFillCommand();
        backupMatchFillEventPassive.setLastFill(true);
        backupMatchFillEventPassive.setPassive(true);
        backupMatchFillEventPassive.setMatchID(1);

        MatchFillCommand backupMatchFillEventAggress=msgs.getMatchFillCommand();
        backupMatchFillEventPassive.setLastFill(true);
        backupMatchFillEventPassive.setPassive(false);
        backupMatchFillEventPassive.setMatchID(1);

        Assert.assertEquals(books.getNextMatchID(), booksBackup.getNextMatchID());
        Assert.assertEquals(books.getNextExternalOrderID(), booksBackup.getNextExternalOrderID());
        Assert.assertEquals(books.getNextOrderID(), booksBackup.getNextOrderID());
    }

    @Test
    public void testReplaceOrderToActiveAndPassive_SameIDUpdated() {
        SequencerOrder order = insertOrder(true, 1, 100);
        int externalOrderID=order.getExternalOrderID();
        SequencerOrder orderbackup = insertOrderForBackupSequencer(true, 1, 100, order.getID(), externalOrderID);

        MatchReplaceCommand replaceCommandActive=msgs.getMatchReplaceCommand();
        replaceCommandActive.setPrice(101);
        replaceCommandActive.setExternalOrderID(0);
        replaceCommandActive.setQty(1);

        MatchReplaceCommand replaceCommandBk=msgs.getMatchReplaceCommand();
        replaceCommandBk.setPrice(101);
        replaceCommandBk.setExternalOrderID(2);
        replaceCommandBk.setQty(1);

        boolean reinsert = books.buildReplace(order, replaceCommandActive.toEvent());// price is changed hence we reinsert into the book
        boolean reinsertBackup = booksBackup.buildReplace(orderbackup, replaceCommandBk.toEvent());// receive new displayOrderID of 2 in the event due to the reinsert

        assertTrue(reinsert);
        assertTrue(reinsertBackup);

        Assert.assertEquals(books.getNextMatchID(),booksBackup.getNextMatchID());
        Assert.assertEquals(books.getNextExternalOrderID(),booksBackup.getNextExternalOrderID());
        Assert.assertEquals(books.getNextOrderID(),booksBackup.getNextOrderID());
    }

    @Test
    public void testReplaceOrderToActiveAndPassive_CancelOrder_SameIDUpdated() {
        SequencerOrder order = insertOrder(true, 1, 100);
        int externalOrderID=order.getExternalOrderID();
        SequencerOrder orderbackup = insertOrderForBackupSequencer(true, 1, 100, order.getID(),externalOrderID);

        MatchReplaceCommand replaceCommandActive=msgs.getMatchReplaceCommand();
        replaceCommandActive.setPrice(101);
        replaceCommandActive.setExternalOrderID(0);
        replaceCommandActive.setQty(1);

        MatchReplaceCommand replaceCommandBk=msgs.getMatchReplaceCommand();
        replaceCommandBk.setPrice(101);
        replaceCommandBk.setExternalOrderID(2);
        replaceCommandBk.setQty(1);

        boolean reinsert = books.buildReplace(order, replaceCommandActive.toEvent());// price is changed hence we reinsert into the book
        boolean reinsertBackup = booksBackup.buildReplace(orderbackup, replaceCommandBk.toEvent());// receive new displayOrderID of 2 in the event due to the reinsert

        booksBackup.cancelOrder(orderbackup);
        books.cancelOrder(order);

        Assert.assertEquals(books.getNextMatchID(),booksBackup.getNextMatchID());
        Assert.assertEquals(books.getNextExternalOrderID(),booksBackup.getNextExternalOrderID());
        Assert.assertEquals(books.getNextOrderID(),booksBackup.getNextOrderID());
    }

    private SequencerOrder insertOrder(boolean buy, int qty, long price) {
        MatchOrderCommand msg = msgs.getMatchOrderCommand();
        msg.setBuy(buy);
        msg.setQty(qty);
        msg.setSecurityID(securityID);
        msg.setPrice(price);
        SequencerOrder order = books.buildOrder(msg.toEvent());
        books.addOrder(order);
        return order;
    }

    private SequencerOrder insertOrderForBackupSequencer(boolean buy, int qty, long price, int orderID, int externalOrderID) {
        MatchOrderCommand msg = msgs.getMatchOrderCommand();
        msg.setBuy(buy);
        msg.setQty(qty);
        msg.setSecurityID(securityID);
        msg.setPrice(price);
        msg.setOrderID(orderID);
        msg.setExternalOrderID(externalOrderID);
        SequencerOrder order = booksBackup.buildOrder(msg.toEvent());
        booksBackup.addOrder(order);
        return order;
    }
}