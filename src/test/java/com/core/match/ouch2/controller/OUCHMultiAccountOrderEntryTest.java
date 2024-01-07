package com.core.match.ouch2.controller;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.StubHeartbeatApp;
import com.core.fix.msgs.FixConstants;
import com.core.match.GenericAppTest;
import com.core.match.msgs.MatchCancelReplaceRejectCommand;
import com.core.match.msgs.MatchClientCancelReplaceRejectEvent;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchContributorCommand;
import com.core.match.msgs.MatchFillCommand;
import com.core.match.msgs.MatchOrderCommand;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchOrderRejectCommand;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.match.ouch.OUCHOrder;
import com.core.match.ouch.msgs.OUCHConstants;
import com.core.match.ouch2.factories.OUCHComponentFactory;
import com.core.match.ouch2.msgs.OUCH2AcceptedCommand;
import com.core.match.ouch2.msgs.OUCH2AcceptedEvent;
import com.core.match.ouch2.msgs.OUCH2CancelCommand;
import com.core.match.ouch2.msgs.OUCH2CancelRejectedCommand;
import com.core.match.ouch2.msgs.OUCH2CancelRejectedEvent;
import com.core.match.ouch2.msgs.OUCH2CanceledCommand;
import com.core.match.ouch2.msgs.OUCH2CanceledEvent;
import com.core.match.ouch2.msgs.OUCH2FillCommand;
import com.core.match.ouch2.msgs.OUCH2FillEvent;
import com.core.match.ouch2.msgs.OUCH2OrderCommand;
import com.core.match.ouch2.msgs.OUCH2RejectedCommand;
import com.core.match.ouch2.msgs.OUCH2RejectedEvent;
import com.core.match.ouch2.msgs.OUCH2ReplaceCommand;
import com.core.match.ouch2.msgs.OUCH2ReplacedCommand;
import com.core.match.ouch2.msgs.OUCH2ReplacedEvent;
import com.core.match.ouch2.msgs.OUCH2TestMessages;
import com.core.match.util.MatchPriceUtils;
import com.core.util.PriceUtils;

/**
 * Created by liuli on 3/31/2016.
 */
public class OUCHMultiAccountOrderEntryTest extends GenericAppTest<OUCHOrder> {

    protected OUCHMultiAccountOrderEntry orderEntry;
    private OUCH2TestMessages ouchMessages;
    protected StubOUCH2Adapter ouchAdapter;

    private OUCHComponentFactory mockAdaptorFactory;
    private final String testTrader="LLIU";
    private final String testTrader2="JIM";

    private final String testTraderNotAuthorizedToTradeViaGUI ="HLI";
    private final String testTraderWithDifferentAccount ="MPARKER";
    private OUCHConnectionController mockOUCHController;
    private OUCHOrdersRepository stubbedOuchRepo;
    private OUCHMultiAccountOrderEntry orderEntry2;
    private StubOUCH2Adapter ouchAdapter2;


    public OUCHMultiAccountOrderEntryTest() {
        super(OUCHOrder.class);
    }


