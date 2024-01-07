package com.core.match.ouch;

import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.StubHeartbeatApp;
import com.core.fix.msgs.FixConstants;
import com.core.match.GenericAppTest;
import com.core.match.StubMatchCommandSender;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchContributorCommand;
import com.core.match.ouch.controller.OUCHOrderEntry;
import com.core.match.ouch.msgs.OUCHConstants;
import com.core.match.ouch.msgs.OUCHOrderCommand;
import com.core.match.ouch.msgs.OUCHTestMessages;
import com.core.match.util.MatchPriceUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by hli on 10/15/15.
 */
public class OUCHOrderEntryStateBehaviorTests extends GenericAppTest<OUCHOrder>
{
    private TestOUCHAdapter ouchAdapter;
    private OUCHOrderEntry target;
    private OUCHTestMessages ouchMessages;
    private StubMatchCommandSender stubMatchCommandSender;

    public OUCHOrderEntryStateBehaviorTests()
    {
        super(OUCHOrder.class);
    }

    @Override
    @Before
    public void before() throws IOException
    {

        ouchMessages = new OUCHTestMessages();
        ouchAdapter = new TestOUCHAdapter(this.ouchMessages, this.dispatcher);
        ouchAdapter.setConnected(true);
        Mockito.when(this.ouchFactory.getOUCHAdaptor("Name",this.log,this.fileFactory,this.tcpSockets,this.timers,123,"UN","PW")).thenReturn(ouchAdapter);
        stubMatchCommandSender = new StubMatchCommandSender("TEST", (short)1, this.dispatcher);

        this.target = new OUCHOrderEntry(this.log, this.tcpSockets,this.fileFactory,this.timers, this.stubMatchCommandSender, this.dispatcher, this.msgs,
                this.accounts, this.traders, this.securities, this.systemEventService, this.contributors,this.referenceBBOBookService, connector,ouchFactory,"Name",123,"UN","PW","JIM");
        HeartbeatFieldRegister register = new StubHeartbeatApp("TESTAPP");
        MatchContributorCommand command = msgs.getMatchContributorCommand();
        command.setCancelOnDisconnect(true);
        command.setContributorID((short) 1);
        command.setContributorSeq(1);
        command.setSourceContributorID((short) 1);
        dispatcher.dispatch(command);
        target.onAddHeartbeatFields(register);
        sendBond("2Y");
        this.openMarket();
        this.sendAccount("JIM", 10000, "NULL", false, 4.0);
        this.sendTrader("JIM", "JIM", 10000, 10000, 100000, 10000, 10000, 10000);

    }

    @Test
    public void testOuchAdaptorIsConnectedOnActive() {
        target.setActive();
        assertTrue(ouchAdapter.isConnected());

    }

    @Test
    public void testDoNotTryToReconnectAdaptorWhenTogglingFromPassive() {
        target.setPassive();
        assertTrue(ouchAdapter.isConnected());
        assertTrue(target.isOuchAdaptorOpen());

        target.setActive();
        assertTrue(ouchAdapter.isConnected());

    }

    @Test
    public void testCannotSendOrdersToCoreWhenPassive() {
        target.setPassive();
        assertTrue(ouchAdapter.isConnected());

        OUCHOrderCommand ouchOrderCommand1 = getOuchOrderCommand(1, 100, 15534,"HL");
        target.onOUCHOrder(ouchOrderCommand1.toEvent());
        Object event = ouchAdapter.getQueue().poll();
        Assert.assertNull(event);

    }

    @Test
    public void testCANSendOrdersToCoreWhenPassiveCHANGEDtoActive() {
        target.setPassive();
        assertTrue(ouchAdapter.isConnected());

        OUCHOrderCommand ouchOrderCommand1 = getOuchOrderCommand(1, 100, 15534,"HL");
        target.onOUCHOrder(ouchOrderCommand1.toEvent());
        Object event = ouchAdapter.getQueue().poll();
        Assert.assertNull(event);

        target.setActive();
        OUCHOrderCommand ouchOrderCommand2 = getOuchOrderCommand(2, 100, 15534,"HL");
        target.onOUCHOrder(ouchOrderCommand2.toEvent());

        Object event2 = ouchAdapter.getQueue().poll();
        Assert.assertNotNull(event2);
    }

    @Test
    public void testWhenPassive_cannotSendToCore_adaptorSendsDebug_msgDropCounterIncrement() {
        long clOrId=15534;
        target.setPassive();
        assertTrue(ouchAdapter.isConnected());

        OUCHOrderCommand ouchOrderCommand1 = getOuchOrderCommand(1, 100, clOrId,"HL");
        target.onOUCHOrder(ouchOrderCommand1.toEvent());
        Object event = ouchAdapter.getQueue().poll();
        Assert.assertNull(event);

        assertEquals(1, target.getNumberMsgDropped());
        assertTrue(ouchAdapter.listOfDebugMsgSent.contains(clOrId));
    }

