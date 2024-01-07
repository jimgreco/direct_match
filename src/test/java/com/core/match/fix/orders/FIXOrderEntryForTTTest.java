package com.core.match.fix.orders;

import static org.mockito.Matchers.anyChar;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.verification.Times;

import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatVirtualMachine;
import com.core.app.heartbeats.HeartbeatVirtualMachineImpl;
import com.core.fix.FixParser;
import com.core.fix.FixWriter;
import com.core.fix.connector.FixServerTcpConnector;
import com.core.fix.msgs.FixConstants;
import com.core.fix.msgs.FixDispatcher;
import com.core.fix.msgs.FixMsgTypes;
import com.core.fix.msgs.FixStubTags;
import com.core.fix.msgs.FixTags;
import com.core.fix.store.FixStore;
import com.core.match.GenericAppTest;
import com.core.match.fix.FixOrder;
import com.core.match.msgs.MatchAccountCommand;
import com.core.match.msgs.MatchClientCancelReplaceRejectEvent;
import com.core.match.msgs.MatchClientOrderRejectEvent;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchContributorCommand;
import com.core.match.msgs.MatchInboundEvent;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchOutboundEvent;
import com.core.match.msgs.MatchSecurityCommand;
import com.core.match.msgs.MatchTraderCommand;
import com.core.match.util.MatchPriceUtils;
import com.core.util.time.TimerServiceImpl;

/**
 * User: jgreco
 */
public class FIXOrderEntryForTTTest extends GenericAppTest<FixOrder> {
	private FixOrderEntryForTT app;
	private FixStore store;
	private FixWriter writer;
	private final FixStubTags tags = new FixStubTags();

	public FIXOrderEntryForTTTest()
	{
		super(FixOrder.class);
	}

	@Before
	public void setup() {
		FixServerTcpConnector fixConnector = mock(FixServerTcpConnector.class);
		FixParser parser = mock(FixParser.class);
		writer = mock(FixWriter.class);
		store = mock(FixStore.class);
		FixDispatcher fixDispatcher = new FixDispatcher(null);
		tags.init(parser);

		when(store.createMessage(anyChar())).thenReturn(writer);

		MatchTraderCommand traderCommand = msgs.getMatchTraderCommand();
		traderCommand.setTraderID(( short ) 1 );
		traderCommand.setName("BAR");
		traderCommand.setFatFinger2YLimit(10000);
		traderCommand.setFatFinger3YLimit(10000);
		traderCommand.setFatFinger5YLimit(10000);
		traderCommand.setFatFinger7YLimit(10000);
		traderCommand.setFatFinger10YLimit(10000);
		traderCommand.setFatFinger30YLimit(10000);

		traderCommand.setAccountID(( short ) 1 ) ;

 
		app = new FixOrderEntryForTT(
                log,
				dispatcher,
				new TimerServiceImpl(log, this.timeSource),
				this.traders,
				this.accounts,
				this.securities,
				this.systemEventService,
				this.contributors,
				this.sender,
				this.connector,
				msgs,
				fixConnector,
				parser,
				writer,
				store,
				fixDispatcher,
				"SENDER",
				"TARGET",
				"FOO",
				FIXQtyMode.RoundLot,
				2);
		this.app.onConnect();
		MatchContributorCommand cmd = msgs.getMatchContributorCommand();
		cmd.setCancelOnDisconnect(true);
		cmd.setContributorID(( short ) 1);
		cmd.setContributorSeq(1);
		cmd.setSourceContributorID((short) 1);
		this.contributors.onMatchContributor(cmd.toEvent());
        HeartbeatVirtualMachine register = new HeartbeatVirtualMachineImpl("CORE03-1");
        HeartbeatFieldRegister fix01 = register.addApp("Fix01", app);
        app.onAddHeartbeatFields(fix01);

        dispatcher.subscribe(accounts);
		dispatcher.subscribe(securities);

		MatchAccountCommand command = msgs.getMatchAccountCommand();
		command.setAccountID((short) 1);
		command.setName(ByteBuffer.wrap("FOO".getBytes()));
        command.setNetDV01Limit(1000000);
		dispatcher.dispatch(command);
		dispatcher.dispatch(traderCommand);

	}
	
