package com.core.match.sequencer;

import com.core.match.msgs.MatchFillEvent;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jgreco on 7/27/15.
 */
public class OrderCommandHandlerFillTest extends HandlerTestBase {
    @Test
    public void testMatchLastFill() {
        orderCmdHandler.onMatch(44, 1, 2, 10, 100, 4, true, false, false);

        MatchFillEvent fill1 = getFirstMessage(MatchFillEvent.class);
        MatchFillEvent fill2 = getFirstMessage(MatchFillEvent.class);

        Assert.assertEquals(1, fill1.getOrderID());
        Assert.assertEquals(10, fill1.getQty());
        Assert.assertEquals(100, fill1.getPrice());
        Assert.assertEquals(44, fill1.getMatchID());
        Assert.assertFalse(fill1.getLastFill());

        Assert.assertEquals(2, fill2.getOrderID());
        Assert.assertEquals(10, fill2.getQty());
        Assert.assertEquals(100, fill2.getPrice());
        Assert.assertEquals(44, fill2.getMatchID());
        Assert.assertTrue(fill2.getLastFill());

        Assert.assertFalse(sender.isFlushed());
    }

    @Test
    public void testMatchNotLastFill() {
        orderCmdHandler.onMatch(44, 1, 2, 10, 100, 4, false, false, false);

        MatchFillEvent fill1 = getFirstMessage(MatchFillEvent.class);
        MatchFillEvent fill2 = getFirstMessage(MatchFillEvent.class);

        Assert.assertEquals(1, fill1.getOrderID());
        Assert.assertEquals(10, fill1.getQty());
        Assert.assertEquals(100, fill1.getPrice());
        Assert.assertEquals(44, fill1.getMatchID());
        Assert.assertFalse(fill1.getLastFill());

        Assert.assertEquals(2, fill2.getOrderID());
        Assert.assertEquals(10, fill2.getQty());
        Assert.assertEquals(100, fill2.getPrice());
        Assert.assertEquals(44, fill2.getMatchID());
        Assert.assertFalse(fill2.getLastFill());

        Assert.assertFalse(sender.isFlushed());
    }

    @Test
    public void testMatchFlush() {
        orderCmdHandler.onMatch(44, 1, 2, 10, 100, 20, false, false, false);

        MatchFillEvent fill1 = getFirstMessage(MatchFillEvent.class);
        MatchFillEvent fill2 = getFirstMessage(MatchFillEvent.class);

        Assert.assertEquals(1, fill1.getOrderID());
        Assert.assertEquals(10, fill1.getQty());
        Assert.assertEquals(100, fill1.getPrice());
        Assert.assertEquals(44, fill1.getMatchID());
        Assert.assertFalse(fill1.getLastFill());

        Assert.assertEquals(2, fill2.getOrderID());
        Assert.assertEquals(10, fill2.getQty());
        Assert.assertEquals(100, fill2.getPrice());
        Assert.assertEquals(44, fill2.getMatchID());
        Assert.assertFalse(fill2.getLastFill());

        Assert.assertTrue(sender.isFlushed());
    }
}