    @Test
    public void testWhenPassiveToActiveToPassive_ResetsMessageDroppedLimitCounter() {
        target.setPassive();
        assertTrue(ouchAdapter.isConnected());

        OUCHOrderCommand ouchOrderCommand1 = getOuchOrderCommand(1, 100, 15534, "HL");
        target.onOUCHOrder(ouchOrderCommand1.toEvent());
        Object event = ouchAdapter.getQueue().poll();
        Assert.assertNull(event);

        OUCHOrderCommand ouchOrderCommand3 = getOuchOrderCommand(1, 100, 15534, "HL");
        target.onOUCHOrder(ouchOrderCommand3.toEvent());
        Object event3 = ouchAdapter.getQueue().poll();
        Assert.assertNull(event3);
        assertEquals(2, target.getNumberMsgDropped());


        target.setActive();
        OUCHOrderCommand ouchOrderCommand2 = getOuchOrderCommand(2, 100, 15534, "HL");
        target.onOUCHOrder(ouchOrderCommand2.toEvent());

        Object event2 = ouchAdapter.getQueue().poll();
        Assert.assertNotNull(event2);

        target.setPassive();
        assertTrue(ouchAdapter.isConnected());
        assertEquals(0, target.getNumberMsgDropped());

    }

    @Test
    public void testPassive_sendMaxOrderDropsClosesConnectionAndSendDebugMessages() {
        long clOrId1=1;
        long clOrId2=2;
        long clOrId3=3;
        long clOrId4=4;
        long clOrId5=5;


        OUCHOrderCommand ouchOrderCommand1 = getOuchOrderCommand(1, 100, clOrId1,"HL");
        target.onOUCHOrder(ouchOrderCommand1.toEvent());
        OUCHOrderCommand ouchOrderCommand2 = getOuchOrderCommand(2, 100, clOrId2,"HL");
        target.onOUCHOrder(ouchOrderCommand2.toEvent());
        OUCHOrderCommand ouchOrderCommand3 = getOuchOrderCommand(3, 100, clOrId3,"HL");
        target.onOUCHOrder(ouchOrderCommand3.toEvent());
        OUCHOrderCommand ouchOrderCommand4 = getOuchOrderCommand(4, 100, clOrId4, "HL");
        target.onOUCHOrder(ouchOrderCommand4.toEvent());
        OUCHOrderCommand ouchOrderCommand5 = getOuchOrderCommand(5, 100, clOrId5,"HL");
        target.onOUCHOrder(ouchOrderCommand5.toEvent());

        Object obj = ouchAdapter.getQueue().poll();
        Assert.assertNull(obj);

        assertFalse(ouchAdapter.isConnected());
        //only clients are closed. Server is still open, hence we dont try to re-establish connection
        assertTrue(target.isOuchAdaptorOpen());
        assertTrue(ouchAdapter.listOfDebugMsgSent.contains(clOrId1));
        assertTrue(ouchAdapter.listOfDebugMsgSent.contains(clOrId2));
        assertTrue(ouchAdapter.listOfDebugMsgSent.contains(clOrId3));
        assertTrue(ouchAdapter.listOfDebugMsgSent.contains(clOrId4));
        assertTrue(ouchAdapter.listOfDebugMsgSent.contains(clOrId5));

    }

    @Test
    public void testPassive_sendMaxOrderDropsClosesConnection_ClientCanReconnectAndCounterResets() throws IOException {
        testPassive_sendMaxOrderDropsClosesConnectionAndSendDebugMessages();
        assertTrue(target.isOuchAdaptorOpen());
        assertFalse(ouchAdapter.isConnected());
        ouchAdapter.open();

        long clOrId6=6;


        OUCHOrderCommand ouchOrderCommand6 = getOuchOrderCommand(1, 100, clOrId6, "HL");
        target.onOUCHOrder(ouchOrderCommand6.toEvent());

        Object obj = ouchAdapter.getQueue().poll();
        Assert.assertNull(obj);

        //This verify that the counter is reset
        assertTrue(ouchAdapter.isConnected());
        assertTrue(ouchAdapter.listOfDebugMsgSent.contains(clOrId6));

        assertTrue(target.isOuchAdaptorOpen());


    }



    private OUCHOrderCommand getOuchOrderCommand(double qty, long price, long clOrdId, String trader)
    {
        OUCHOrderCommand ouchOrderCommand = ouchMessages.getOUCHOrderCommand();
        ouchOrderCommand.setClOrdID(clOrdId);
        ouchOrderCommand.setPrice(price * MatchPriceUtils.getPriceMultiplier());
        ouchOrderCommand.setQty((int) (qty * MatchConstants.QTY_MULTIPLIER));
        ouchOrderCommand.setSecurity("2Y");
        ouchOrderCommand.setSide(OUCHConstants.Side.Buy);
        ouchOrderCommand.setTimeInForce(FixConstants.TimeInForce.Day);
        ouchOrderCommand.setTrader(trader);
        return ouchOrderCommand;
    }
}