	@Test
	public void testRejectOnMarketNotOpen() {
		setInitialReqFields();
		this.app.onClose(0);
		
		app.onFixNewOrderSingle();

		MatchClientOrderRejectEvent reject = sender
				.getMessage(MatchClientOrderRejectEvent.class);

		String error = "Market Closed";
		Assert.assertEquals(error, reject.getTextAsString());

		verify(store).createMessage(FixMsgTypes.ExecutionReport);
		verify(writer).writeChar(tags.Side, FixConstants.Side.Buy);
		verify(writer).writeString(tags.Symbol, "10Y");
		verify(writer).writeString(tags.SecurityID, "912828L32");
		verify(writer).writeString(tags.Text, str(error));
		verify(writer).writeChar(tags.ExecType, FixConstants.ExecType.Rejected);
		verify(writer).writeChar(tags.OrdStatus,
				FixConstants.OrdStatus.Rejected);
	}

	@Test
	public void testRejectOnMissingFields() {
		setInitialReqFields();
		tags.ClOrdID.setValue(null);

		app.onFixNewOrderSingle();

		MatchClientOrderRejectEvent reject = sender
				.getMessage(MatchClientOrderRejectEvent.class);

		String error = "Req Tag Missing: ClOrdID<" + FixTags.ClOrdID + ">";
		Assert.assertEquals(error, reject.getTextAsString());

		verify(store).createMessage(FixMsgTypes.ExecutionReport);
		verify(writer).writeString(tags.ClOrdID, "NONE");
		verify(writer).writeChar(tags.Side, FixConstants.Side.Buy);
		verify(writer).writeString(tags.Symbol, "10Y");
		verify(writer).writeString(tags.SecurityID, "912828L32");
		verify(writer).writeString(tags.Text, str(error));
		verify(writer).writeChar(tags.ExecType, FixConstants.ExecType.Rejected);
		verify(writer).writeChar(tags.OrdStatus,
				FixConstants.OrdStatus.Rejected);
	}

	@Test
	public void testRejectOnEmptyClOrdIdFields() {
		setInitialReqFields();
		tags.ClOrdID.setValue("");

		app.onFixNewOrderSingle();

		MatchClientOrderRejectEvent reject = sender
				.getMessage(MatchClientOrderRejectEvent.class);

		String error = "Req Tag Missing: ClOrdID<" + FixTags.ClOrdID + ">";
		Assert.assertEquals(error, reject.getTextAsString());

		verify(store).createMessage(FixMsgTypes.ExecutionReport);
		verify(writer).writeString(tags.ClOrdID, "NONE");
		verify(writer).writeChar(tags.Side, FixConstants.Side.Buy);
		verify(writer).writeString(tags.Symbol, "10Y");
		verify(writer).writeString(tags.SecurityID, "912828L32");
		verify(writer).writeString(tags.Text, str(error));
		verify(writer).writeChar(tags.ExecType, FixConstants.ExecType.Rejected);
		verify(writer).writeChar(tags.OrdStatus,
				FixConstants.OrdStatus.Rejected);
	}

	@Test
	public void testRejectOnInvalidSide() {
		setInitialReqFields();
		tags.Side.setValue('3');

		app.onFixNewOrderSingle();

		MatchClientOrderRejectEvent reject = sender
				.getMessage(MatchClientOrderRejectEvent.class);
		Assert.assertEquals("Invalid Side<54>: 3", reject.getTextAsString());

		verify(store).createMessage(FixMsgTypes.ExecutionReport);
		verify(writer).writeString(tags.ClOrdID, str("101"));
		verify(writer).writeChar(tags.Side, FixConstants.Side.Sell);
		verify(writer).writeChar(tags.ExecType, FixConstants.ExecType.Rejected);
		verify(writer).writeChar(tags.OrdStatus, FixConstants.OrdStatus.Rejected);
	}

	@Test
	public void testNewSingleOrder_invalidOrderType_OrderRejected() {
		setInitialReqFields();
		tags.OrdType.setValue('1');

		app.onFixNewOrderSingle();

		MatchClientOrderRejectEvent reject = sender
				.getMessage(MatchClientOrderRejectEvent.class);
		Assert.assertEquals("Expect Limit. Invalid OrdType<40>: 1", reject.getTextAsString());

		verify(store).createMessage(FixMsgTypes.ExecutionReport);
		verify(writer).writeString(tags.ClOrdID, str("101"));
		verify(writer).writeChar(tags.AvgPx, '0');
		verify(writer).writeChar(tags.ExecType, FixConstants.ExecType.Rejected);
		verify(writer).writeChar(tags.OrdStatus, FixConstants.OrdStatus.Rejected);
	}


