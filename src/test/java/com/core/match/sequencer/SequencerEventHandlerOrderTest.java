package com.core.match.sequencer;

import com.core.match.msgs.MatchCancelCommand;
import com.core.match.msgs.MatchFillCommand;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchOrderCommand;
import com.core.match.msgs.MatchReplaceCommand;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by jgreco on 7/26/15.
 */
public class SequencerEventHandlerOrderTest extends HandlerTestBase {
    @Test
    public void onMatchFill_any_verifyFillsInStore() {
        SequencerOrder order = new SequencerOrder();
        Mockito.when(books.getOrder(1)).thenReturn(order);

        MatchFillCommand cmd = messages.getMatchFillCommand(sender.startMessage());
        cmd.setContributorID((short) 1);
        cmd.setContributorSeq(3);
        cmd.setOrderID(1);
        cmd.setPrice(100);
        cmd.setQty(10);
        cmd.setLastFill(true);
        cmd.setMatchID(5);
        MatchFillEvent msg = cmd.toEvent();

        eventHandler.onMatchFill(msg);

        Mockito.verify(backupQueue).verifyFillEvent( msg);
    }

    @Test
    public void onMatchFill_multipleFillsRx_WillInvokeCorrectFieldsInRightOrder() {
        SequencerOrder order1 = new SequencerOrder();
        Mockito.when(books.getOrder(1)).thenReturn(order1);

        SequencerOrder order2 = new SequencerOrder();
        Mockito.when(books.getOrder(2)).thenReturn(order2);

        MatchFillCommand cmd1 = messages.getMatchFillCommand(sender.startMessage());
        cmd1.setContributorID((short) 1);
        cmd1.setContributorSeq(3);
        cmd1.setOrderID(1);
        cmd1.setPrice(100);
        cmd1.setQty(10);
        cmd1.setPassive(true);
        cmd1.setLastFill(false);
        cmd1.setMatchID(5);
        MatchFillEvent msg1 = cmd1.toEvent();
        eventHandler.onMatchFill(msg1);

        MatchFillCommand cmd2 = messages.getMatchFillCommand(sender.startMessage());
        cmd2.setContributorID((short) 1);
        cmd2.setContributorSeq(4);
        cmd2.setOrderID(2);
        cmd2.setPrice(100);
        cmd2.setQty(10);
        cmd2.setPassive(false);
        cmd2.setLastFill(true);
        cmd2.setMatchID(5);
        MatchFillEvent msg2 = cmd2.toEvent();
        eventHandler.onMatchFill(msg2);

        Mockito.verify(backupQueue).verifyFillEvent( msg1);
        Mockito.verify(backupQueue).verifyFillEvent( msg2);
    }

    @Test
    public void onMatchFill_any_InvokedCorrectFunctions() {
        Mockito.when(books.getOrder(1)).thenReturn(null);

        MatchFillCommand cmd = messages.getMatchFillCommand(sender.startMessage());
        cmd.setContributorID((short) 1);
        cmd.setContributorSeq(4);
        cmd.setOrderID(1);
        cmd.setPrice(100);
        cmd.setQty(10);

        //Act
        eventHandler.onMatchFill(cmd.toEvent());
        Mockito.verify(backupQueue).verifyFillEvent( cmd.toEvent());
    }

    @Test
    public void onMatchReplace_any_correctfunctionsInvoked() {
        SequencerOrder order = new SequencerOrder();
        Mockito.when(books.getOrder(1)).thenReturn(order);

        MatchReplaceCommand msg = messages.getMatchReplaceCommand(sender.startMessage());
        msg.setContributorID((short)2);
        msg.setContributorSeq(2);
        msg.setOrderID(1);
        msg.setQty(1);
        msg.setPrice(100);
        msg.setExternalOrderID(3);

        Mockito.when(books.buildReplace(order, msg.toEvent())).thenReturn(true);

        //Act
        eventHandler.onMatchReplace(msg.toEvent());
        Mockito.verify(orderEventHandler).onMatchReplace( msg.toEvent());
        Mockito.verify(backupQueue).verifyReplaceEvent( msg.toEvent());
    }

    @Test
    public void onMatchCancel_any_correctFunctionsInvoked() {
        SequencerOrder order = new SequencerOrder();
        Mockito.when(books.getOrder(1)).thenReturn(order);

        MatchCancelCommand msg = messages.getMatchCancelCommand(sender.startMessage());
        msg.setContributorID((short) 2);
        msg.setContributorSeq(3);
        msg.setOrderID(1);

        eventHandler.onMatchCancel(msg.toEvent());
        Mockito.verify(orderEventHandler).onMatchCancel( msg.toEvent());
        Mockito.verify(backupQueue).verifyCancelEvent( msg.toEvent());
    }

    @Test
    public void onMatchOrder_any_correctFunctionsInvoked() {
        MatchOrderCommand msg = messages.getMatchOrderCommand(sender.startMessage());
        msg.setContributorID((short) 2);
        msg.setContributorSeq(2);
        msg.setBuy(true);
        msg.setQty(10);
        msg.setPrice(100);
        msg.setSecurityID((short) 1);

        SequencerOrder order = new SequencerOrder();
        Mockito.when(books.buildOrder(msg.toEvent())).thenReturn(order);

        //Act
        eventHandler.onMatchOrder(msg.toEvent());
        Mockito.verify(orderEventHandler).onMatchOrder( msg.toEvent());
        Mockito.verify(backupQueue).verifyOrderEvent( msg.toEvent());
    }
}
