package com.core.match.sequencer;

import com.core.match.msgs.MatchCancelReplaceRejectEvent;
import com.core.match.msgs.MatchClientCancelReplaceRejectCommand;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchReplaceCommand;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.util.PriceUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by jgreco on 7/26/15.
 */
@SuppressWarnings("boxing")
public class OrderCommandHandlerReplaceTest extends HandlerTestBase {
    @Test
    public void testOrderPassive()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));

        long price = PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS);

        SequencerOrder order = new SequencerOrder();
        order.securityID = 1;
        order.qty = 10;
        order.price = price;
        Mockito.when(books.getOrder(1)).thenReturn(order);

        MatchReplaceCommand command = messages.getMatchReplaceCommand();
        command.setClOrdID("1234");
        command.setOrderID(1);
        command.setContributorSeq(1);
        command.setPrice(price);
        command.setQty(1000);
        command.setContributorID((short) 1) ;

        Mockito.when(books.buildReplace(order, command.toEvent())).thenReturn(true);

        orderCmdHandler.onMatchReplace(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        MatchReplaceEvent lastMsg = getFirstMessage(MatchReplaceEvent.class);
        Assert.assertEquals(1000, lastMsg.getQty());
        Assert.assertEquals(price, lastMsg.getPrice());
        Mockito.verify(books).buildReplace(order, command.toEvent());
        Mockito.verify(books).replaceOrder(order, true);
        Assert.assertTrue(sender.isFlushed());
    }

    @Test
    public void testReplaceBadSequenceNumber()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));

        SequencerOrder order = new SequencerOrder();
        Mockito.when(books.getOrder(1)).thenReturn(order);

        MatchReplaceCommand command = messages.getMatchReplaceCommand();
        command.setClOrdID("1234");
        command.setOrderID(1);
        command.setContributorSeq(5);
        command.setPrice(com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(100);
        command.setContributorID((short) 1) ;

        orderCmdHandler.onMatchReplace(command.toEvent());

        Assert.assertEquals(0, contribs.getSeqNum(1));
    }

    @Test
    public void testReplaceBadQty()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));

        SequencerOrder order = new SequencerOrder();
        order.securityID = (short) 1;
        order.qty = 100;
        order.price = com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS);
        Mockito.when(books.getOrder(1)).thenReturn(order);

        MatchReplaceCommand command = messages.getMatchReplaceCommand();
        command.setClOrdID("1234");
        command.setOrderID(1);
        command.setContributorSeq(1);
        command.setPrice(com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(100);
        command.setContributorID((short) 1) ;

        orderCmdHandler.onMatchReplace(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Assert.assertEquals(MatchConstants.OrderRejectReason.InvalidQuantity, getFirstMessage(MatchCancelReplaceRejectEvent.class).getReason());
    }

    @Test
    public void testReplaceNoOrder()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));

        MatchReplaceCommand command = messages.getMatchReplaceCommand();
        command.setClOrdID("1234");
        command.setOrderID(1);
        command.setContributorSeq(1);
        command.setPrice(com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(100);
        command.setContributorID((short) 1) ;

        orderCmdHandler.onMatchReplace(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Assert.assertEquals(MatchConstants.OrderRejectReason.UnknownOrderID, getFirstMessage(MatchCancelReplaceRejectEvent.class).getReason());
    }

    @Test
    public void testReplaceNoQtyChange()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));

        SequencerOrder order = new SequencerOrder();
        order.securityID = (short) 1;
        order.qty = 100;
        order.price = com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS);
        Mockito.when(books.getOrder(1)).thenReturn(order);

        MatchReplaceCommand command = messages.getMatchReplaceCommand();
        command.setClOrdID("1234");
        command.setOrderID(1);
        command.setContributorSeq(1);
        command.setPrice(com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(0);
        command.setContributorID((short) 1) ;

        orderCmdHandler.onMatchReplace(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Assert.assertEquals(MatchConstants.OrderRejectReason.InvalidQuantity, getFirstMessage(MatchCancelReplaceRejectEvent.class).getReason());
    }

    @Test
    public void testReplaceNoQtyChange_causingReject()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));

        SequencerOrder order = new SequencerOrder();
        order.securityID = (short) 1;
        order.qty = 10000;
        order.price = com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS);
        Mockito.when(books.getOrder(1)).thenReturn(order);

        MatchReplaceCommand command = messages.getMatchReplaceCommand();
        command.setClOrdID("1234");
        command.setOrderID(1);
        command.setContributorSeq(1);
        command.setPrice(com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(10000);
        command.setContributorID((short) 1) ;

        orderCmdHandler.onMatchReplace(command.toEvent());

        Assert.assertEquals(MatchConstants.OrderRejectReason.InvalidQuantity, getFirstMessage(MatchCancelReplaceRejectEvent.class).getReason());
    }

    @Test
    public void testReplaceBadPrice1()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));

        SequencerOrder order = new SequencerOrder();
        order.securityID = (short) 1;
        order.qty = 100;
        order.price = com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS);
        Mockito.when(books.getOrder(1)).thenReturn(order);

        MatchReplaceCommand command = messages.getMatchReplaceCommand();
        command.setClOrdID("1234");
        command.setOrderID(1);
        command.setContributorSeq(1);
        command.setPrice(com.core.util.PriceUtils.toLong(0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(100);
        command.setContributorID((short) 1) ;

        orderCmdHandler.onMatchReplace(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Assert.assertEquals(MatchConstants.OrderRejectReason.InvalidPrice, getFirstMessage(MatchCancelReplaceRejectEvent.class).getReason());
    }

    @Test
    public void testReplaceBadPrice2()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));

        SequencerOrder order = new SequencerOrder();
        order.securityID = (short) 1;
        order.qty = 100;
        order.price = com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS);
        Mockito.when(books.getOrder(1)).thenReturn(order);

        MatchReplaceCommand command = messages.getMatchReplaceCommand();
        command.setClOrdID("1234");
        command.setOrderID(1);
        command.setContributorSeq(1);
        command.setPrice(com.core.util.PriceUtils.toLong(.333333, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(1000);
        command.setContributorID((short) 1) ;

        orderCmdHandler.onMatchReplace(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Assert.assertEquals(MatchConstants.OrderRejectReason.InvalidPrice, getFirstMessage(MatchCancelReplaceRejectEvent.class).getReason());
    }

	@Test
    public void testClientCancelReplaceReject()
    {
        Mockito.when(books.numOrders()).thenReturn((long) MatchConstants.MAX_LIVE_ORDERS);

        MatchClientCancelReplaceRejectCommand command = messages.getMatchClientCancelReplaceRejectCommand();
        command.setContributorID((short) 1) ;
        command.setContributorSeq((short) 1);
        command.setOrderID(152);
        command.setClOrdID("4567");
        command.setClOrdID("1234");
        command.setText("WTF");

        orderCmdHandler.onMatchClientCancelReplaceReject(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Assert.assertTrue(sender.isFlushed());
    }

    @Test
    public void testClientCancelReplaceRejectBadSeqNum()
    {
        Mockito.when(books.numOrders()).thenReturn((long) MatchConstants.MAX_LIVE_ORDERS);

        MatchClientCancelReplaceRejectCommand command = messages.getMatchClientCancelReplaceRejectCommand();
        command.setContributorID((short) 1) ;
        command.setContributorSeq((short) 2);
        command.setOrderID(152);
        command.setClOrdID("4567");
        command.setClOrdID("1234");
        command.setText("WTF");

        orderCmdHandler.onMatchClientCancelReplaceReject(command.toEvent());

        Assert.assertEquals(0, contribs.getSeqNum(1));
        Assert.assertFalse(sender.isFlushed());
    }
}
