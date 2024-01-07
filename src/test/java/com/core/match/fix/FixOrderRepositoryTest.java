package com.core.match.fix;

import com.core.match.msgs.MatchCancelCommand;
import com.core.match.msgs.MatchCancelReplaceRejectCommand;
import com.core.match.msgs.MatchFillCommand;
import com.core.match.msgs.MatchOrderCommand;
import com.core.match.msgs.MatchOrderRejectCommand;
import com.core.match.msgs.MatchReplaceCommand;
import com.core.match.msgs.MatchTestMessages;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 1/3/15.
 */
public class FixOrderRepositoryTest {
    private static short CONTRIB_ID = 1;
    private FixOrderRepository store;
    private MatchTestMessages messages;

    @Before
    public void before() {
        messages = new MatchTestMessages();
        store = new FixOrderRepository(100);
    }

    @Test
    public void testOrder() {
        MatchOrderCommand msg = messages.getMatchOrderCommand();
        msg.setClOrdID(wrap("FOO"));
        //msg.setContributorID((short)1);

        FixOrder order = newOrder();
        store.onOrder(order, msg.toEvent());

        Assert.assertEquals(order, store.getOrder(wrap("FOO")));
        Assert.assertNull(store.getOrder(wrap("BAR")));
        Assert.assertTrue(store.seenClOrdID(wrap("FOO")));
    }

    @Test
    public void testCancel() {
        FixOrder order = newOrder();

        MatchOrderCommand orderMsg = messages.getMatchOrderCommand();
        orderMsg.setClOrdID(wrap("FOO"));

        MatchCancelCommand cancelMsg = messages.getMatchCancelCommand();
        cancelMsg.setClOrdID(wrap("BAR"));
        cancelMsg.setOrigClOrdID(wrap("FOO"));

        store.onOrder(order, orderMsg.toEvent());
        store.onCancel(order, cancelMsg.toEvent());

        Assert.assertNull(store.getOrder(wrap("FOO")));
        Assert.assertTrue(store.seenClOrdID(wrap("FOO")));
        Assert.assertTrue(store.seenClOrdID(wrap("BAR")));
    }

    @Test
    public void testReplace() {
        FixOrder order = newOrder();

        MatchOrderCommand orderMsg = messages.getMatchOrderCommand();
        orderMsg.setClOrdID(wrap("FOO"));

        MatchReplaceCommand replaceMsg = messages.getMatchReplaceCommand();
        replaceMsg.setClOrdID(wrap("BAR"));
        replaceMsg.setOrigClOrdID(wrap("FOO"));

        store.onOrder(order, orderMsg.toEvent());
        store.onReplace(order, replaceMsg.toEvent(), null);

        Assert.assertNull(store.getOrder(wrap("FOO")));
        Assert.assertEquals(order, store.getOrder(wrap("BAR")));
        Assert.assertTrue(store.seenClOrdID(wrap("FOO")));
        Assert.assertTrue(store.seenClOrdID(wrap("BAR")));
    }

    @Test
    public void testFill() {
        FixOrder order = newOrder();

        MatchOrderCommand orderMsg = messages.getMatchOrderCommand();
        orderMsg.setClOrdID(wrap("FOO"));

        MatchFillCommand fillMsg = messages.getMatchFillCommand();
        fillMsg.setQty(5);

        store.onOrder(order, orderMsg.toEvent());

        order.addCumQty(5);
        store.onFill(order, fillMsg.toEvent());

        Assert.assertEquals(order, store.getOrder(wrap("FOO")));
        Assert.assertTrue(store.seenClOrdID(wrap("FOO")));

        order.addCumQty(5);
        store.onFill(order, fillMsg.toEvent());
        Assert.assertNull(store.getOrder(wrap("FOO")));
    }

    @Test
    public void testUnsolicitedCancel() {
        FixOrder order = newOrder();

        MatchOrderCommand orderMsg = messages.getMatchOrderCommand();
        orderMsg.setClOrdID(wrap("FOO"));

        MatchCancelCommand cancelMsg = messages.getMatchCancelCommand();
        cancelMsg.setOrigClOrdID(wrap("FOO"));

        store.onOrder(order, orderMsg.toEvent());
        store.onCancel(order, cancelMsg.toEvent());

        Assert.assertNull(store.getOrder(wrap("FOO")));
        Assert.assertTrue(store.seenClOrdID(wrap("FOO")));
    }

    @Test
    public void testReject() {
        MatchOrderRejectCommand rejectMsg = messages.getMatchOrderRejectCommand();
        rejectMsg.setContributorID(CONTRIB_ID);
        rejectMsg.setClOrdID(wrap("FOO"));

        store.onOrderReject(rejectMsg.toEvent());

        Assert.assertNull(store.getOrder(wrap("FOO")));
        Assert.assertTrue(store.seenClOrdID(wrap("FOO")));
    }

    @Test
    public void testCancelReplaceReject() {
        FixOrder order = newOrder();

        MatchOrderCommand orderMsg = messages.getMatchOrderCommand();
        orderMsg.setClOrdID(wrap("FOO"));
        store.onOrder(order, orderMsg.toEvent());

        MatchCancelReplaceRejectCommand rejectMsg = messages.getMatchCancelReplaceRejectCommand();
        rejectMsg.setContributorID(CONTRIB_ID);
        rejectMsg.setClOrdID(wrap("BAR"));
        rejectMsg.setOrigClOrdID(wrap("FOO"));
        store.onCancelReplaceReject(order, rejectMsg.toEvent());

        Assert.assertEquals(order, store.getOrder(wrap("FOO")));
        Assert.assertTrue(store.seenClOrdID(wrap("FOO")));
        Assert.assertTrue(store.seenClOrdID(wrap("BAR")));
    }

    private static FixOrder newOrder() {
        FixOrder fixOrder = new FixOrder();
        fixOrder.clear();
        fixOrder.setQty(10);
        return fixOrder;
    }

    private static ByteBuffer wrap(String clordid) {
        return ByteBuffer.wrap(clordid.getBytes());
    }


}
