package com.core.match.ouch;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.StubHeartbeatApp;
import com.core.fix.msgs.FixConstants.TimeInForce;
import com.core.match.GenericAppTest;
import com.core.match.StubMatchCommandSender;
import com.core.match.msgs.MatchCancelReplaceRejectCommand;
import com.core.match.msgs.MatchClientCancelReplaceRejectEvent;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchContributorCommand;
import com.core.match.msgs.MatchFillCommand;
import com.core.match.msgs.MatchOrderCommand;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchOrderRejectCommand;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.match.ouch.controller.OUCHOrderEntry;
import com.core.match.ouch.msgs.OUCHAcceptedCommand;
import com.core.match.ouch.msgs.OUCHAcceptedEvent;
import com.core.match.ouch.msgs.OUCHCancelCommand;
import com.core.match.ouch.msgs.OUCHCancelRejectedCommand;
import com.core.match.ouch.msgs.OUCHCancelRejectedEvent;
import com.core.match.ouch.msgs.OUCHCanceledCommand;
import com.core.match.ouch.msgs.OUCHCanceledEvent;
import com.core.match.ouch.msgs.OUCHConstants;
import com.core.match.ouch.msgs.OUCHConstants.Side;
import com.core.match.ouch.msgs.OUCHFillCommand;
import com.core.match.ouch.msgs.OUCHFillEvent;
import com.core.match.ouch.msgs.OUCHOrderCommand;
import com.core.match.ouch.msgs.OUCHRejectedCommand;
import com.core.match.ouch.msgs.OUCHRejectedEvent;
import com.core.match.ouch.msgs.OUCHReplaceCommand;
import com.core.match.ouch.msgs.OUCHReplacedCommand;
import com.core.match.ouch.msgs.OUCHReplacedEvent;
import com.core.match.ouch.msgs.OUCHTestMessages;
import com.core.match.ouch.msgs.OUCHTradeConfirmationCommand;
import com.core.match.ouch.msgs.OUCHTradeConfirmationEvent;
import com.core.match.util.MatchPriceUtils;
import com.core.util.PriceUtils;

public class OUCHOrderEntryTest extends GenericAppTest<OUCHOrder>
{
	protected TestOUCHAdapter ouchAdapter;
	protected OUCHOrderEntry orderEntry;
	private OUCHTestMessages ouchMessages;
	StubMatchCommandSender stubSender;
	private final String ouchPortAccount="DM";

	public OUCHOrderEntryTest()
	{
		super(OUCHOrder.class);
	}
	
	@Override
	@Before
	public void before() throws IOException
	{

		this.ouchMessages = new OUCHTestMessages(); 
		this.ouchAdapter = new TestOUCHAdapter(this.ouchMessages, this.dispatcher);
		this.ouchAdapter.setConnected(true);
		stubSender = new StubMatchCommandSender("TEST", (short)1, this.dispatcher);


		Mockito.when(this.ouchFactory.getOUCHAdaptor("Name",this.log,this.fileFactory,this.tcpSockets,this.timers,123,"UN","PW")).thenReturn(ouchAdapter);

		this.orderEntry = new OUCHOrderEntry(this.log, this.tcpSockets,this.fileFactory,this.timers, this.stubSender, this.dispatcher, this.msgs,
				this.accounts, this.traders, this.securities, this.systemEventService, this.contributors, this.referenceBBOBookService,connector,ouchFactory,"Name",123,"UN","PW",ouchPortAccount);

		HeartbeatFieldRegister register = new StubHeartbeatApp("TESTAPP");
		MatchContributorCommand command = msgs.getMatchContributorCommand();
		command.setCancelOnDisconnect(true);
		command.setContributorID((short) 0);
		command.setContributorSeq(1);
		command.setSourceContributorID((short) 1);

		this.dispatcher.dispatch(command);
		this.orderEntry.onAddHeartbeatFields(register);
		this.sendBond("2Y");
		this.sendBond("10Y");

		this.sendSpread("2Y10Y","2Y","10Y",4,1);

		this.openMarket();
		this.sendAccount(ouchPortAccount, 100000, "NULL", false, 3.5);
		this.sendTrader("Trader1", ouchPortAccount, 1000, 1000, 1000, 1000, 1000, 1000);
		this.orderEntry.setActive();

	}
	
