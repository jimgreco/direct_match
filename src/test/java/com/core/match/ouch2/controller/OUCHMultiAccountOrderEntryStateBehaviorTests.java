package com.core.match.ouch2.controller;

import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.StubHeartbeatApp;
import com.core.fix.msgs.FixConstants;
import com.core.match.GenericAppTest;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchContributorCommand;
import com.core.match.ouch.OUCHOrder;
import com.core.match.ouch.msgs.OUCHConstants;
import com.core.match.ouch2.msgs.OUCH2OrderCommand;
import com.core.match.ouch2.msgs.OUCH2TestMessages;
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
public class OUCHMultiAccountOrderEntryStateBehaviorTests extends GenericAppTest<OUCHOrder>
{
    private OUCH2TestMessages ouchMessages;
    protected StubOUCH2Adapter ouchAdapter;
    private OUCHMultiAccountOrderEntry target;

    public OUCHMultiAccountOrderEntryStateBehaviorTests()
    {
        super(OUCHOrder.class);
    }

    @Override
    @Before
    public void before() throws IOException
    {

        ouchMessages = new OUCH2TestMessages();
        ouchAdapter = new StubOUCH2Adapter(this.ouchMessages, this.dispatcher);
        ouchAdapter.setConnected(true);
        Mockito.when(this.ouchFactory.getOUCH2Adaptor("Name",this.log,this.fileFactory,this.tcpSockets,this.timers,123,"UN","PW")).thenReturn(ouchAdapter);

        this.target = new OUCHMultiAccountOrderEntry(this.log, this.tcpSockets,this.fileFactory,this.timers, this.sender, this.dispatcher, this.msgs,
                this.accounts, this.traders, this.securities, this.systemEventService, this.contributors, connector,ouchFactory,"Name",123,"UN","PW","","JIM,JPMS");
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
        assertFalse(target.isActive());


        target.setActive();
        assertTrue(ouchAdapter.isConnected());

    }

    @Test
    public void testCannotSendOrdersToCoreWhenPassive() {
        target.setPassive();
        assertTrue(ouchAdapter.isConnected());

        OUCH2OrderCommand ouchOrderCommand1 = getOuchOrderCommand(1, 100, 15534,"HL");
        target.onOUCH2Order(ouchOrderCommand1.toEvent());
        Object event = ouchAdapter.getQueue().poll();
        Assert.assertNull(event);

    }

    @Test
    public void testCANSendOrdersToCoreWhenPassiveCHANGEDtoActive() {
        target.setPassive();
        assertTrue(ouchAdapter.isConnected());

        OUCH2OrderCommand ouchOrderCommand1 = getOuchOrderCommand(1, 100, 15534,"HL");
        target.onOUCH2Order(ouchOrderCommand1.toEvent());
        Object event = ouchAdapter.getQueue().poll();
        Assert.assertNull(event);

        target.setActive();
        OUCH2OrderCommand ouchOrderCommand2 = getOuchOrderCommand(2, 100, 15534,"HL");
        target.onOUCH2Order(ouchOrderCommand2.toEvent());

        Object event2 = ouchAdapter.getQueue().poll();
        Assert.assertNotNull(event2);
    }

    @Test
    public void testWhenPassive_cannotSendToCore_adaptorSendsDebug_msgDropCounterIncrement() {
        long clOrId=15534;
        target.setPassive();
        assertTrue(ouchAdapter.isConnected());

        OUCH2OrderCommand ouchOrderCommand1 = getOuchOrderCommand(1, 100, clOrId,"HL");
        target.onOUCH2Order(ouchOrderCommand1.toEvent());
        Object event = ouchAdapter.getQueue().poll();
        Assert.assertNull(event);

        assertTrue(ouchAdapter.listOfDebugMsgSent.contains(clOrId));
    }

    @Test
    public void testWhenPassiveToActiveToPassive_ResetsMessageDroppedLimitCounter() {
        target.setPassive();
        assertTrue(ouchAdapter.isConnected());

        OUCH2OrderCommand ouchOrderCommand1 = getOuchOrderCommand(1, 100, 15534, "HL");
        target.onOUCH2Order(ouchOrderCommand1.toEvent());
        Object event = ouchAdapter.getQueue().poll();
        Assert.assertNull(event);

        OUCH2OrderCommand ouchOrderCommand3 = getOuchOrderCommand(1, 100, 15534, "HL");
        target.onOUCH2Order(ouchOrderCommand3.toEvent());
        Object event3 = ouchAdapter.getQueue().poll();
        Assert.assertNull(event3);


        target.setActive();
        OUCH2OrderCommand ouchOrderCommand2 = getOuchOrderCommand(2, 100, 15534, "HL");
        target.onOUCH2Order(ouchOrderCommand2.toEvent());

        Object event2 = ouchAdapter.getQueue().poll();
        Assert.assertNotNull(event2);

        target.setPassive();
        assertTrue(ouchAdapter.isConnected());

    }



    @Test
    public void testPassive_sendMaxOrderDropsClosesConnection_ClientCanReconnectAndCounterResets() throws IOException {
        target.setPassive();

        //Set passive so cannot send upstream but we can enable ouch to accept incoming messages
        assertFalse(target.isActive());
        assertTrue(ouchAdapter.isConnected());
        ouchAdapter.open();

        long clOrId6=6;


        OUCH2OrderCommand ouchOrderCommand6 = getOuchOrderCommand(1, 100, clOrId6, "HL");
        target.onOUCH2Order(ouchOrderCommand6.toEvent());

        Object obj = ouchAdapter.getQueue().poll();
        Assert.assertNull(obj);

        //This verify that the counter is reset
        assertTrue(ouchAdapter.isConnected());
        assertTrue(ouchAdapter.listOfDebugMsgSent.contains(clOrId6));

        assertFalse(target.isActive());


    }



    public OUCH2OrderCommand getOuchOrderCommand(double qty, long price, long clOrdId, String trader)
    {
        return getOuchOrderCommand(qty, price, clOrdId, trader, "2Y");
    }

    private OUCH2OrderCommand getOuchOrderCommand(double qty, long price, long clOrdId, String trader, String sec)
    {
        OUCH2OrderCommand OUCH2OrderCommand = ouchMessages.getOUCH2OrderCommand();
        OUCH2OrderCommand.setClOrdID(clOrdId);
        OUCH2OrderCommand.setPrice(price * MatchPriceUtils.getPriceMultiplier());
        OUCH2OrderCommand.setQty((int) (qty * MatchConstants.QTY_MULTIPLIER));
        OUCH2OrderCommand.setSecurity(sec);
        OUCH2OrderCommand.setSide(OUCHConstants.Side.Buy);
        OUCH2OrderCommand.setTimeInForce(FixConstants.TimeInForce.Day);
        OUCH2OrderCommand.setTrader(trader);

        return OUCH2OrderCommand;
    }


}