	@Test
	public void testRejectOnExceedingFatFingerQuantity() {
		setInitialReqFields();
		tags.OrderQty.setValue(20000);
		tags.Price.setValue("1");

		app.onFixNewOrderSingle();

		MatchClientOrderRejectEvent reject = sender
				.getMessage(MatchClientOrderRejectEvent.class);
		Assert.assertEquals("Violation of Fat Finger risk check: FOO", reject.getTextAsString());

		verify(store).createMessage(FixMsgTypes.ExecutionReport);
		verify(writer).writeString(tags.ClOrdID, str("101"));
		verify(writer).writeChar(tags.Side, FixConstants.Side.Buy);
		verify(writer).writeChar(tags.ExecType, FixConstants.ExecType.Rejected);
		verify(writer).writeChar(tags.OrdStatus, FixConstants.OrdStatus.Rejected);
	}

	@Test
	public void testRejectOnZeroQty() {
		setInitialReqFields();
		tags.OrderQty.setValue(0);

		app.onFixNewOrderSingle();

		MatchClientOrderRejectEvent reject = sender
				.getMessage(MatchClientOrderRejectEvent.class);
		String error = "Invalid OrderQty<38>: 0";
		Assert.assertEquals(error, reject.getTextAsString());

		verify(store).createMessage(FixMsgTypes.ExecutionReport);
		// verify(writer).writeString(tags.OrderID, str("NONE"));
		verify(writer).writeString(tags.ClOrdID, str("101"));
		verify(writer).writeChar(tags.Side, FixConstants.Side.Buy);
		verify(writer).writeString(tags.Text, str(error));
		verify(writer).writeChar(tags.ExecType, FixConstants.ExecType.Rejected);
		verify(writer).writeChar(tags.OrdStatus,
				FixConstants.OrdStatus.Rejected);
	}
	