	@Test
	public void testSecurityAsString()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234, "Trader1");
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getSecurityAsString(),"2Y");
	}

	@Test
	public void testMultipleCancelsOnDisconnect()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234, "Trader1");
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(),1234);
		
		ouchOrderCommand = getOuchOrderCommand(1, 100, 1235, "Trader1");
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(), 1235);
		
		ouchOrderCommand = getOuchOrderCommand(1, 100, 1236, "Trader1");
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(), 1236);
		
		this.ouchAdapter.setConnected(false);
		this.orderEntry.onDisconnect();
		OUCHCanceledEvent canceled = ((OUCHCanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1234, canceled.getClOrdID());
		Assert.assertEquals('U', canceled.getReason());
		
		canceled = ((OUCHCanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1236, canceled.getClOrdID());
		Assert.assertEquals('U', canceled.getReason());
		
		canceled = ((OUCHCanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1235, canceled.getClOrdID());
		Assert.assertEquals('U', canceled.getReason());
	}
	
	@Test
	public void testMultipleCancelsOnDisconnectReconnectMidwayThrough()
	{

		// 3 orders
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234, "Trader1");
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(),1234);
		
		ouchOrderCommand = getOuchOrderCommand(1, 100, 1235, "Trader1");
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(), 1235);
		
		ouchOrderCommand = getOuchOrderCommand(1, 100, 1236, "Trader1");
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(), 1236);
		
		this.stubSender.setDontDispatch(true);
		this.ouchAdapter.setConnected(false);
		this.orderEntry.onDisconnect();
		this.stubSender.dequeue();
		OUCHCanceledEvent canceled = ((OUCHCanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1234,  canceled.getClOrdID());
		Assert.assertEquals('U', canceled.getReason());
		
		// when this canceled comes back we'll send out the next one
		
		this.ouchAdapter.setConnected(true);
		this.orderEntry.onConnect();
		
		this.stubSender.dequeue();
		canceled = ((OUCHCanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1236,  canceled.getClOrdID());
		Assert.assertEquals('U', canceled.getReason());
		
		// no more cancels -- should also check to make sure no others were sent
		Assert.assertEquals(0, this.ouchAdapter.getQueue().size());
	}
	
	@Test
	public void testCancelOnDisconnect()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234, "Trader1");
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(),1234);

		this.ouchAdapter.setConnected(false);
		this.orderEntry.onDisconnect();
		OUCHCanceledEvent canceled = ((OUCHCanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1234,  canceled.getClOrdID());
		Assert.assertEquals('U', canceled.getReason());
	}
	
	@Test
	public void testMultipleTraders()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234, "Trader1");
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(),1234);
		ouchOrderCommand = getOuchOrderCommand(1, 100, 1235, "JIM");
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(),1235);
	}

	@Test
	public void onOUCHOrder_spread_orderAccepted()
	{
		//Arrange
		long price = com.core.util.PriceUtils.toLong(-0.98, MatchConstants.IMPLIED_DECIMALS);
		OUCHOrderCommand ouchOrderCommand = getSpreadOuchOrderCommand("2Y10Y",true,4, price, 1234, "Trader1");

		//Act
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());

		//Assert
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(),1234);
	}

	@Test
	public void onOUCHOrder_spreadInvalid_orderRejected()
	{
		//Arrange
		long price = com.core.util.PriceUtils.toLong(-0.98, MatchConstants.IMPLIED_DECIMALS);
		OUCHOrderCommand ouchOrderCommand = getSpreadOuchOrderCommand("10Y2Y",true,4, price, 1234, "Trader1");

		//Act
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());

		//Assert
		OUCHRejectedEvent event = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(),1234);
		Assert.assertEquals(OUCHConstants.RejectReason.InvalidSecurity,event.getReason());

	}

	@Test
	public void onOUCHOrder_spreadEmptyStringTrader_orderRejected()
	{
		//Arrange
		long price = com.core.util.PriceUtils.toLong(-0.98, MatchConstants.IMPLIED_DECIMALS);
		OUCHOrderCommand ouchOrderCommand = getSpreadOuchOrderCommand("2Y10Y",true,4, price, 1234, "");

		//Act
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());

		//Assert
		OUCHRejectedEvent event = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(),1234);
		Assert.assertEquals(OUCHConstants.RejectReason.InvalidTrader,event.getReason());
	}

	@Test
	public void onOUCHOrder_sameTraderTwoLargeOrderBuyAndSell_riskOffsetsEachOther()
	{
		//Arrange
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(5, 100, 1234, "JIM");
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(),1234);
		ouchOrderCommand = getOuchOrderCommand(5, 100, 1235, "JIM");
		ouchOrderCommand.setSide('S');
		//Act
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());

		//Assert
		OUCHAcceptedEvent acceptedEvent = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(acceptedEvent.getClOrdID(),1235);
	}
	
	@Test
	public void testRiskServiceWorksDifferentTraderSameAccount()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(100, 100, 1234, "Trader1");
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(),1234);
		ouchOrderCommand = getOuchOrderCommand(100, 100, 1235, "JIM");
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHRejectedEvent rejected = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(rejected.getClOrdID(),1235);
	}
	
	@Test
	public void testDuplicateClOrdIdWorks()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(),1234);
		ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHRejectedEvent reject = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1234,event.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.DuplicateClOrdID, reject.getReason());
	}
	
	@Test
	public void testReplaceRejectedClOrdIdNotFound()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(),1234);
		OUCHReplaceCommand ouchReplace = getOuchReplace(1000, 100, 1111);
		this.orderEntry.onOUCHReplace(ouchReplace.toEvent());
		OUCHRejectedEvent reject = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1112, reject.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.UnknownClOrdID, reject.getReason());
	}
	
	@Test
	public void testReplaceRejectedRiskViolation()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(),1234);
		OUCHReplaceCommand ouchReplace = getOuchReplace(1000, 100, 1234);
		this.orderEntry.onOUCHReplace(ouchReplace.toEvent());
		OUCHRejectedEvent reject = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1235, reject.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.RiskViolation, reject.getReason());
	}
	
	@Test
	public void testReplaceRejectedBadPrice()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(),1234);
		OUCHReplaceCommand ouchReplace = getOuchReplace(1, 0, 1234);
		this.orderEntry.onOUCHReplace(ouchReplace.toEvent());
		OUCHRejectedEvent reject = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1235, reject.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.InvalidPrice, reject.getReason());
	}
	
	@Test
	public void testReplaceRejectedBadQty()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(),1234);
		OUCHReplaceCommand ouchReplace = getOuchReplace(0, 100, 1234);
		this.orderEntry.onOUCHReplace(ouchReplace.toEvent());
		OUCHRejectedEvent reject = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1235, reject.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.InvalidQuantity, reject.getReason());
	}
	
	@Test
	public void testCancelRejectBySequencer()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());

		MatchOrderEvent orderMsg = stubSender.getMessage(MatchOrderEvent.class);
		Assert.assertEquals(1, orderMsg.getQty());
		Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), orderMsg.getPrice());

		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
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
		
		this.orderEntry.onCancelReplaceReject(this.orderEntry.getOrders().getOrders().get(1), reject.toEvent());
		
		OUCHCancelRejectedEvent rejectedEvent = ((OUCHCancelRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1234,  rejectedEvent.getClOrdID());
	}

	@Test
	public void testReplaceRejectBySequencer()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());

		MatchOrderEvent orderMsg = stubSender.getMessage(MatchOrderEvent.class);
		Assert.assertEquals(1, orderMsg.getQty());
		Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), orderMsg.getPrice());

		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
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
		this.orderEntry.onCancelReplaceReject(this.orderEntry.getOrders().getOrders().get(1), reject.toEvent());
		
		OUCHRejectedEvent rejectedEvent = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1235,  rejectedEvent.getClOrdID());
	}
	
	@Test
	public void testReplace()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());

		MatchOrderEvent orderMsg = stubSender.getMessage(MatchOrderEvent.class);
		Assert.assertEquals(1, orderMsg.getQty());
		Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), orderMsg.getPrice());

		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(), 1234);
		OUCHReplaceCommand ouchReplace = getOuchReplace(2, 100, 1234);

		this.orderEntry.onOUCHReplace(ouchReplace.toEvent());

		MatchReplaceEvent replaceMsg = stubSender.getMessage(MatchReplaceEvent.class);
		Assert.assertEquals(2, replaceMsg.getQty());
		Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), replaceMsg.getPrice());

		OUCHReplacedEvent replaced = ((OUCHReplacedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1235,  replaced.getClOrdID());
		Assert.assertEquals(1234,  replaced.getOldClOrdId());

	}

	@Test
	public void testReplace_replaceExceedFatfingerLimit()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());

		MatchOrderEvent orderMsg = stubSender.getMessage(MatchOrderEvent.class);
		Assert.assertEquals(1, orderMsg.getQty());
		Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), orderMsg.getPrice());

		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(), 1234);
		//Price is small enough that we only violate fat finger
		OUCHReplaceCommand ouchReplace = getOuchReplace(1001, 1, 1234);

		this.orderEntry.onOUCHReplace(ouchReplace.toEvent());

		MatchClientCancelReplaceRejectEvent replaceRejMsg = stubSender.getMessage(MatchClientCancelReplaceRejectEvent.class);
		//Reject reason is risk violation
		Assert.assertEquals('Z', replaceRejMsg.getReason());
		OUCHRejectedEvent reject = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1235, reject.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.RiskViolation, reject.getReason());


	}
	
	@Test
	public void testCancelRejected()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(),1234);
		OUCHCancelCommand ouchCancelCommand = getOuchCancel( 1 );
		this.orderEntry.onOUCHCancel(ouchCancelCommand.toEvent());
		OUCHCancelRejectedEvent reject = ((OUCHCancelRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1, reject.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.UnknownClOrdID, reject.getReason());
	}

	@Test
	public void testCancel()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(),1234);
		OUCHCancelCommand ouchCancelCommand = getOuchCancel( 1234 );
		this.orderEntry.onOUCHCancel(ouchCancelCommand.toEvent());
		OUCHCanceledEvent canceled = ((OUCHCanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1234,  canceled.getClOrdID());
		Assert.assertEquals('U', canceled.getReason());
	}

	@Test
	public void testReplaceThenCancel()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(), 1234);

		OUCHReplaceCommand ouchReplace = getOuchReplace(2, 100, 1234);
		this.orderEntry.onOUCHReplace(ouchReplace.toEvent());
		OUCHReplacedEvent replaced = ((OUCHReplacedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1235, replaced.getClOrdID());

		OUCHCancelCommand ouchCancelCommand = getOuchCancel(1234);
		this.orderEntry.onOUCHCancel(ouchCancelCommand.toEvent());
		OUCHCancelRejectedEvent reject = ((OUCHCancelRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1234, reject.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.UnknownClOrdID, reject.getReason());

		OUCHCancelCommand ouchCancelCommand2 = getOuchCancel(1235);
		this.orderEntry.onOUCHCancel(ouchCancelCommand2.toEvent());
		OUCHCanceledEvent canceled = ((OUCHCanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1235, canceled.getClOrdID());
		Assert.assertEquals(OUCHConstants.CanceledReason.UserRequest, canceled.getReason());
	}

	@Test
	public void testCancelThenReplace()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(), 1234);

		OUCHCancelCommand ouchCancelCommand = getOuchCancel(1234);
		this.orderEntry.onOUCHCancel(ouchCancelCommand.toEvent());
		OUCHCanceledEvent canceled = ((OUCHCanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1234, canceled.getClOrdID());
		Assert.assertEquals(OUCHConstants.CanceledReason.UserRequest, canceled.getReason());

		OUCHReplaceCommand ouchReplace = getOuchReplace(2, 100, 1234);
		this.orderEntry.onOUCHReplace(ouchReplace.toEvent());
		OUCHRejectedEvent reject = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1235, reject.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.UnknownClOrdID, reject.getReason());
	}

	@Test
	public void testCancelThenCancel()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(), 1234);

		OUCHCancelCommand ouchCancelCommand = getOuchCancel(1234);
		this.orderEntry.onOUCHCancel(ouchCancelCommand.toEvent());
		OUCHCanceledEvent canceled = ((OUCHCanceledCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1234, canceled.getClOrdID());
		Assert.assertEquals(OUCHConstants.CanceledReason.UserRequest, canceled.getReason());

		OUCHCancelCommand ouchCancelCommand2 = getOuchCancel(1234);
		this.orderEntry.onOUCHCancel(ouchCancelCommand2.toEvent());
		OUCHCancelRejectedEvent reject = ((OUCHCancelRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1234, reject.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.UnknownClOrdID, reject.getReason());
	}

	@Test
	public void testReplaceFullyFilledOrder()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(), 1234);

		MatchFillCommand command = this.msgs.getMatchFillCommand();
		command.setOrderID(1);
		command.setMatchID(1);
		command.setLastFill(true);
		command.setPrice(100 * MatchPriceUtils.getPriceMultiplier());
		command.setQty(1);
		this.dispatcher.dispatch(command);
		OUCHFillEvent fill = ((OUCHFillCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1234, fill.getClOrdID());
		Assert.assertEquals(OUCHConstants.Messages.Fill, fill.getMsgType());
		OUCHTradeConfirmationEvent confirm = ((OUCHTradeConfirmationCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1234, confirm.getClOrdID());

		OUCHReplaceCommand ouchReplace = getOuchReplace(2, 100, 1234);
		this.orderEntry.onOUCHReplace(ouchReplace.toEvent());
		OUCHRejectedEvent reject = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1235, reject.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.UnknownClOrdID, reject.getReason());
	}

	@Test
	public void testCancelFullyFilledOrder()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(), 1234);

		MatchFillCommand command = this.msgs.getMatchFillCommand();
		command.setOrderID(1);
		command.setMatchID(1);
		command.setLastFill(true);
		command.setPrice(100 * MatchPriceUtils.getPriceMultiplier());
		command.setQty(1);
		this.dispatcher.dispatch(command);
		OUCHFillEvent fill = ((OUCHFillCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1234, fill.getClOrdID());
		Assert.assertEquals(OUCHConstants.Messages.Fill, fill.getMsgType());
		OUCHTradeConfirmationEvent confirm = ((OUCHTradeConfirmationCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1234, confirm.getClOrdID());

		OUCHCancelCommand ouchCancelCommand = getOuchCancel(1234);
		this.orderEntry.onOUCHCancel(ouchCancelCommand.toEvent());
		OUCHCancelRejectedEvent reject = ((OUCHCancelRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1234, reject.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.UnknownClOrdID, reject.getReason());
	}
	
	@Test
	public void testMultipleFills()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(2, 100, 1234);
		this.stubSender.setDontDispatch(true);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		MatchOrderCommand cmd = (MatchOrderCommand) this.stubSender.pollDisconnectQueue();
		this.stubSender.setDontDispatch(false);
		this.stubSender.send(cmd);
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(), 1234);

		MatchFillCommand command = this.msgs.getMatchFillCommand();
		command.setOrderID(2);
		command.setMatchID(1);
		command.setPassive(true);
		command.setLastFill(false);
		command.setPrice(100 * MatchPriceUtils.getPriceMultiplier());
		command.setQty(1);
		this.dispatcher.dispatch(command);

		OUCHFillEvent fill = ((OUCHFillCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1234, fill.getClOrdID());
		Assert.assertEquals('E', fill.getMsgType());
		Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), fill.getExecutionPrice());
		Assert.assertEquals(1 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQty());
		
		// because this order is marked passive we should immediately see a trade confirmation message
		OUCHTradeConfirmationEvent confirm = ((OUCHTradeConfirmationCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1 * MatchConstants.QTY_MULTIPLIER, confirm.getExecQty());
		Assert.assertEquals(100 * Math.pow(10, MatchConstants.IMPLIED_DECIMALS), confirm.getExecPrice(), .0000001);
		Assert.assertEquals(1234, confirm.getClOrdID());
		Assert.assertEquals(1, confirm.getCommissionAmountAsDouble(),0);
		Assert.assertEquals(1, confirm.getMatchID());
		Assert.assertEquals("Trader1", confirm.getTraderAsString());
		Assert.assertEquals(OUCHConstants.Side.Buy, confirm.getSide());
		Assert.assertEquals('T', confirm.getMsgType());
		Assert.assertEquals("2Y_CUSIP", confirm.getSecurityAsString());

		command = this.msgs.getMatchFillCommand();
		command.setOrderID(2);
		command.setMatchID(2);
		command.setPassive(false);
		command.setLastFill(true);
		command.setPrice(95 * MatchPriceUtils.getPriceMultiplier());
		command.setQty(1);
		this.dispatcher.dispatch(command);
		fill = ((OUCHFillCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1234, fill.getClOrdID());
		Assert.assertEquals('E', fill.getMsgType());
		Assert.assertEquals(95 * MatchPriceUtils.getPriceMultiplier(), fill.getExecutionPrice());
		Assert.assertEquals(1 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQty());
		
		confirm = ((OUCHTradeConfirmationCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1 * MatchConstants.QTY_MULTIPLIER, confirm.getExecQty());
		Assert.assertEquals(95 * Math.pow(10, MatchConstants.IMPLIED_DECIMALS), confirm.getExecPrice(), .0000001);
		Assert.assertEquals(1234, confirm.getClOrdID());
		Assert.assertEquals(1, confirm.getCommissionAmountAsDouble(),0);
		Assert.assertEquals(2, confirm.getMatchID());
		Assert.assertEquals("Trader1", confirm.getTraderAsString());
		Assert.assertEquals(OUCHConstants.Side.Buy, confirm.getSide());
		Assert.assertEquals('T', confirm.getMsgType());
		Assert.assertEquals("2Y_CUSIP", confirm.getSecurityAsString());
		
		ouchOrderCommand = getOuchOrderCommand(3, 100, 4321);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(), 4321);
		command = this.msgs.getMatchFillCommand();
		command.setOrderID(3);
		command.setMatchID(3);
		command.setLastFill(false);
		command.setPrice(100 * MatchPriceUtils.getPriceMultiplier());
		command.setQty(2);
		this.dispatcher.dispatch(command);
		fill = ((OUCHFillCommand) this.ouchAdapter.getQueue().poll()).toEvent();
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
		fill = ((OUCHFillCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(4321, fill.getClOrdID());
		Assert.assertEquals('E', fill.getMsgType());
		Assert.assertEquals(95 * MatchPriceUtils.getPriceMultiplier(), fill.getExecutionPrice());
		Assert.assertEquals(1 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQty());
		
		confirm = ((OUCHTradeConfirmationCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(3 * MatchConstants.QTY_MULTIPLIER, confirm.getExecQty());
		Assert.assertEquals(98.333333333 * MatchPriceUtils.getPriceMultiplier(), confirm.getExecPrice(), .001);
		Assert.assertEquals(4321, confirm.getClOrdID());
		Assert.assertEquals(3, confirm.getCommissionAmountAsDouble(),0);
		Assert.assertEquals(2, confirm.getMatchID());
		Assert.assertEquals("Trader1", confirm.getTraderAsString());
		Assert.assertEquals(OUCHConstants.Side.Buy, confirm.getSide());
		Assert.assertEquals('T', confirm.getMsgType());
		Assert.assertEquals("2Y_CUSIP", confirm.getSecurityAsString());
	}
	
	@Test
	// ignore this for now -- test will have to change for this to work
	public void testFill()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(), 1234);
		MatchFillCommand command = this.msgs.getMatchFillCommand();
		command.setOrderID(1);
		command.setMatchID(1);
		command.setLastFill(true);
		command.setPrice(100 * MatchPriceUtils.getPriceMultiplier());
		command.setQty(1);
		this.dispatcher.dispatch(command);
		OUCHFillEvent fill = ((OUCHFillCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1234, fill.getClOrdID());
		Assert.assertEquals('E', fill.getMsgType());
		Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), fill.getExecutionPrice());
		Assert.assertEquals(1 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQty());
		
		OUCHTradeConfirmationEvent confirm = ((OUCHTradeConfirmationCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1 * MatchConstants.QTY_MULTIPLIER, confirm.getExecQty());
		Assert.assertEquals(100 * Math.pow(10, MatchConstants.IMPLIED_DECIMALS), confirm.getExecPrice(), 0);
		Assert.assertEquals(1234, confirm.getClOrdID());
		Assert.assertEquals(1, confirm.getCommissionAmountAsDouble(),0);
		Assert.assertEquals(1, confirm.getMatchID());
		Assert.assertEquals("Trader1", confirm.getTraderAsString());
		Assert.assertEquals(OUCHConstants.Side.Buy, confirm.getSide());
		Assert.assertEquals('T', confirm.getMsgType());
		Assert.assertEquals("2Y_CUSIP", confirm.getSecurityAsString());
	}
	
	
	@Test
	// ignore this for now -- test will have to change for this to work
	public void testReplaceInsufficientLeavesQuantity()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(2, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(), 1234);
		MatchFillCommand command = this.msgs.getMatchFillCommand();
		command.setOrderID(1);
		command.setLastFill(true);
		command.setMatchID(1);
		command.setPrice(100 * MatchPriceUtils.getPriceMultiplier());
		command.setQty(1);
		this.dispatcher.dispatch(command);
		OUCHFillEvent fillEvent = ((OUCHFillCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1234, fillEvent.getClOrdID());
		Assert.assertEquals('E', fillEvent.getMsgType());
		Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(), fillEvent.getExecutionPrice());
		Assert.assertEquals(1 * MatchConstants.QTY_MULTIPLIER, fillEvent.getExecutionQty());
		
		OUCHReplaceCommand ouchReplaceCommand = getOuchReplace(1, 100, 1234);
		this.orderEntry.onOUCHReplace(ouchReplaceCommand.toEvent());
		OUCHTradeConfirmationEvent confirm = ((OUCHTradeConfirmationCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1 * MatchConstants.QTY_MULTIPLIER, confirm.getExecQty());
		Assert.assertEquals(100 * Math.pow(10, MatchConstants.IMPLIED_DECIMALS), confirm.getExecPrice(), 0);
		Assert.assertEquals(1234, confirm.getClOrdID());
		Assert.assertEquals(1, confirm.getCommissionAmountAsDouble(),0);

		Assert.assertEquals(1, confirm.getMatchID());
		Assert.assertEquals("Trader1", confirm.getTraderAsString());
		Assert.assertEquals(OUCHConstants.Side.Buy, confirm.getSide());
		Assert.assertEquals('T', confirm.getMsgType());
		Assert.assertEquals("2Y_CUSIP", confirm.getSecurityAsString());	
		OUCHRejectedEvent reject = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().poll()).toEvent();
		Assert.assertEquals(1235, reject.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.InvalidQuantity, reject.getReason());
	}
	
	@Test
	public void testBasicClOrdIdWorks()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
		Assert.assertEquals(event.getClOrdID(),1234);
	}


	
	@Test
	public void testSequencerOrderReject()
	{
		MatchOrderRejectCommand command = this.msgs.getMatchOrderRejectCommand();
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putLong(1234L);
		buffer.flip();
		command.setClOrdID(buffer);
		this.orderEntry.onOrderReject(command.toEvent());
		
		OUCHRejectedEvent event = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
		Assert.assertEquals(1234,event.getClOrdID());
	}
	
	@Test
	public void testMarketClosed()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1, 100, 1234);
		this.closeMarket();
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHRejectedEvent event = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
		Assert.assertEquals(1234,event.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.TradingSystemClosed, event.getReason());
	}
		
	@Test
	public void testInvalidSecurityViolation()
	{
		OUCHOrderCommand ouchOrderCommand = this.ouchMessages.getOUCHOrderCommand();
		ouchOrderCommand.setClOrdID(1234);
		ouchOrderCommand.setPrice(100 * MatchPriceUtils.getPriceMultiplier());
		ouchOrderCommand.setQty(1);
		ouchOrderCommand.setSecurity(String.format( "%16s", "4Y"));
		ouchOrderCommand.setSide(Side.Buy);
		ouchOrderCommand.setTimeInForce(TimeInForce.Day);
		ouchOrderCommand.setTrader("JIM");
		
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHRejectedEvent event = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
		Assert.assertEquals(1234,event.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.InvalidSecurity, event.getReason());
	}

	@Test
	public void testInvalidSideViolation()
	{
		OUCHOrderCommand ouchOrderCommand = this.ouchMessages.getOUCHOrderCommand();
		ouchOrderCommand.setClOrdID(1234);
		ouchOrderCommand.setPrice(100 * MatchPriceUtils.getPriceMultiplier());
		ouchOrderCommand.setQty(1);
		ouchOrderCommand.setSecurity(String.format( "%16s", "2Y"));
		ouchOrderCommand.setSide('b');
		ouchOrderCommand.setTimeInForce(TimeInForce.Day);
		ouchOrderCommand.setTrader("JIM");
		
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHRejectedEvent event = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
		Assert.assertEquals(1234,event.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.InvalidSide, event.getReason());
	}
	

	@Test
	public void testQtyViolationNotFullLot()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1.5, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHRejectedEvent event = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
		Assert.assertEquals(1234,event.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.InvalidQuantity, event.getReason());
	}
	
	@Test
	public void testQtyViolation()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(0, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHRejectedEvent event = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
		Assert.assertEquals(1234,event.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.InvalidQuantity, event.getReason());
	}
	
	@Test
	public void testTickViolation()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(100, PriceUtils.toLong(100.333333, MatchConstants.IMPLIED_DECIMALS), 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHRejectedEvent event = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
		Assert.assertEquals(1234,event.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.InvalidPrice, event.getReason());
	}
	
	@Test
	public void testPriceViolation()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(100, 0, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHRejectedEvent event = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
		Assert.assertEquals(1234,event.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.InvalidPrice, event.getReason());
	}
	
	@Test
	public void testRiskViolation()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(200, 100, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHRejectedEvent event = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
		Assert.assertEquals(1234,event.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.RiskViolation, event.getReason());
	}

	@Test
	public void testRiskViolation_FatFingerQuantity()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(1001, 1, 1234);
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHRejectedEvent event = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
		Assert.assertEquals(1234,event.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.RiskViolation, event.getReason());
	}

	@Test
	public void onOUCHOrder_negativeQty_receieveRejects()
	{
		OUCHOrderCommand ouchOrderCommand = getOuchOrderCommand(-5, 100, 1234, "JIM");
		this.orderEntry.onOUCHOrder(ouchOrderCommand.toEvent());
		OUCHRejectedEvent event = ((OUCHRejectedCommand) this.ouchAdapter.getQueue().peek()).toEvent();
		Assert.assertEquals(1234,event.getClOrdID());
		Assert.assertEquals(OUCHConstants.RejectReason.InvalidQuantity, event.getReason());
	}

	private OUCHCancelCommand getOuchCancel(long i)
	{
		OUCHCancelCommand command = this.ouchMessages.getOUCHCancelCommand(); 
		command.setClOrdID(i);
		return command;
	}

	private OUCHReplaceCommand getOuchReplace(double qty, long price, long clOrdId)
	{
		OUCHReplaceCommand ouchOrderCommand = this.ouchMessages.getOUCHReplaceCommand();
		ouchOrderCommand.setClOrdID(clOrdId);
		ouchOrderCommand.setNewClOrdID(clOrdId + 1);
		ouchOrderCommand.setNewPrice(price * MatchPriceUtils.getPriceMultiplier());
		ouchOrderCommand.setNewQty((int) (qty * MatchConstants.QTY_MULTIPLIER));
		return ouchOrderCommand;
	}

	private OUCHOrderCommand getOuchOrderCommand(double qty, long price, long clOrdId)
	{
		return getOuchOrderCommand(qty, price, clOrdId, "JIM");
	}

	public OUCHOrderCommand getSpreadOuchOrderCommand(String spread, boolean isBuy, int qty, long price, long clOrdId, String trader)
	{
		OUCHOrderCommand ouchOrderCommand = ouchMessages.getOUCHOrderCommand();
		ouchOrderCommand.setClOrdID(clOrdId);
		ouchOrderCommand.setPrice(price * MatchPriceUtils.getPriceMultiplier());
		ouchOrderCommand.setQty(qty * MatchConstants.QTY_MULTIPLIER);
		ouchOrderCommand.setSecurity(spread);
		ouchOrderCommand.setSide(isBuy?Side.Buy:Side.Sell);
		ouchOrderCommand.setTimeInForce(TimeInForce.Day);
		if(trader!=null){
			ouchOrderCommand.setTrader(trader);
		}
		return ouchOrderCommand;	}


	private OUCHOrderCommand getOuchOrderCommand(double qty, long price, long clOrdId, String trader)
	{
		OUCHOrderCommand ouchOrderCommand = ouchMessages.getOUCHOrderCommand();
		ouchOrderCommand.setClOrdID(clOrdId);
		ouchOrderCommand.setPrice(price * MatchPriceUtils.getPriceMultiplier());
		ouchOrderCommand.setQty((int) (qty * MatchConstants.QTY_MULTIPLIER));
		ouchOrderCommand.setSecurity("2Y");
		ouchOrderCommand.setSide(Side.Buy);
		ouchOrderCommand.setTimeInForce(TimeInForce.Day);
		ouchOrderCommand.setTrader(trader);
		return ouchOrderCommand;
	}

}