    @Before
    @Override
    public void before() throws IOException {
        this.ouchMessages = new OUCH2TestMessages();

        this.ouchAdapter = new StubOUCH2Adapter(this.ouchMessages, this.dispatcher);
        this.ouchAdapter2 = new StubOUCH2Adapter(this.ouchMessages, this.dispatcher);

        mockAdaptorFactory= Mockito.mock(OUCHComponentFactory.class);
        mockOUCHController= Mockito.mock(OUCHConnectionController.class);
        stubbedOuchRepo = new OUCHOrdersRepository();
        this.ouchAdapter.setConnected(true);
        this.ouchAdapter2.setConnected(true);

        Mockito.when(mockAdaptorFactory.getOUCH2Adaptor("Name",this.log,this.fileFactory,this.tcpSockets,this.timers,123,"UN","PW")).thenReturn(ouchAdapter);
        Mockito.when(mockAdaptorFactory.getOUCHConnectionController(this.ouchAdapter,this.log,this.orderEntry)).thenReturn(mockOUCHController);
        Mockito.when(mockAdaptorFactory.getOUCHRepository()).thenReturn(stubbedOuchRepo);
        orderEntry = new OUCHMultiAccountOrderEntry(this.log, this.tcpSockets,this.fileFactory,this.timers, this.sender, this.dispatcher, this.msgs,
                this.accounts, this.traders, this.securities, this.systemEventService, this.contributors, connector,mockAdaptorFactory,"Name",123,"UN","PW",",OUCH01,OUCH2","DIRECTMATCH,JPMS");
        Mockito.when(mockAdaptorFactory.getOUCH2Adaptor("Name2",this.log,this.fileFactory,this.tcpSockets,this.timers,345,"UN","PW")).thenReturn(ouchAdapter2);

        orderEntry2 = new OUCHMultiAccountOrderEntry(this.log, this.tcpSockets,this.fileFactory,this.timers, this.sender, this.dispatcher, this.msgs,
                this.accounts, this.traders, this.securities, this.systemEventService, this.contributors, connector,mockAdaptorFactory,"Name2",345,"UN","PW","OUCH01,OUCH2","DIRECTMATCH,JPMS");
        HeartbeatFieldRegister register = new StubHeartbeatApp("TESTAPP");



        orderEntry.onAddHeartbeatFields(register);
        orderEntry2.onAddHeartbeatFields(register);

        MatchContributorCommand command = msgs.getMatchContributorCommand();
        command.setCancelOnDisconnect(true);
        command.setName("OUCH1");
        command.setContributorID((short) 1);
        command.setContributorSeq(1);
        command.setSourceContributorID((short) 1);

        this.dispatcher.dispatch(command);

        MatchContributorCommand command2 = msgs.getMatchContributorCommand();
        command2.setCancelOnDisconnect(true);
        command2.setName("OUCH2");
        command2.setContributorID((short) 2);
        command2.setContributorSeq(2);
        command2.setSourceContributorID((short) 2);

        this.dispatcher.dispatch(command2);

        this.sendBond("2Y");
        this.openMarket();
        this.sendAccount("DIRECTMATCH", 100000, "NULL", false, 3.5);
        this.sendAccount("GTS", 100000, "NULL", false, 3.5);
        this.sendAccount("JPMS", 100000, "NULL", false, 3.5);

        this.sendTrader(testTrader, "DIRECTMATCH", 2000, 3000, 5000, 7000, 10000, 10000);
        this.sendTrader(testTrader2, "DIRECTMATCH", 2000, 3000, 5000, 7000, 10000, 10000);

        this.sendTrader(testTraderNotAuthorizedToTradeViaGUI, "GTS", 2000, 3000, 5000, 7000, 10000, 10000);
        this.sendTrader(testTraderWithDifferentAccount, "JPMS", 2000, 3000, 5000, 7000, 10000, 10000);
        this.orderEntry.setActive();
        this.orderEntry2.setActive();



    }

    @Test
    public void onOUCH2Order_authorizedAccount_GetOrderAcceptOnBothOuch() {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1, testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1);