	@Test
	public void testMultipleCancelOnDisconnect() {
		setInitialReqFields();
		
		app.onFixNewOrderSingle();
		
		MatchOrderEvent order = sender.getMessage(MatchOrderEvent.class);
		Assert.assertEquals("101", order.getClOrdIDAsString());
		Assert.assertEquals((short) 1, order.getSecurityID());
		Assert.assertEquals(10, order.getQty());
		Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), order.getPrice());
		Assert.assertEquals(Boolean.TRUE, Boolean.valueOf(order.getBuy()));
		verify(writer, new Times(1)).writeString(tags.ClOrdID, str("101"));
		
		setInitialReqFields("102");
		
		app.onFixNewOrderSingle();
		
		order = sender.getMessage(MatchOrderEvent.class);
		Assert.assertEquals("102", order.getClOrdIDAsString());
		Assert.assertEquals((short) 1, order.getSecurityID());
		Assert.assertEquals(10, order.getQty());
		Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), order.getPrice());
		Assert.assertEquals(Boolean.TRUE, Boolean.valueOf(order.getBuy()));
		verify(writer, new Times(1)).writeString(tags.ClOrdID, str("102"));
		
		this.sender.setDontDispatch(true);
		// this will send out the cancel
		this.app.onDisconnect();
		
		this.sender.dequeue();
		verify(writer, new Times(2)).writeString(tags.ClOrdID, str("101"));
		verify(writer, new Times(1)).writeString(tags.OrigClOrdID, str("101"));
		verify(writer).writeChar(tags.OrdStatus, FixConstants.OrdStatus.Canceled);
		verify(writer).writeChar(tags.ExecType, FixConstants.ExecType.Canceled);
		
		this.sender.dequeue();
		verify(writer, new Times(2)).writeString(tags.ClOrdID, str("102"));
		verify(writer, new Times(1)).writeString(tags.OrigClOrdID, str("102"));
		verify(writer, new Times(2)).writeChar(tags.OrdStatus, FixConstants.OrdStatus.Canceled);
		verify(writer, new Times(2)).writeChar(tags.ExecType, FixConstants.ExecType.Canceled);
	}
	
	@Test
	public void testMultipleCancelOnDisconnectReconnectHalfwayThrough() {
		setInitialReqFields();
		
		app.onFixNewOrderSingle();
		
		MatchOrderEvent order = sender.getMessage(MatchOrderEvent.class);
		Assert.assertEquals("101", order.getClOrdIDAsString());
		Assert.assertEquals((short) 1, order.getSecurityID());
		Assert.assertEquals(10, order.getQty());
		Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), order.getPrice());
		Assert.assertEquals(Boolean.TRUE, Boolean.valueOf(order.getBuy()));
		verify(writer, new Times(1)).writeString(tags.ClOrdID, str("101"));
		
		setInitialReqFields("102");
		
		app.onFixNewOrderSingle();
		
		order = sender.getMessage(MatchOrderEvent.class);
		Assert.assertEquals("102", order.getClOrdIDAsString());
		Assert.assertEquals((short) 1, order.getSecurityID());
		Assert.assertEquals(10, order.getQty());
		Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), order.getPrice());
		Assert.assertEquals(Boolean.TRUE, Boolean.valueOf(order.getBuy()));
		verify(writer, new Times(1)).writeString(tags.ClOrdID, str("102"));
		
		this.sender.setDontDispatch(true);
		// this will send out the cancel, before the cxld acknowledgement comes back from sequencer, we get a reconnect
		this.app.onDisconnect();
		this.app.onConnect();
		
		//cxld on the first guy comes back
		this.sender.dequeue();
		verify(writer, new Times(2)).writeString(tags.ClOrdID, str("101"));
		verify(writer, new Times(1)).writeString(tags.OrigClOrdID, str("101"));
		verify(writer).writeChar(tags.OrdStatus, FixConstants.OrdStatus.Canceled);
		verify(writer).writeChar(tags.ExecType, FixConstants.ExecType.Canceled);
		
		// should be nothing when we dequeue here, since we didn't try to cxl again.
		Assert.assertEquals(0, this.sender.queueSize());
		// 2nd guy isn't cxld out, as desired
	}
	
	@Test
	public void testCancelOnDisconnect() {
		setInitialReqFields();
		
		app.onFixNewOrderSingle();
		
		MatchOrderEvent order = sender.getMessage(MatchOrderEvent.class);
		Assert.assertEquals("101", order.getClOrdIDAsString());
		Assert.assertEquals((short) 1, order.getSecurityID());
		Assert.assertEquals(10, order.getQty());
		Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), order.getPrice());
		Assert.assertEquals(Boolean.TRUE, Boolean.valueOf(order.getBuy()));
		verify(writer, new Times(1)).writeString(tags.ClOrdID, str("101"));
		
		this.sender.setDontDispatch(true);
		// this will send out the cancel
		this.app.onDisconnect();
		this.sender.dequeue();
		
		verify(writer, new Times(2)).writeString(tags.ClOrdID, str("101"));
		verify(writer, new Times(1)).writeString(tags.OrigClOrdID, str("101"));
		verify(writer).writeChar(tags.OrdStatus, FixConstants.OrdStatus.Canceled);
		verify(writer).writeChar(tags.ExecType, FixConstants.ExecType.Canceled);
	}

	@Test
	public void testRejectOnNegativeQty() {
		setInitialReqFields();
		tags.OrderQty.setValue(-10);

		app.onFixNewOrderSingle();

		MatchClientOrderRejectEvent reject = sender
				.getMessage(MatchClientOrderRejectEvent.class);
		String error = reject.getTextAsString();
		Assert.assertEquals("Invalid OrderQty<38>: -10", error);

		verify(store).createMessage(FixMsgTypes.ExecutionReport);
		verify(writer).writeString(tags.ClOrdID, str("101"));
		verify(writer).writeChar(tags.Side, FixConstants.Side.Buy);
		verify(writer).writeChar(tags.ExecType, FixConstants.ExecType.Rejected);
		verify(writer).writeChar(tags.OrdStatus,
				FixConstants.OrdStatus.Rejected);
		verify(writer).writeString(tags.Text, str(error));
	}

	@Test
	public void testRejectOnZeroPrice() {
		setInitialReqFields();
		tags.Price.setValue(0);

		app.onFixNewOrderSingle();

		MatchClientOrderRejectEvent reject = sender
				.getMessage(MatchClientOrderRejectEvent.class);

		Assert.assertEquals("Invalid Price<44>: 0", reject.getTextAsString());
	}

	@Test
	public void testRejectOnNegativePrice() {
		setInitialReqFields();
		tags.Price.setValue(-99.99);

		app.onFixNewOrderSingle();

		MatchClientOrderRejectEvent reject = sender
				.getMessage(MatchClientOrderRejectEvent.class);

		Assert.assertEquals("Invalid Price<44>: -99.99",
				reject.getTextAsString());
	}

	@Test
	public void testRejectOnInvalidSecurity() {
		setInitialReqFields();
		tags.Symbol.setValue("9Y");

		app.onFixNewOrderSingle();

		MatchClientOrderRejectEvent reject = sender
				.getMessage(MatchClientOrderRejectEvent.class);

		Assert.assertEquals("Invalid Symbol<55>: 9Y", reject.getTextAsString());
	}

	@Test
	public void testSendOrder() {
		setInitialReqFields();

		app.onFixNewOrderSingle();

		MatchOrderEvent order = sender.getMessage(MatchOrderEvent.class);
		Assert.assertEquals("101", order.getClOrdIDAsString());
		Assert.assertEquals((short) 1, order.getSecurityID());
		Assert.assertEquals(10, order.getQty());
		Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), order.getPrice());
		Assert.assertEquals(Boolean.TRUE, Boolean.valueOf(order.getBuy()));
	}

	@Test
	public void testSendOrder_riskCheckFailed() {
		setInitialReqFields();

		app.onFixNewOrderSingle();

		MatchOrderEvent order = sender.getMessage(MatchOrderEvent.class);
		Assert.assertEquals("101", order.getClOrdIDAsString());
		Assert.assertEquals((short) 1, order.getSecurityID());
		Assert.assertEquals(10, order.getQty());
		Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), order.getPrice());
		Assert.assertEquals(Boolean.TRUE, Boolean.valueOf(order.getBuy()));
	}

	@Test
	public void testSendOrderTIFIOC() {
		setInitialReqFields();
		tags.TimeInForce.setValue(FixConstants.TimeInForce.IOC);

		app.onFixNewOrderSingle();

		MatchOrderEvent order = sender.getMessage(MatchOrderEvent.class);
		Assert.assertEquals("101", order.getClOrdIDAsString());
		Assert.assertEquals((short) 1, order.getSecurityID());
		Assert.assertEquals(10, order.getQty());
		Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), order.getPrice());
		Assert.assertTrue(order.getBuy());
		Assert.assertTrue(order.getIOC());

		verify(store).createMessage(FixMsgTypes.ExecutionReport);
		verify(writer).writeChar(tags.TimeInForce, FixConstants.TimeInForce.IOC);
	}

	@Test
	public void testSendOrderTIFDay() {
		setInitialReqFields();
		tags.TimeInForce.setValue(FixConstants.TimeInForce.Day);

		app.onFixNewOrderSingle();

		MatchOrderEvent order = sender.getMessage(MatchOrderEvent.class);
		Assert.assertEquals("101", order.getClOrdIDAsString());
		Assert.assertEquals((short) 1, order.getSecurityID());
		Assert.assertEquals(10, order.getQty());
		Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), order.getPrice());
		Assert.assertTrue(order.getBuy());
		Assert.assertFalse(order.getIOC());

		verify(store).createMessage(FixMsgTypes.ExecutionReport);
		verify(writer).writeChar(tags.TimeInForce, FixConstants.TimeInForce.Day);
	}

	@Test
	public void testRejectOnTooLongOfClOrdID() {
		setInitialReqFields();
		tags.MsgType.setValue(FixMsgTypes.NewOrderSingle);
		tags.ClOrdID.setValue("REALLY LONG CLORDID VALUE REJECT ME");

		app.onFixNewOrderSingle();

		MatchInboundEvent heartbeat = sender.getMessage(MatchInboundEvent.class);
		Assert.assertEquals(FixMsgTypes.Heartbeat, heartbeat.getFixMsgType());

		MatchOutboundEvent reject = sender.getMessage(MatchOutboundEvent.class);
		Assert.assertEquals(FixMsgTypes.NewOrderSingle, reject.getRefMsgType());
		String error = reject.getTextAsString();
		Assert.assertEquals(
				"ClOrdID<11> length: REALLY LONG CLORDID VALUE REJECT ME",
				error);
	}

	@Test
		 public void testReplaceRejectOnSurpassingDV01Limit()
	{
		setInitialReqFields();
		tags.OrderQty.setValue(300);
		tags.Price.setValue("20");
		app.onFixNewOrderSingle();


		tags.OrderQty.setValue(900);
		tags.OrigClOrdID.setValue(tags.ClOrdID.getValueAsInt());
		tags.ClOrdID.setValue(tags.ClOrdID.getValueAsInt() + 1);
		app.onFixOrderCancelReplaceRequest();
	}


	@Test
	public void testReplaceRejectOnSurpassingFatFingerLimit()
	{
		setInitialReqFields();
		tags.OrderQty.setValue(300);
		tags.Price.setValue("1");
		app.onFixNewOrderSingle();
		//We know this is successful, and we need to drain send queue
		MatchOrderEvent orderSuccess = sender
				.getMessage(MatchOrderEvent.class);

		//price is small so we wont trigger dv01 limit and only ff limit

		tags.OrderQty.setValue(20000);
		tags.OrigClOrdID.setValue(tags.ClOrdID.getValueAsInt());
		tags.ClOrdID.setValue(tags.ClOrdID.getValueAsInt() + 1);
		app.onFixOrderCancelReplaceRequest();

		MatchClientCancelReplaceRejectEvent reject = sender
				.getMessage(MatchClientCancelReplaceRejectEvent.class);
		Assert.assertEquals("Violation of Fat Finger risk check: FOO", reject.getTextAsString());

	}

	@Test
	public void testCancel() {
		setInitialReqFields();
		app.onFixNewOrderSingle();

		tags.OrigClOrdID.setValue(tags.ClOrdID.getValueAsInt());
		tags.ClOrdID.setValue(tags.ClOrdID.getValueAsInt() + 1);
		app.onFixOrderCancelRequest();
	}

	@Test
	public void testRejectOnDuplicateClOrdId() {
		setInitialReqFields();

		app.onFixNewOrderSingle();

		sender.getMessage(MatchOrderEvent.class);

		verify(store).createMessage(FixMsgTypes.ExecutionReport);
		verify(writer).writeString(tags.ClOrdID, str("101"));
		verify(writer).writeChar(tags.ExecType, FixConstants.ExecType.New);
		verify(writer).writeChar(tags.OrdStatus, FixConstants.OrdStatus.New);

		app.onFixNewOrderSingle();

		MatchClientOrderRejectEvent reject = sender
				.getMessage(MatchClientOrderRejectEvent.class);

		String error = reject.getTextAsString();
		Assert.assertEquals("Duplicate ClOrdID<11>: 101", error);

		verify(store, new Times(2)).createMessage(FixMsgTypes.ExecutionReport);
		verify(writer, new Times(2)).writeString(tags.ClOrdID, str("101"));
		verify(writer).writeChar(tags.ExecType, FixConstants.ExecType.Rejected);
		verify(writer).writeChar(tags.OrdStatus,
				FixConstants.OrdStatus.Rejected);
		verify(writer).writeString(tags.Text, str(error));
	}

	private void setInitialReqFields()
	{
		setInitialReqFields("101");
	}
	
	private void setInitialReqFields(String clOrdId) { 
		this.app.onOpen(0);
		tags.ClOrdID.setValue(clOrdId);
		tags.Side.setValue(FixConstants.Side.Buy);
		tags.OrderQty.setValue(10);
		tags.Symbol.setValue("10Y");
		tags.SecurityID.setValue("912828L32");
		tags.Price.setValue(100.00);
		tags.Account.setValue("BAR");
		tags.OrdType.setValue('2');

		MatchSecurityCommand command = msgs.getMatchSecurityCommand();
		command.setSecurityID((short) 1);
		command.setName(ByteBuffer.wrap("10Y".getBytes()));
		command.setCUSIP(ByteBuffer.wrap("912828L32".getBytes()));
		command.setMaturityDate(20440815);
		command.setCoupon((long) (2.50 * MatchPriceUtils.getPriceMultiplier()));
		command.setType('1');
		command.setTickSize(100);
        command.setCouponFrequency((byte) 2);
        command.setIssueDate(20140815);
		command.setType(MatchConstants.SecurityType.TreasuryBond);
		dispatcher.dispatch(command);
	}

	private static ByteBuffer str(String str) {
		return ByteBuffer.wrap(str.getBytes());
	}
}
