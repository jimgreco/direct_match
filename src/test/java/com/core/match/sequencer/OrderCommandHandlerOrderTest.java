package com.core.match.sequencer;

import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchClientOrderRejectCommand;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchOrderCommand;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchOrderRejectEvent;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by jgreco on 7/22/15.
 */
@SuppressWarnings("boxing")
public class OrderCommandHandlerOrderTest extends HandlerTestBase {
    @Test
    public void testOrderPassive()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));

        MatchOrderCommand command = messages.getMatchOrderCommand();
        command.setBuy(true);
        command.setClOrdID("1234");
        command.setTraderID((short) 1);
        command.setOrderID(1);
        command.setSecurityID((short) 1);
        command.setContributorSeq(1);
        command.setPrice(com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(1000);
        command.setContributorID((short) 1) ;

        SequencerOrder order = new SequencerOrder();
        Mockito.when(books.buildOrder(command.toEvent())).thenReturn(order);

        orderCmdHandler.onMatchOrder(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Mockito.verify(books).buildOrder(command.toEvent());
        Mockito.verify(books).addOrder(order);
        Assert.assertTrue(sender.isFlushed());
    }


    @Test
    public void testOrderAggressive()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));

        MatchOrderCommand command = messages.getMatchOrderCommand();
        command.setBuy(true);
        command.setClOrdID("1234");
        command.setTraderID((short) 1);
        command.setOrderID(1);
        command.setSecurityID((short) 1);
        command.setContributorSeq(1);
        command.setPrice(com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(1000);
        command.setContributorID((short) 1) ;

        SequencerOrder order = new SequencerOrder();
        order.id = 5;
        Mockito.when(books.buildOrder(command.toEvent())).thenReturn(order);

        orderCmdHandler.onMatchOrder(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Mockito.verify(books).buildOrder(command.toEvent());
        Mockito.verify(books).addOrder(order);
        Assert.assertTrue(sender.isFlushed());
    }

    @Test
    public void testOrderIOC()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));

        MatchOrderCommand command = messages.getMatchOrderCommand();
        command.setBuy(true);
        command.setClOrdID("1234");
        command.setTraderID((short) 1);
        command.setOrderID(1);
        command.setSecurityID((short) 1);
        command.setContributorSeq(1);
        command.setPrice(com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(1000);
        command.setContributorID((short) 2) ;
        command.setIOC(true);

        SequencerOrder order = new SequencerOrder();
        order.id = 1;
        order.buy = true;
        order.securityID = (short)0;
        order.qty = 100;
        order.price = 100;
        order.cumQty = 90;

        Mockito.when(books.buildOrder(command.toEvent())).thenReturn(order);

        orderCmdHandler.onMatchOrder(command.toEvent());

        // sequencer
        Assert.assertEquals(1, contribs.getSeqNum(1));
        // order sender
        Assert.assertEquals(1, contribs.getSeqNum(2));

        MatchOrderEvent orderEvent = getFirstMessage(MatchOrderEvent.class);
        MatchCancelEvent cancelEvent = getFirstMessage(MatchCancelEvent.class);
        Assert.assertEquals(1, cancelEvent.getOrderID());

        Mockito.verify(books).buildOrder(command.toEvent());
        Mockito.verify(books).addOrder(order);
        Mockito.verify(books).deleteOrder(order);
        Assert.assertTrue(sender.isFlushed());
    }

    @Test
    public void testSetOrderParameters()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));

        MatchOrderCommand command = messages.getMatchOrderCommand();
        command.setBuy(true);
        command.setClOrdID("1234");
        command.setTraderID((short) 1);
        command.setOrderID(52523); // nonsense, doesn't matter
        command.setSecurityID((short) 1);
        command.setContributorSeq(1);
        command.setPrice(com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(1000);
        command.setContributorID((short) 1) ;

        SequencerOrder order = new SequencerOrder();
        order.id = 5;
        Mockito.when(books.buildOrder(command.toEvent())).thenReturn(order);

        orderCmdHandler.onMatchOrder(command.toEvent());

        MatchOrderEvent lastMsg = getFirstMessage(MatchOrderEvent.class);
        Assert.assertEquals(5, lastMsg.getOrderID());
        Assert.assertTrue(lastMsg.getBuy());
        Assert.assertEquals("1234", lastMsg.getClOrdIDAsString());
        Assert.assertEquals(1, lastMsg.getTraderID());
        Assert.assertEquals(1, lastMsg.getSecurityID());
        Assert.assertEquals(1, lastMsg.getContributorID());
        Assert.assertEquals(1, lastMsg.getContributorSeq());
        Assert.assertEquals(com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS), lastMsg.getPrice());
        Assert.assertEquals(1000, lastMsg.getQty());

        Mockito.verify(books).buildOrder(command.toEvent());
        Mockito.verify(books).addOrder(order);
        Assert.assertTrue(sender.isFlushed());
    }

    @Test
    public void testOrderBadSecurity()
    {
        MatchOrderCommand command = messages.getMatchOrderCommand();
        command.setBuy(true);
        command.setClOrdID("1234");
        command.setTraderID((short) 1);
        command.setOrderID(1);
        command.setSecurityID((short) 7);
        command.setContributorSeq(1);
        command.setPrice(com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(100);
        command.setContributorID((short) 1) ;

        orderCmdHandler.onMatchOrder(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Assert.assertEquals(MatchConstants.OrderRejectReason.InvalidSecurity, getFirstMessage(MatchOrderRejectEvent.class).getReason());
    }

    @Test
    public void testOrderBadPrice2()
    {
        MatchOrderCommand command = messages.getMatchOrderCommand();
        command.setBuy(true);
        command.setClOrdID("1234");
        command.setTraderID((short) 1);
        command.setOrderID(1);
        command.setSecurityID((short) 1);
        command.setContributorSeq(1);
        command.setPrice(com.core.util.PriceUtils.toLong(100.33333, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(100);
        command.setContributorID((short) 1) ;

        orderCmdHandler.onMatchOrder(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Assert.assertEquals(MatchConstants.OrderRejectReason.InvalidPrice, getFirstMessage(MatchOrderRejectEvent.class).getReason());
    }

    @Test
    public void testOrderBadPrice1()
    {
        MatchOrderCommand command = messages.getMatchOrderCommand();
        command.setBuy(true);
        command.setClOrdID("1234");
        command.setTraderID((short) 1);
        command.setOrderID(1);
        command.setSecurityID((short) 1);
        command.setContributorSeq(1);
        command.setPrice(com.core.util.PriceUtils.toLong(0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(100);
        command.setContributorID((short) 1) ;

        orderCmdHandler.onMatchOrder(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Assert.assertEquals(MatchConstants.OrderRejectReason.InvalidPrice, getFirstMessage(MatchOrderRejectEvent.class).getReason());
    }

    @Test
    public void testOrderBadQty()
    {
        MatchOrderCommand command = messages.getMatchOrderCommand();
        command.setBuy(true);
        command.setClOrdID("1234");
        command.setTraderID((short) 1);
        command.setOrderID(1);
        command.setSecurityID((short) 1);
        command.setContributorSeq(1);
        command.setPrice(com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(0);
        command.setContributorID((short) 1) ;

        orderCmdHandler.onMatchOrder(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Assert.assertEquals(MatchConstants.OrderRejectReason.InvalidQuantity, getFirstMessage(MatchOrderRejectEvent.class).getReason());
    }

    @Test
    public void testOrderDisabledSecurity()
    {
        MatchOrderCommand command = messages.getMatchOrderCommand();
        this.securities.setDisabled((short) 1);
        command.setBuy(true);
        command.setClOrdID("1234");
        command.setTraderID((short) 1);
        command.setOrderID(1);
        command.setSecurityID((short) 1);
        command.setContributorSeq(1);
        command.setPrice(com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(100);
        command.setContributorID((short) 1) ;

        orderCmdHandler.onMatchOrder(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Assert.assertEquals(MatchConstants.OrderRejectReason.SecurityDisabled, getFirstMessage(MatchOrderRejectEvent.class).getReason());
    }

    @Test
    public void testOrderMarketClosed()
    {
        MatchOrderCommand command = messages.getMatchOrderCommand();
        this.marketService.forceClose();
        command.setBuy(true);
        command.setClOrdID("1234");
        command.setTraderID((short) 1);
        command.setOrderID(1);
        command.setSecurityID((short) 1);
        command.setContributorSeq(1);
        command.setPrice(com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(100);
        command.setContributorID((short) 1) ;

        orderCmdHandler.onMatchOrder(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Assert.assertEquals(MatchConstants.OrderRejectReason.TradingSystemClosed, getFirstMessage(MatchOrderRejectEvent.class).getReason());
    }

    @Test
    public void testOrderDisabledTrader()
    {
        MatchOrderCommand command = messages.getMatchOrderCommand();
        this.traders.setDisabled((short) 1);
        command.setBuy(true);
        command.setClOrdID("1234");
        command.setTraderID((short) 1);
        command.setOrderID(1);
        command.setSecurityID((short) 1);
        command.setContributorSeq(1);
        command.setPrice(com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(100);
        command.setContributorID((short) 1) ;

        orderCmdHandler.onMatchOrder(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Assert.assertEquals(MatchConstants.OrderRejectReason.TraderDisabled, getFirstMessage(MatchOrderRejectEvent.class).getReason());
    }

    @Test
    public void testOrderDisabledAccount()
    {
        MatchOrderCommand command = messages.getMatchOrderCommand();
        this.accounts.setDisabled((short) 1);
        command.setBuy(true);
        command.setClOrdID("1234");
        command.setTraderID((short) 1);
        command.setOrderID(1);
        command.setSecurityID((short) 1);
        command.setContributorSeq(1);
        command.setPrice(com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(100);
        command.setContributorID((short) 1) ;

        orderCmdHandler.onMatchOrder(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Assert.assertEquals(MatchConstants.OrderRejectReason.AccountDisabled, getFirstMessage(MatchOrderRejectEvent.class).getReason());
    }

    @Test
    public void testOrderBadTrader()
    {
        MatchOrderCommand command = messages.getMatchOrderCommand();
        command.setBuy(true);
        command.setClOrdID("1234");
        command.setTraderID((short) 3);
        command.setOrderID(1);
        command.setSecurityID((short) 1);
        command.setContributorSeq(1);
        command.setPrice(com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(100);
        command.setContributorID((short) 1) ;

        orderCmdHandler.onMatchOrder(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Assert.assertEquals(MatchConstants.OrderRejectReason.InvalidTrader, getFirstMessage(MatchOrderRejectEvent.class).getReason());
    }

    @Test
    public void testOrderBadSequenceNumber()
    {
        MatchOrderCommand command = messages.getMatchOrderCommand();
        command.setContributorID((short) 1) ;
        command.setContributorSeq((short) 2);
        command.setBuy(true);
        command.setClOrdID("1234");
        command.setOrderID(1);
        command.setPrice(com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(100);

        orderCmdHandler.onMatchOrder(command.toEvent());

        Assert.assertEquals(0, contribs.getSeqNum(1));
    }

    @Test
    public void testTooManyOrders()
    {
        Mockito.when(books.numOrders()).thenReturn((long) MatchConstants.MAX_LIVE_ORDERS);

        MatchOrderCommand command = messages.getMatchOrderCommand();
        command.setContributorID((short) 1) ;
        command.setContributorSeq((short) 1);
        command.setBuy(true);
        command.setClOrdID("1234");
        command.setOrderID(1);
        command.setPrice(com.core.util.PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(100);

        orderCmdHandler.onMatchOrder(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Assert.assertEquals(MatchConstants.OrderRejectReason.TradingSystemNotAcceptingOrders, getFirstMessage(MatchOrderRejectEvent.class).getReason());
    }

    @Test
    public void testClientOrderReject()
    {
        Mockito.when(books.numOrders()).thenReturn((long) MatchConstants.MAX_LIVE_ORDERS);

        MatchClientOrderRejectCommand command = messages.getMatchClientOrderRejectCommand();
        command.setContributorID((short) 1) ;
        command.setContributorSeq((short) 1);
        command.setBuy(true);
        command.setClOrdID("1234");
        command.setTrader("BAR");
        command.setText("WTF");
        command.setSecurity("SEC");

        orderCmdHandler.onMatchClientOrderReject(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Assert.assertTrue(sender.isFlushed());
    }

    @Test
    public void testClientOrderRejectBadSeqNum()
    {
        Mockito.when(books.numOrders()).thenReturn((long) MatchConstants.MAX_LIVE_ORDERS);

        MatchClientOrderRejectCommand command = messages.getMatchClientOrderRejectCommand();
        command.setContributorID((short) 1) ;
        command.setContributorSeq((short) 2);
        command.setBuy(true);
        command.setClOrdID("1234");
        command.setTrader("BAR");
        command.setText("WTF");
        command.setSecurity("SEC");

        orderCmdHandler.onMatchClientOrderReject(command.toEvent());

        Assert.assertEquals(0, contribs.getSeqNum(1));
        Assert.assertFalse(sender.isFlushed());
    }

    @Test
    public void onMatchOrder_spreadOrderWithNegativePrice_OrderSuccessfullyGenerated()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));
        //security id =3 is a spread
        MatchOrderCommand command = buildOrderCommand(true,"abc1", 1,3,1,-0.01,4000,1);

        SequencerOrder order = new SequencerOrder();
        Mockito.when(books.buildOrder(command.toEvent())).thenReturn(order);

        orderCmdHandler.onMatchOrder(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Mockito.verify(books).buildOrder(command.toEvent());
        Mockito.verify(books).addOrder(order);
        Assert.assertTrue(sender.isFlushed());
    }

    @Test
    public void onMatchOrder_spreadOrderWithZeroPrice_OrderSuccessfullyGenerated()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));

        MatchOrderCommand command = buildOrderCommand(true,"abc1", 1,3,1,0,4000,1);

        SequencerOrder order = new SequencerOrder();
        Mockito.when(books.buildOrder(command.toEvent())).thenReturn(order);

        orderCmdHandler.onMatchOrder(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Mockito.verify(books).buildOrder(command.toEvent());
        Mockito.verify(books).addOrder(order);
        Assert.assertTrue(sender.isFlushed());
    }

    @Test
    public void onMatchOrder_spreadOrderWithPositivePrice_OrderSuccessfullyGenerated()
    {
        Assert.assertEquals(0, contribs.getSeqNum(1));

        MatchOrderCommand command = buildOrderCommand(true,"abc1", 1,3,1,0.01,4000,1);

        SequencerOrder order = new SequencerOrder();
        Mockito.when(books.buildOrder(command.toEvent())).thenReturn(order);

        orderCmdHandler.onMatchOrder(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        Mockito.verify(books).buildOrder(command.toEvent());
        Mockito.verify(books).addOrder(order);
        Assert.assertTrue(sender.isFlushed());
    }
    private MatchOrderCommand buildOrderCommand(boolean buy, String clientOrderID, int traderID,
                                                int securityID, int contributorSeq, double price, int qty, int orderID){
        MatchOrderCommand command = messages.getMatchOrderCommand();
        command.setBuy(buy);
        command.setClOrdID(clientOrderID);
        command.setTraderID((short)traderID);
        command.setOrderID(orderID);
        command.setSecurityID((short)securityID);
        command.setContributorSeq(contributorSeq);
        command.setPrice(com.core.util.PriceUtils.toLong(price, MatchConstants.IMPLIED_DECIMALS));
        command.setQty(qty);
        command.setContributorID((short) 1) ;
        return command;
    }

}