        OUCH2AcceptedEvent event2 = ((OUCH2AcceptedCommand) this.ouchAdapter2.getQueue().poll()).toEvent();
        Assert.assertEquals(event2.getClOrdID(), 1);

    }


    @Test
    public void onOUCH2Order_3InboundOrders_3CanceledMessagesOnDisconnect()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234, testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(),1234);

        OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1235, testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1235);

        OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1236, testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1236);

        this.ouchAdapter.setConnected(false);
        //listener gets callback to cancel live order
        this.orderEntry.cancelAllLiveOrdersOnDisconnect();
        OUCH2CanceledEvent canceled = ((OUCH2CanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234, canceled.getClOrdID());
        Assert.assertEquals('U', canceled.getReason());

        canceled = ((OUCH2CanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1236, canceled.getClOrdID());
        Assert.assertEquals('U', canceled.getReason());

        canceled = ((OUCH2CanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1235, canceled.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.UnknownClOrdID, canceled.getReason());
    }

    @Test
    public void testMultipleCancelsOnDisconnectReconnectMidwayThrough()
    {

        // 3 orders
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234, testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(),1234);

        OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1235, testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1235);

        OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1236, testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1236);

        this.sender.setDontDispatch(true);
        this.ouchAdapter.setConnected(false);
        this.orderEntry.cancelAllLiveOrdersOnDisconnect();
        this.sender.dequeue();
        OUCH2CanceledEvent canceled = ((OUCH2CanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234,  canceled.getClOrdID());
        Assert.assertEquals('U', canceled.getReason());

        // when this canceled comes back we'll send out the next one

        this.ouchAdapter.setConnected(true);

        this.sender.dequeue();
        canceled = ((OUCH2CanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1236,  canceled.getClOrdID());
        Assert.assertEquals('U', canceled.getReason());

        // no more cancels -- should also check to make sure no others were sent
        Assert.assertEquals(0, this.ouchAdapter.getQueue().size());
    }

    @Test
    public void onDisconnect_hasLiveOrders_OrdersCancelled()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234, testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(),1234);

        this.ouchAdapter.setConnected(false);
        this.orderEntry.cancelAllLiveOrdersOnDisconnect();
        OUCH2CanceledEvent canceled = ((OUCH2CanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234,  canceled.getClOrdID());
        Assert.assertEquals('U', canceled.getReason());
    }

    @Test
    public void onOUCH2Order_2TradersWith2DifferentAccount_BothOrdersAccepted()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234, testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(),1234);
        OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1235, testTraderWithDifferentAccount);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(),1235);
    }

    @Test
    public void onOUCH2Order_sameAccountDifferentTraderAndExccedAccountDV01_receiveRejects()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(100, 100, 1234, testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(),1234);
        OUCH2OrderCommand = getOUCH2OrderCommand(100, 100, 1235, testTrader2);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2RejectedEvent rejected = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1235,rejected.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.RiskViolation,rejected.getReason());

    }

    @Test
    public void onOUCH2Order_2OrdersWithDuplicatedClientOrderID_receiveRejects()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(),1234);
        OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2RejectedEvent reject = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234,event.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.DuplicateClOrdID, reject.getReason());
    }

    @Test
    public void onOUCH2Replace_clientIDNotFound_replaceRejected()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(),1234);
        OUCH2ReplaceCommand ouchReplace = getOuchReplace(1000, 100, 1111);
        this.orderEntry.onOUCH2Replace(ouchReplace.toEvent());
        OUCH2RejectedEvent reject = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1112, reject.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.UnknownClOrdID, reject.getReason());
    }

    @Test
    public void onOUCH2Replace_riskviolation_replaceRejected()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(),1234);
        OUCH2ReplaceCommand ouchReplace = getOuchReplace(1000, 100, 1234);
        this.orderEntry.onOUCH2Replace(ouchReplace.toEvent());
        OUCH2RejectedEvent reject = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1235, reject.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.RiskViolation, reject.getReason());
    }

    @Test
    public void onOUCH2Replace_priceInvalid_replaceRejected()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(),1234);
        OUCH2ReplaceCommand ouchReplace = getOuchReplace(1, 0, 1234);
        this.orderEntry.onOUCH2Replace(ouchReplace.toEvent());
        OUCH2RejectedEvent reject = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1235, reject.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.InvalidPrice, reject.getReason());
    }

    @Test
    public void onOUCH2Replace_qtyInvalid_replaceRejected()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(),1234);
        OUCH2ReplaceCommand ouchReplace = getOuchReplace(0, 100, 1234);
        this.orderEntry.onOUCH2Replace(ouchReplace.toEvent());
        OUCH2RejectedEvent reject = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1235, reject.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.InvalidQuantity, reject.getReason());
    }

    @Test
    public void onCancelReplaceReject_any_adaptorSendsOuchCancelRpl()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());

        MatchOrderEvent orderMsg = sender.getMessage(MatchOrderEvent.class);
        Assert.assertEquals(1, orderMsg.getQty());
        Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), orderMsg.getPrice());

        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(),1234);

        MatchCancelReplaceRejectCommand reject = this.msgs.getMatchCancelReplaceRejectCommand();
        reject.setIsReplace(false);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(1234L);
        buffer.flip();
        reject.setOrigClOrdID(buffer);
        buffer.clear();
        buffer.putLong(1234L);
        buffer.flip();
        reject.setClOrdID(buffer);
        OUCHOrder order=new OUCHOrder();
        order.setClOrdID(1234L);

        this.orderEntry.onCancelReplaceReject(order, reject.toEvent());

        OUCH2CancelRejectedEvent rejectedEvent = ((OUCH2CancelRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234,  rejectedEvent.getClOrdID());
    }

    @Test
    public void onCancelReplaceReject_any_adaptorSendsOUCHReject()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());

        MatchOrderEvent orderMsg = sender.getMessage(MatchOrderEvent.class);
        Assert.assertEquals(1, orderMsg.getQty());
        Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), orderMsg.getPrice());

        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1234);

        MatchCancelReplaceRejectCommand reject = this.msgs.getMatchCancelReplaceRejectCommand();
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(1234L);
        buffer.flip();
        reject.setOrigClOrdID(buffer);
        buffer.clear();
        buffer.putLong(1235L);
        buffer.flip();
        reject.setClOrdID(buffer);
        reject.setIsReplace(true);
        OUCHOrder order=new OUCHOrder();
        order.setClOrdID(1234L);

        this.orderEntry.onCancelReplaceReject(order, reject.toEvent());

        OUCH2RejectedEvent rejectedEvent = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1235,  rejectedEvent.getClOrdID());
    }

    @Test
    public void onOUCH2Replace_any_adaptorSendsOUCHReplace()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());

        MatchOrderEvent orderMsg = sender.getMessage(MatchOrderEvent.class);
        Assert.assertEquals(1, orderMsg.getQty());
        Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), orderMsg.getPrice());

        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1234);
        OUCH2ReplaceCommand ouchReplace = getOuchReplace(2, 100, 1234);

        this.orderEntry.onOUCH2Replace(ouchReplace.toEvent());

        MatchReplaceEvent replaceMsg = sender.getMessage(MatchReplaceEvent.class);
        Assert.assertEquals(2, replaceMsg.getQty());
        Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), replaceMsg.getPrice());

        OUCH2ReplacedEvent replaced = ((OUCH2ReplacedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1235,  replaced.getClOrdID());
        Assert.assertEquals(1234,  replaced.getOldClOrdId());

    }

    @Test
    public void testReplace_replaceExceedFatfingerLimit()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());

        MatchOrderEvent orderMsg = sender.getMessage(MatchOrderEvent.class);
        Assert.assertEquals(1, orderMsg.getQty());
        Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), orderMsg.getPrice());

        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1234);
        //Price is small enough that we only violate fat finger
        OUCH2ReplaceCommand ouchReplace = getOuchReplace(2001, 1, 1234);

        this.orderEntry.onOUCH2Replace(ouchReplace.toEvent());

        MatchClientCancelReplaceRejectEvent replaceRejMsg = sender.getMessage(MatchClientCancelReplaceRejectEvent.class);
        //Reject reason is risk violation
        Assert.assertEquals('Z', replaceRejMsg.getReason());
        OUCH2RejectedEvent reject = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1235, reject.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.RiskViolation, reject.getReason());


    }

    @Test
    public void onOUCH2Cancel_ClientOrderIDONCancelNotFound_cancelRejected()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(),1234);
        OUCH2CancelCommand OUCH2CancelCommand = getOuchCancel( 1 );

        this.orderEntry.onOUCH2Cancel(OUCH2CancelCommand.toEvent());
        OUCH2CancelRejectedEvent reject = ((OUCH2CancelRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1, reject.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.UnknownClOrdID, reject.getReason());
    }

    @Test
    public void onOUCH2Cancel_any_orderCanceled()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(),1234);
        OUCH2CancelCommand OUCH2CancelCommand = getOuchCancel( 1234 );

        this.orderEntry.onOUCH2Cancel(OUCH2CancelCommand.toEvent());
        OUCH2CanceledEvent canceled = ((OUCH2CanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234,  canceled.getClOrdID());
        Assert.assertEquals('U', canceled.getReason());
    }

    @Test
    public void onOUCH2Cancel_newOrderThenReplacedThenCancel_ReceiveAcksandUserRequestedCancel()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1234);

        OUCH2ReplaceCommand ouchReplace = getOuchReplace(2, 100, 1234);
        this.orderEntry.onOUCH2Replace(ouchReplace.toEvent());
        OUCH2ReplacedEvent replaced = ((OUCH2ReplacedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1235, replaced.getClOrdID());

        OUCH2CancelCommand OUCH2CancelCommand = getOuchCancel(1234);
        this.orderEntry.onOUCH2Cancel(OUCH2CancelCommand.toEvent());
        OUCH2CancelRejectedEvent reject = ((OUCH2CancelRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234, reject.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.UnknownClOrdID, reject.getReason());

        OUCH2CancelCommand OUCH2CancelCommand2 = getOuchCancel(1235);
        this.orderEntry.onOUCH2Cancel(OUCH2CancelCommand2.toEvent());
        OUCH2CanceledEvent canceled = ((OUCH2CanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1235, canceled.getClOrdID());
        Assert.assertEquals(OUCHConstants.CanceledReason.UserRequest, canceled.getReason());
    }

    @Test
    public void onOUCH2Replace_newOrderCancelTHenReplaceWithOriginalClOrdID_orderCanceledHenceReplaceRejected()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1234);

        OUCH2CancelCommand OUCH2CancelCommand = getOuchCancel(1234);
        this.orderEntry.onOUCH2Cancel(OUCH2CancelCommand.toEvent());
        OUCH2CanceledEvent canceled = ((OUCH2CanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234, canceled.getClOrdID());
        Assert.assertEquals(OUCHConstants.CanceledReason.UserRequest, canceled.getReason());

        OUCH2ReplaceCommand ouchReplace = getOuchReplace(2, 100, 1234);
        this.orderEntry.onOUCH2Replace(ouchReplace.toEvent());
        OUCH2RejectedEvent reject = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1235, reject.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.UnknownClOrdID, reject.getReason());
    }

    @Test
    public void onOUCH2Cancel_cancelAnORderTwice_secondCancelIsRejected()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1234);

        OUCH2CancelCommand OUCH2CancelCommand = getOuchCancel(1234);
        this.orderEntry.onOUCH2Cancel(OUCH2CancelCommand.toEvent());
        OUCH2CanceledEvent canceled = ((OUCH2CanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234, canceled.getClOrdID());
        Assert.assertEquals(OUCHConstants.CanceledReason.UserRequest, canceled.getReason());

        OUCH2CancelCommand OUCH2CancelCommand2 = getOuchCancel(1234);
        this.orderEntry.onOUCH2Cancel(OUCH2CancelCommand2.toEvent());
        OUCH2CancelRejectedEvent reject = ((OUCH2CancelRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234, reject.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.UnknownClOrdID, reject.getReason());
    }

    @Test
    public void onOUCH2Replace_replaceOnFullyFilledOrder_replaceRejected()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1234);

        MatchFillCommand command = this.msgs.getMatchFillCommand();
        command.setOrderID(1);
        command.setMatchID(1);
        command.setLastFill(true);
        command.setPrice(100 * MatchPriceUtils.getPriceMultiplier());
        command.setQty(1);
        this.dispatcher.dispatch(command);
        OUCH2FillEvent fill = ((OUCH2FillCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234, fill.getClOrdID());
        Assert.assertEquals(OUCHConstants.Messages.Fill, fill.getMsgType());


        OUCH2ReplaceCommand ouchReplace = getOuchReplace(2, 100, 1234);
        this.orderEntry.onOUCH2Replace(ouchReplace.toEvent());
        OUCH2RejectedEvent reject = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1235, reject.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.UnknownClOrdID, reject.getReason());
    }

    @Test
    public void testCancelFullyFilledOrder()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1234);

        MatchFillCommand command = this.msgs.getMatchFillCommand();
        command.setOrderID(1);
        command.setMatchID(1);
        command.setLastFill(true);
        command.setPrice(100 * MatchPriceUtils.getPriceMultiplier());
        command.setQty(1);
        this.dispatcher.dispatch(command);
        OUCH2FillEvent fill = ((OUCH2FillCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234, fill.getClOrdID());
        Assert.assertEquals(OUCHConstants.Messages.Fill, fill.getMsgType());


        OUCH2CancelCommand OUCH2CancelCommand = getOuchCancel(1234);
        this.orderEntry.onOUCH2Cancel(OUCH2CancelCommand.toEvent());
        OUCH2CancelRejectedEvent reject = ((OUCH2CancelRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234, reject.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.UnknownClOrdID, reject.getReason());
    }


    @Test
    public void onOUCH2Order_fillsWithDisplayID_receiveCorrectOuchFillMessages() {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(2, 100, 1234, testTrader);
        this.sender.setDontDispatch(true);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        MatchOrderCommand cmd = (MatchOrderCommand) this.sender.pollDisconnectQueue();
        this.sender.setDontDispatch(false);
        this.sender.send(cmd);
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1234);

        MatchFillCommand command = this.msgs.getMatchFillCommand();
        command.setOrderID(2);
        command.setMatchID(1);
        command.setPassive(true);
        command.setLastFill(false);
        command.setPrice(100 * MatchPriceUtils.getPriceMultiplier());
        command.setQty(1);
        this.dispatcher.dispatch(command);

        OUCH2FillEvent fill = ((OUCH2FillCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234, fill.getClOrdID());
        Assert.assertEquals('E', fill.getMsgType());
        Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), fill.getExecutionPrice());
        Assert.assertEquals(1 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQty());
    }

    @Test
    public void onOUCH2Order_multipleFills_receiveCorrectOuchFillMessages()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(2, 100, 1234,testTrader);
        this.sender.setDontDispatch(true);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        MatchOrderCommand cmd = (MatchOrderCommand) this.sender.pollDisconnectQueue();
        this.sender.setDontDispatch(false);
        this.sender.send(cmd);
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1234);

        MatchFillCommand command = this.msgs.getMatchFillCommand();
        command.setOrderID(2);
        command.setMatchID(1);
        command.setPassive(true);
        command.setLastFill(false);
        command.setPrice(100 * MatchPriceUtils.getPriceMultiplier());
        command.setQty(1);
        this.dispatcher.dispatch(command);

        OUCH2FillEvent fill = ((OUCH2FillCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234, fill.getClOrdID());
        Assert.assertEquals('E', fill.getMsgType());
        Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), fill.getExecutionPrice());
        Assert.assertEquals(1 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQty());

        // because this order is marked passive we should immediately see a trade confirmation message
   
        command = this.msgs.getMatchFillCommand();
        command.setOrderID(2);
        command.setMatchID(2);
        command.setPassive(false);
        command.setLastFill(true);
        command.setPrice(95 * MatchPriceUtils.getPriceMultiplier());
        command.setQty(1);
        this.dispatcher.dispatch(command);
        fill = ((OUCH2FillCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234, fill.getClOrdID());
        Assert.assertEquals('E', fill.getMsgType());
        Assert.assertEquals(95 * MatchPriceUtils.getPriceMultiplier(), fill.getExecutionPrice());
        Assert.assertEquals(1 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQty());

   
        OUCH2OrderCommand = getOUCH2OrderCommand(3, 100, 4321,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 4321);
        command = this.msgs.getMatchFillCommand();
        command.setOrderID(3);
        command.setMatchID(3);
        command.setLastFill(false);
        command.setPrice(100 * MatchPriceUtils.getPriceMultiplier());
        command.setQty(2);
        this.dispatcher.dispatch(command);
        fill = ((OUCH2FillCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(4321, fill.getClOrdID());
        Assert.assertEquals('E', fill.getMsgType());
        Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), fill.getExecutionPrice());
        Assert.assertEquals(2 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQty());

        command = this.msgs.getMatchFillCommand();
        command.setOrderID(3);
        command.setMatchID(2);
        command.setLastFill(true);
        command.setPrice(95 * MatchPriceUtils.getPriceMultiplier());
        command.setQty(1);
        this.dispatcher.dispatch(command);
        fill = ((OUCH2FillCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(4321, fill.getClOrdID());
        Assert.assertEquals('E', fill.getMsgType());
        Assert.assertEquals(95 * MatchPriceUtils.getPriceMultiplier(), fill.getExecutionPrice());
        Assert.assertEquals(1 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQty());

   
    }

    @Test
    public void onOUCH2Order_authorizedAccount_GetOrderAccept() {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1, testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1);
    }

    @Test
    public void onOUCH2Order_unauthorizedAccount_GetReject() {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1, "HLI");
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2RejectedEvent event = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1);
        Assert.assertEquals(event.getReason(), OUCHConstants.RejectReason.InvalidTrader);

    }


    @Test
    public void onOUCH2Replace_sendNewOrderThenFullyFilledThenReplace_RejectReplaceBecauseOrderIsFullyFilled()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1234);

        MatchFillCommand command = this.msgs.getMatchFillCommand();
        command.setOrderID(1);
        command.setMatchID(1);
        command.setLastFill(true);
        command.setPrice(100 * MatchPriceUtils.getPriceMultiplier());
        command.setQty(1);
        this.dispatcher.dispatch(command);
        OUCH2FillEvent fill = ((OUCH2FillCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234, fill.getClOrdID());
        Assert.assertEquals(OUCHConstants.Messages.Fill, fill.getMsgType());

        OUCH2ReplaceCommand ouchReplace = getOuchReplace(2, 100, 1234);
        this.orderEntry.onOUCH2Replace(ouchReplace.toEvent());
        OUCH2RejectedEvent reject = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1235, reject.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.UnknownClOrdID, reject.getReason());
    }

    @Test
    public void onOUCH2Cancel_sendNewOrderThenFullyFilledThenCancel_RejectCancelBecauseOrderIsFullyFilled()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1234);

        MatchFillCommand command = this.msgs.getMatchFillCommand();
        command.setOrderID(1);
        command.setMatchID(1);
        command.setLastFill(true);
        command.setPrice(100 * MatchPriceUtils.getPriceMultiplier());
        command.setQty(1);
        this.dispatcher.dispatch(command);
        OUCH2FillEvent fill = ((OUCH2FillCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234, fill.getClOrdID());
        Assert.assertEquals(OUCHConstants.Messages.Fill, fill.getMsgType());


        OUCH2CancelCommand OUCH2CancelCommand = getOuchCancel(1234);
        this.orderEntry.onOUCH2Cancel(OUCH2CancelCommand.toEvent());
        OUCH2CancelRejectedEvent reject = ((OUCH2CancelRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234, reject.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.UnknownClOrdID, reject.getReason());
    }


    @Test
    // ignore this for now -- test will have to change for this to work
    public void onOUCH2Replace_replaceOrderWithSameLeavesQty_rejectReplaceSinceQtyNotChanged()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(2, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(event.getClOrdID(), 1234);
        MatchFillCommand command = this.msgs.getMatchFillCommand();
        command.setOrderID(1);
        command.setLastFill(true);
        command.setMatchID(1);
        command.setPrice(100 * MatchPriceUtils.getPriceMultiplier());
        command.setQty(1);
        this.dispatcher.dispatch(command);

        OUCH2FillEvent fillEvent = ((OUCH2FillCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1234, fillEvent.getClOrdID());
        Assert.assertEquals('E', fillEvent.getMsgType());
        Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), fillEvent.getExecutionPrice());
        Assert.assertEquals(1 * MatchConstants.QTY_MULTIPLIER, fillEvent.getExecutionQty());

        OUCH2ReplaceCommand OUCH2ReplaceCommand = getOuchReplace(1, 100, 1234);
        this.orderEntry.onOUCH2Replace(OUCH2ReplaceCommand.toEvent());
        OUCH2RejectedEvent reject = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
        Assert.assertEquals(1235, reject.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.InvalidQuantity, reject.getReason());
    }

    @Test
    public void onOUCH2Order_validClientORderIdAndValidTrader_getOrderAccept()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
        Assert.assertEquals(event.getClOrdID(),1234);
    }



    @Test
    public void onOrderReject_anyRejectFromSeq_SendsOuchReject()
    {
        MatchOrderRejectCommand command = this.msgs.getMatchOrderRejectCommand();
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(1234L);
        buffer.flip();
        command.setClOrdID(buffer);
        this.orderEntry.onOrderReject(command.toEvent());

        OUCH2RejectedEvent event = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
        Assert.assertEquals(1234,event.getClOrdID());
    }

    @Test
    public void onOUCH2Order_marketClose_OrderRejected()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader);
        this.closeMarket();

        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2RejectedEvent event = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
        Assert.assertEquals(1234,event.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.TradingSystemClosed, event.getReason());
    }

    @Test
    public void onOUCH2Order_invalidSecurity_orderRejected()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader, "4T");

        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2RejectedEvent event = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
        Assert.assertEquals(1234,event.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.InvalidSecurity, event.getReason());
    }

    @Test
    public void onOUCH2Order_sideInvalid_rejected()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1, 100, 1234,testTrader, "2Y");
        OUCH2OrderCommand.setSide('x');

        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2RejectedEvent event = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
        Assert.assertEquals(1234,event.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.InvalidSide, event.getReason());
    }


    @Test
    public void onOUCH2Order_invalidQty_orderRejected()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(1.5, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2RejectedEvent event = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
        Assert.assertEquals(1234,event.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.InvalidQuantity, event.getReason());
    }

    @Test
    public void onOUCH2Order_zeroQty_orderRejecred()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(0, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2RejectedEvent event = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
        Assert.assertEquals(1234,event.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.InvalidQuantity, event.getReason());
    }

    @Test
    public void onOUCH2Order_wrongTickSize_orderRejected()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(100, PriceUtils.toLong(100.333333, MatchConstants.IMPLIED_DECIMALS), 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2RejectedEvent event = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
        Assert.assertEquals(1234,event.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.InvalidPrice, event.getReason());
    }

    @Test
    public void onOUCH2Order_priceInvalid_orderRejected()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(100, 0, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2RejectedEvent event = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
        Assert.assertEquals(1234,event.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.InvalidPrice, event.getReason());
    }

    @Test
    public void onOUCH2Order_orderQtyExceedRiskLimit_orderRejected()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(200, 100, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2RejectedEvent event = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
        Assert.assertEquals(1234,event.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.RiskViolation, event.getReason());
    }

    @Test
    public void onOUCH2Order_exceedFatFinger_orderRejected()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(2001, 1, 1234,testTrader);
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2RejectedEvent event = ((OUCH2RejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
        Assert.assertEquals(1234,event.getClOrdID());
        Assert.assertEquals(OUCHConstants.RejectReason.RiskViolation, event.getReason());
    }

    @Test
    public void onOUCH2Order_displayQtyZero_orderDisplayQtyIsQty()
    {
        OUCH2OrderCommand OUCH2OrderCommand = getOUCH2OrderCommand(3, 100, 1234, testTrader, "2Y");
        this.orderEntry.onOUCH2Order(OUCH2OrderCommand.toEvent());
        OUCH2AcceptedEvent event = ((OUCH2AcceptedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
        Assert.assertEquals(1234,event.getClOrdID());
    }

    public OUCH2OrderCommand getOUCH2OrderCommand(double qty, long price, long clOrdId, String trader)
    {
        return getOUCH2OrderCommand(qty, price, clOrdId, trader, "2Y");
    }

    private OUCH2OrderCommand getOUCH2OrderCommand(double qty, long price, long clOrdId, String trader, String sec)
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

    private OUCH2CancelCommand getOuchCancel(long i)
    {
        OUCH2CancelCommand command = this.ouchMessages.getOUCH2CancelCommand();
        command.setClOrdID(i);
        return command;
    }

    private OUCH2ReplaceCommand getOuchReplace(double qty, long price, long clOrdId) {
        OUCH2ReplaceCommand OUCH2OrderCommand = this.ouchMessages.getOUCH2ReplaceCommand();
        OUCH2OrderCommand.setClOrdID(clOrdId);
        OUCH2OrderCommand.setNewClOrdID(clOrdId + 1);
        OUCH2OrderCommand.setNewPrice(price * MatchPriceUtils.getPriceMultiplier());
        OUCH2OrderCommand.setNewQty((int) (qty * MatchConstants.QTY_MULTIPLIER));
        return OUCH2OrderCommand;    }
}
