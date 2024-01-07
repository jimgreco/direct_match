package com.core.match.fix;

import com.core.fix.StubFIXStore;
import com.core.fix.connector.StubFIXConnector;
import com.core.fix.msgs.FixDispatcher;
import com.core.fix.msgs.FixMsgTypes;
import com.core.fix.msgs.FixTags;
import com.core.fix.tags.StubFIXTagCreator;
import com.core.match.StubMatchCommandSender;
import com.core.match.msgs.MatchInboundEvent;
import com.core.match.msgs.MatchOutboundEvent;
import com.core.match.msgs.MatchTestDispatcher;
import com.core.match.msgs.MatchTestMessages;
import com.core.nio.SimulatedSelectorService;
import com.core.util.log.SystemOutLog;
import com.core.util.time.SimulatedTimeSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jgreco on 1/6/15.
 */
public class FixStateMachineTest {
    private FixDispatcher dispatcher;
    private SimulatedSelectorService selectorService;
    private MatchTestDispatcher testDispatcher;
    private StubMatchCommandSender commandSender;
    private StubFIXTagCreator tagCreator;
    private StubFIXStore store;
    private StubFIXConnector connector;
    private MatchTestMessages testMessages;
    private FixStateMachine stateMachine;
    private SystemOutLog log;

    @Before
    public void before() {
        SimulatedTimeSource timeSource = new SimulatedTimeSource();
        String name = "FIX01";
        log = new SystemOutLog("CORE03-1", name, timeSource);

        dispatcher = new FixDispatcher(log);
        selectorService = new SimulatedSelectorService(log, timeSource);
        testDispatcher = new MatchTestDispatcher();
        commandSender = new StubMatchCommandSender(name, (short) 1, testDispatcher);
        tagCreator = new StubFIXTagCreator(4, "CLIENT", "SERVER");
        store = new StubFIXStore();
        connector = new StubFIXConnector();
        testMessages = new MatchTestMessages();

        stateMachine = new FixStateMachine(
                log,
                dispatcher,
                selectorService,
                commandSender,
                tagCreator,
                store,
                connector,
                testMessages,
                false,
                false,
                4,
                "CLIENT",
                "SERVER");
        stateMachine.onContributorDefined((short)1, null);
        commandSender.setActive();
    }

    @Test
    public void testDisconnectOnInvalidMsgSeqNum() {
        tagCreator.setTag(FixTags.MsgSeqNum, -1);
        Assert.assertEquals(FixStateMachine.FixMessageResult.Disconnect, stateMachine.onFixMessage());
    }

    @Test
    public void testLogon() {
        doLogin();
    }

    private void doLogin() {
        tagCreator.newMsg(FixMsgTypes.Logon);
        Assert.assertEquals(FixStateMachine.FixMessageResult.Complete, stateMachine.onFixMessage());

        MatchInboundEvent logonInbound = commandSender.getMessage(MatchInboundEvent.class);
        MatchOutboundEvent logonOutbound = commandSender.getMessage(MatchOutboundEvent.class);

        Assert.assertEquals(FixMsgTypes.Logon, logonInbound.getFixMsgType());
        Assert.assertEquals(FixMsgTypes.Logon, logonOutbound.getFixMsgType());

        stateMachine.onMatchInbound(logonInbound);
        stateMachine.onMatchOutbound(logonOutbound);
    }

    @Test
    public void testResendOnHigherSeqNumForLogonMessage() {
        tagCreator.setTag(FixTags.BeginString, "FIX.4.4");
        tagCreator.setTag(FixTags.SenderCompID, "CLIENT");
        tagCreator.setTag(FixTags.TargetCompID, "SERVER");
        tagCreator.setTag(FixTags.MsgSeqNum, 4);
        tagCreator.setTag(FixTags.MsgType, FixMsgTypes.Logon);
        Assert.assertEquals(FixStateMachine.FixMessageResult.Complete, stateMachine.onFixMessage());

        MatchOutboundEvent logonOutbound = commandSender.getMessage(MatchOutboundEvent.class);
        MatchOutboundEvent resendOutbound = commandSender.getMessage(MatchOutboundEvent.class);

        Assert.assertEquals(FixMsgTypes.Logon, logonOutbound.getFixMsgType());
        Assert.assertEquals(FixMsgTypes.ResendRequest, resendOutbound.getFixMsgType());
    }

    @Test
    public void testOnFixMessage_HigherSeqNum_sendResendRequest() {
        doLogin();

        tagCreator.setTag(FixTags.MsgSeqNum, 20);
        tagCreator.setTag(FixTags.MsgType, FixMsgTypes.NewOrderSingle);
        Assert.assertEquals(FixStateMachine.FixMessageResult.NextMessage, stateMachine.onFixMessage());

        MatchOutboundEvent outboundMessage = commandSender.getMessage(MatchOutboundEvent.class);

        Assert.assertEquals(FixMsgTypes.ResendRequest, outboundMessage.getFixMsgType());
    }


