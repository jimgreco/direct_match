package com.core.match.sequencer;

import com.core.match.msgs.MatchCancelCommand;
import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchCancelReplaceRejectEvent;
import com.core.match.msgs.MatchConstants;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by jgreco on 7/26/15.
 */
public class OrderCommandHandlerCancelTest extends HandlerTestBase {
    @Test
    public void testCancelOrder()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));

        SequencerOrder order = new SequencerOrder();
        Mockito.when(books.getOrder(1)).thenReturn(order);

        MatchCancelCommand command = messages.getMatchCancelCommand();
        command.setClOrdID("1234");
        command.setOrderID(1);
        command.setContributorSeq(1);
        command.setContributorID((short) 1) ;

        orderCmdHandler.onMatchCancel(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Mockito.verify(books).cancelOrder(order);
        Assert.assertTrue(sender.isFlushed());
    }

    @Test
    public void testCancelBadSequenceNumber()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));

        SequencerOrder order = new SequencerOrder();
        Mockito.when(books.getOrder(1)).thenReturn(order);

        MatchCancelCommand command = messages.getMatchCancelCommand();
        command.setClOrdID("1234");
        command.setOrderID(1);
        command.setContributorSeq(5);
        command.setContributorID((short) 1) ;

        orderCmdHandler.onMatchCancel(command.toEvent());

        Assert.assertEquals(0, contribs.getSeqNum(1));
    }

    @Test
    public void testCancelNoOrder()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));

        MatchCancelCommand command = messages.getMatchCancelCommand();
        command.setClOrdID("1234");
        command.setOrderID(1);
        command.setContributorSeq(1);
        command.setContributorID((short) 1) ;

        orderCmdHandler.onMatchCancel(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Assert.assertEquals(MatchConstants.OrderRejectReason.UnknownOrderID, getFirstMessage(MatchCancelReplaceRejectEvent.class).getReason());
    }

    @Test
    public void testForceCancel()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));

        SequencerOrder order = new SequencerOrder();
        Mockito.when(books.getOrder(1)).thenReturn(order);

        Assert.assertTrue(orderCmdHandler.forceCancel(1));

        Assert.assertEquals(1, getFirstMessage(MatchCancelEvent.class).getOrderID());
        Assert.assertEquals(1, contribs.getSeqNum(1));
    }

    @Test
    public void testForceCancelUnknownOrder()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));

        Assert.assertFalse(orderCmdHandler.forceCancel(1));

        Assert.assertEquals(0, contribs.getSeqNum(1));
    }
}
