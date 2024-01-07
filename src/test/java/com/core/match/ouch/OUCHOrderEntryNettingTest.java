package com.core.match.ouch;

import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.StubHeartbeatApp;
import com.core.fix.msgs.FixConstants.TimeInForce;
import com.core.match.GenericAppTest;
import com.core.match.StubMatchCommandSender;
import com.core.match.msgs.*;
import com.core.match.ouch.controller.OUCHOrderEntry;
import com.core.match.ouch.msgs.*;
import com.core.match.ouch.msgs.OUCHConstants.Side;
import com.core.match.util.MatchPriceUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class OUCHOrderEntryNettingTest extends GenericAppTest<OUCHOrder>
{
	private TestOUCHAdapter ouchAdapter;
	private TestOUCHAdapter ouchAdapter2;

	private OUCHOrderEntry ouchEntryPortWithNettingAccount;
	private OUCHOrderEntry ouchEntryPortWithNonNettingAccount;

	private OUCHTestMessages ouchMessages;
	StubMatchCommandSender stubMatchCommandSender;

	public OUCHOrderEntryNettingTest()
	{
		super(OUCHOrder.class);
	}

	@Override
	@Before
	public void before() throws IOException {
		this.ouchMessages = new OUCHTestMessages();
		this.ouchAdapter = new TestOUCHAdapter(this.ouchMessages, this.dispatcher);
		this.ouchAdapter.setConnected(true);

		this.ouchAdapter2 = new TestOUCHAdapter(this.ouchMessages, this.dispatcher);
		this.ouchAdapter2.setConnected(true);
		Mockito.when(this.ouchFactory.getOUCHAdaptor("Name",this.log,this.fileFactory,this.tcpSockets,this.timers,123,"UN","PW")).thenReturn(ouchAdapter);
		Mockito.when(this.ouchFactory.getOUCHAdaptor("Name",this.log,this.fileFactory,this.tcpSockets,this.timers,456,"UN2","PW2")).thenReturn(ouchAdapter2);


		stubMatchCommandSender = new StubMatchCommandSender("TEST", (short)1, this.dispatcher);
		this.ouchEntryPortWithNettingAccount = new OUCHOrderEntry(this.log, this.tcpSockets,this.fileFactory,this.timers, this.stubMatchCommandSender, this.dispatcher, this.msgs,
				this.accounts, this.traders, this.securities, this.systemEventService, this.contributors, this.referenceBBOBookService, connector,ouchFactory,"Name",123,"UN","PW","DM");

		this.ouchEntryPortWithNonNettingAccount = new OUCHOrderEntry(this.log, this.tcpSockets,this.fileFactory,this.timers, this.stubMatchCommandSender, this.dispatcher, this.msgs,
				this.accounts, this.traders, this.securities, this.systemEventService, this.contributors,this.referenceBBOBookService, connector,ouchFactory,"Name",456,"UN2","PW2","DM_NotNetting");
		HeartbeatFieldRegister register = new StubHeartbeatApp("TESTAPP");
		HeartbeatFieldRegister register2 = new StubHeartbeatApp("TESTAPP");
		this.ouchEntryPortWithNettingAccount.onAddHeartbeatFields(register);
		this.ouchEntryPortWithNonNettingAccount.onAddHeartbeatFields(register2);

		MatchContributorCommand command = msgs.getMatchContributorCommand();
		command.setCancelOnDisconnect(true);
		command.setContributorID((short) 1);
		command.setContributorSeq(1);
		command.setSourceContributorID((short) 1);
		this.dispatcher.dispatch(command);

		this.sendBond("2Y");
		this.sendBond("10Y");
		this.sendSpread("2Y10Y","2Y","10Y",4,1);
		this.openMarket();
		this.sendAccount("DM", 100000,"KARUNA89X",true, 2.5);
		this.sendAccount("DM_NotNetting", 100000,"ABC",false, 2.5);

		this.sendTrader("JIM", "DM", 1000,1000,1000,1000,1000,1000);
		this.sendTrader("Trader1", "DM_NotNetting", 1000,1000,1000,1000,1000,1000);

		this.ouchEntryPortWithNettingAccount.setActive();


		this.ouchEntryPortWithNonNettingAccount.setActive();

	}


	@Test
	public void onFill_bondAccountISNetting_generateCorrectFillAndNOTradeConfirmation()
	{
		OUCHOrderCommand ouchOrderCommand = sendOUCHOrder(2, 100, 1234, "JIM", "2Y");
		this.stubMatchCommandSender.setDontDispatch(true);
		this.ouchEntryPortWithNettingAccount.onOUCHOrder(ouchOrderCommand.toEvent());
		MatchOrderCommand cmd = (MatchOrderCommand) this.stubMatchCommandSender.pollDisconnectQueue();
		this.stubMatchCommandSender.setDontDispatch(false);
		this.stubMatchCommandSender.send(cmd);
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

		ouchOrderCommand = sendOUCHOrder(3, 100, 4321, "JIM", "2Y");
		this.ouchEntryPortWithNettingAccount.onOUCHOrder(ouchOrderCommand.toEvent());
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


	}

	@Test
	public void onFill_spreadNotNettingAccount_generateCorrectFillAndTradeConfirmation() throws IOException {
		//Arrange
		OUCHOrderCommand ouchOrderCommand = sendOUCHOrder(4, -0.95, 1234, "Trader1", "2Y10Y");
		this.stubMatchCommandSender.setDontDispatch(true);
		this.ouchEntryPortWithNonNettingAccount.onOUCHOrder(ouchOrderCommand.toEvent());
		MatchOrderCommand cmd = (MatchOrderCommand) this.stubMatchCommandSender.pollDisconnectQueue();
		this.stubMatchCommandSender.setDontDispatch(false);
		this.stubMatchCommandSender.send(cmd);
		OUCHAcceptedEvent event = ((OUCHAcceptedCommand) this.ouchAdapter2.getQueue().poll()).toEvent();
		Assert.assertEquals(event.getClOrdID(), 1234);

		MatchFillCommand command = this.msgs.getMatchFillCommand();
		command.setOrderID(2);
		command.setMatchID(1);
		command.setPassive(true);
		command.setLastFill(false);
		command.setPrice(-0.95);
		command.setQty(4);

		//Act
		this.dispatcher.dispatch(command);

		//Assert
		OUCHFillEvent fill = ((OUCHFillCommand) this.ouchAdapter2.getQueue().poll()).toEvent();
		OUCHTradeConfirmationEvent tc1 = ((OUCHTradeConfirmationCommand) this.ouchAdapter2.getQueue().poll()).toEvent();
		OUCHTradeConfirmationEvent tc2 = ((OUCHTradeConfirmationCommand) this.ouchAdapter2.getQueue().poll()).toEvent();

		Assert.assertEquals(1234, fill.getClOrdID());
		Assert.assertEquals('E', fill.getMsgType());
		Assert.assertEquals(-0.95 * MatchPriceUtils.getPriceMultiplier(), fill.getExecutionPrice(),0);
		Assert.assertEquals(4 * MatchConstants.QTY_MULTIPLIER, fill.getExecutionQty());
		Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(),tc1.getExecPrice(),0);
		Assert.assertEquals("2Y_CUSIP",tc1.getSecurityAsString());
		Assert.assertEquals(4*MatchConstants.QTY_MULTIPLIER,tc1.getExecQty());

		Assert.assertEquals(100 * MatchPriceUtils.getPriceMultiplier(),tc2.getExecPrice(),0);
		Assert.assertEquals("10Y_CUSIP",tc2.getSecurityAsString());
		Assert.assertEquals(1*MatchConstants.QTY_MULTIPLIER,tc2.getExecQty());

	}
	
	private OUCHOrderCommand sendOUCHOrder(double qty, double price, long clOrdId, String trader, String security) {
		OUCHOrderCommand ouchOrderCommand = ouchMessages.getOUCHOrderCommand();
		ouchOrderCommand.setClOrdID(clOrdId);
		ouchOrderCommand.setPrice(price);
		ouchOrderCommand.setQty((int) (qty * MatchConstants.QTY_MULTIPLIER));
		ouchOrderCommand.setSecurity(security);
		ouchOrderCommand.setSide(Side.Buy);
		ouchOrderCommand.setTimeInForce(TimeInForce.Day);
		ouchOrderCommand.setTrader(trader);
		return ouchOrderCommand;
	}

}