    @Test
    public void testOnFixMessage_RejectFollowedByCorrectMessage_sendResendRequest() {
        doLogin();

        tagCreator.setTag(FixTags.MsgSeqNum, 2);
        tagCreator.setTag(FixTags.MsgType, 'T');
        Assert.assertEquals(2, stateMachine.getNextInboundSeqNo());
        Assert.assertEquals(2, stateMachine.getNextOutboundSeqNo());

        //TODO: verify we send out 1 inbound heart beat and 1 outbound reject of the current seqnum=2
        Assert.assertEquals(FixStateMachine.FixMessageResult.Complete, stateMachine.onFixMessage());

        Assert.assertEquals(2,stateMachine.getNextInboundSeqNo());
        Assert.assertEquals(2,stateMachine.getNextOutboundSeqNo());

        MatchInboundEvent hearbeatInbound = commandSender.getMessage(MatchInboundEvent.class);
        MatchOutboundEvent rejectOutbound = commandSender.getMessage(MatchOutboundEvent.class);


        stateMachine.onMatchInbound(hearbeatInbound);
        stateMachine.onMatchOutbound(rejectOutbound);
        //verify current sequence number here

        Assert.assertEquals(3, stateMachine.getNextInboundSeqNo());
        Assert.assertEquals(3, stateMachine.getNextOutboundSeqNo());

        //We send an outbound reject with seq num=2, what will
        tagCreator.setTag(FixTags.MsgSeqNum, 2);
        tagCreator.setTag(FixTags.MsgType, FixMsgTypes.MarketDataRequest);

    }

    @Test
    public void testOnFixMessage_ResendRequestHasHigherSeqNum_resendMessagesFromStore() {
        doLogin();

        tagCreator.setTag(FixTags.MsgSeqNum, 20);
        tagCreator.setTag(FixTags.MsgType, FixMsgTypes.ResendRequest);
        tagCreator.setTag(FixTags.BeginSeqNo, 10);
        tagCreator.setTag(FixTags.EndSeqNo, 20);

        //TODO: HL check on this logic
        Assert.assertEquals(FixStateMachine.FixMessageResult.NextMessage, stateMachine.onFixMessage());
    }

    @Test
    public void testDisconnectWrongSenderCompID() {
        tagCreator.setTag(FixTags.BeginString, "FIX.4.4");
        tagCreator.setTag(FixTags.SenderCompID, "FOO");
        tagCreator.setTag(FixTags.TargetCompID, "SERVER");
        tagCreator.setTag(FixTags.MsgSeqNum, 1);
        tagCreator.setTag(FixTags.MsgType, FixMsgTypes.Logon);
        Assert.assertEquals(FixStateMachine.FixMessageResult.Disconnect, stateMachine.onFixMessage());
    }

    @Test
    public void testDisconnectWrongTargetCompID() {
        tagCreator.setTag(FixTags.BeginString, "FIX.4.4");
        tagCreator.setTag(FixTags.SenderCompID, "CLIENT");
        tagCreator.setTag(FixTags.TargetCompID, "FOO");
        tagCreator.setTag(FixTags.MsgSeqNum, 1);
        tagCreator.setTag(FixTags.MsgType, FixMsgTypes.Logon);
        Assert.assertEquals(FixStateMachine.FixMessageResult.Disconnect, stateMachine.onFixMessage());
    }

    @Test
    public void testDisconnectWrongVersion() {
        tagCreator.setTag(FixTags.BeginString, "FIX.4.2");
        tagCreator.setTag(FixTags.SenderCompID, "CLIENT");
        tagCreator.setTag(FixTags.TargetCompID, "SERVER");
        tagCreator.setTag(FixTags.MsgSeqNum, 1);
        tagCreator.setTag(FixTags.MsgType, FixMsgTypes.Logon);
        Assert.assertEquals(FixStateMachine.FixMessageResult.Disconnect, stateMachine.onFixMessage());
    }

    @Test
    public void testDisconnectOnAllowGapFillReset() {
        doLogin();

        stateMachine.setNextInboundSeqNum(10);

        tagCreator.newMsg(FixMsgTypes.SequenceReset);
        tagCreator.setTag(FixTags.NewSeqNo, 5);
        Assert.assertEquals(FixStateMachine.FixMessageResult.Disconnect, stateMachine.onFixMessage());
    }

    @Test
    public void testDisconnectOnGapFillToLowerSequenceNumber() {
        doLogin();

        stateMachine.setNextInboundSeqNum(10);

        tagCreator.newMsg(FixMsgTypes.SequenceReset);
        tagCreator.setTag(FixTags.GapFillFlag, 'Y');
        tagCreator.setTag(FixTags.NewSeqNo, 5);
        Assert.assertEquals(FixStateMachine.FixMessageResult.Disconnect, stateMachine.onFixMessage());
    }

    @Test
    public void testDisconnectOnSeqNoTooLow() {
        doLogin();

        stateMachine.setNextInboundSeqNum(10);

        tagCreator.newMsg(FixMsgTypes.NewOrderSingle);
        Assert.assertEquals(FixStateMachine.FixMessageResult.Disconnect, stateMachine.onFixMessage());
    }

    @Test
    public void testGapFill() {
        doLogin();

        tagCreator.newMsg(FixMsgTypes.SequenceReset);
        tagCreator.setTag(FixTags.GapFillFlag, 'Y');
        tagCreator.setTag(FixTags.NewSeqNo, 7);
        Assert.assertEquals(FixStateMachine.FixMessageResult.Complete, stateMachine.onFixMessage());
        Assert.assertEquals(2, stateMachine.getNextInboundSeqNo());

        MatchInboundEvent gapFill = commandSender.getMessage(MatchInboundEvent.class);
        Assert.assertEquals(FixMsgTypes.SequenceReset, gapFill.getFixMsgType());
        Assert.assertEquals(7, gapFill.getEndSeqNo());

        stateMachine.onMatchInbound(gapFill);
        Assert.assertEquals(7, stateMachine.getNextInboundSeqNo());
    }

    @Test
    public void testRejectSuccessfullySendHeartBeatAndRejectMessage() {
        doLogin();

        tagCreator.newMsg(FixMsgTypes.SequenceReset);
        tagCreator.setTag(FixTags.GapFillFlag, 'Y');
        tagCreator.setTag(FixTags.NewSeqNo, 7);
        char reason= '3';
        stateMachine.reject("", FixTags.BusinessRejectReason, reason);

        Assert.assertEquals(2, stateMachine.getNextInboundSeqNo());
        MatchInboundEvent hearbeatEvent = commandSender.getMessage(MatchInboundEvent.class);
        MatchOutboundEvent rejectEvent = commandSender.getMessage(MatchOutboundEvent.class);

        Assert.assertEquals(FixMsgTypes.Reject, rejectEvent.getFixMsgType());
        Assert.assertEquals(FixMsgTypes.Heartbeat, hearbeatEvent.getFixMsgType());
    }

    @Test
    public void testResetSequenceNums(){
        doLogin();
        tagCreator.newMsg(FixMsgTypes.NewOrderSingle);
        stateMachine.resetSequenceNums();
        Assert.assertEquals(1, stateMachine.getNextInboundSeqNo());
    }




    @Test
    public void testNormalMessage() {
        doLogin();

        tagCreator.newMsg(FixMsgTypes.NewOrderSingle);
        Assert.assertEquals(FixStateMachine.FixMessageResult.Complete, stateMachine.onFixMessage());
    }

    @Test
    public void testResendSendMultipleTimes() {
        stateMachine.setNextOutboundSeqNo(10, false);
        store.setResendResult(5);

        doLogin();

        tagCreator.newMsg(FixMsgTypes.ResendRequest);
        tagCreator.setTag(FixTags.BeginSeqNo, 1);
        Assert.assertEquals(FixStateMachine.FixMessageResult.Complete, stateMachine.onFixMessage());

        MatchInboundEvent resend = commandSender.getMessage(MatchInboundEvent.class);
        stateMachine.onMatchInbound(resend);

        Assert.assertEquals(1, store.getLastBeginSeqNo());
        Assert.assertEquals(0, store.getLastEndSeqNo());

        store.setResendResult(8);
        selectorService.runFor(80);
        Assert.assertEquals(1, store.getLastBeginSeqNo());
        Assert.assertEquals(0, store.getLastEndSeqNo());

        selectorService.runFor(21);
        Assert.assertFalse(commandSender.hasMessages());
        Assert.assertEquals(5, store.getLastBeginSeqNo());
        Assert.assertEquals(0, store.getLastEndSeqNo());

        store.setResendResult(10);
        selectorService.runFor(80);
        Assert.assertEquals(5, store.getLastBeginSeqNo());
        Assert.assertEquals(0, store.getLastEndSeqNo());

        selectorService.runFor(21);
        Assert.assertFalse(commandSender.hasMessages());
        Assert.assertEquals(8, store.getLastBeginSeqNo());
        Assert.assertEquals(0, store.getLastEndSeqNo());
    }

    @Test
    public void testNoHeartbeatWhileSendingMessage() {
        doLogin();

        commandSender.setCanWrite(false);
        stateMachine.sendHeartbeat();

        Assert.assertFalse(commandSender.hasMessages());

        commandSender.setCanWrite(true);
        stateMachine.sendHeartbeat();

        MatchOutboundEvent message = commandSender.getMessage(MatchOutboundEvent.class);
        Assert.assertEquals(FixMsgTypes.Heartbeat, message.getFixMsgType());
    }

    @Test
    public void testNoTestRequestWhileSendingMessage() {
        doLogin();

        commandSender.setCanWrite(false);
        stateMachine.sendTestRequest();

        Assert.assertFalse(commandSender.hasMessages());

        commandSender.setCanWrite(true);
        stateMachine.sendHeartbeat();

        MatchOutboundEvent message = commandSender.getMessage(MatchOutboundEvent.class);
        Assert.assertEquals(FixMsgTypes.Heartbeat, message.getFixMsgType());
    }
}
