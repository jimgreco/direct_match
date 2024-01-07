package com.core.match.fix.stp;

import com.core.fix.FixMessage;
import com.core.fix.msgs.FixConstants;
import com.core.fix.msgs.FixTags;
import com.core.match.fix.FixOrder;
import com.core.match.fix.GenericFIXAppTest;
import com.core.match.msgs.MatchConstants;
import com.core.util.TimeUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDate;

/**
 * User: jgreco
 */
public class FIXSTPTest extends GenericFIXAppTest<FixOrder> {
	private FixSTP app;
	private int testTradeDate=20150828;
	private int testSettlementDate;

	public FIXSTPTest() {
		super(FixOrder.class, 4, "SENDER", "TARGET");
	}

	@Before
	public void setup() {

        timeSource.setDate(testTradeDate);
		//This is the mock logic for settle date which is inconsistent with the logic in GenericAppTest
		testSettlementDate= TimeUtils.toDateInt(LocalDate.of(2016,3,14));

		app = new FixSTP(
                log,
				timers,
				dispatcher,
				sender,
				msgs,
				traders,
				securities,
				accounts,
				fixConnector,
				fixParser,
				fixWriter,
				fixStore,
				fixDispatcher,
                fixInfo,
				"FOO", referenceBBOBookService);

		sendContributor("STP01");
		sendAccount("FOO", 100, "XXX", false, 5.0);
		sendTrader("BAR", "FOO");
		sendBond("5Y");
		sendBond("10Y");
		sendBond("20Y");
		this.sendSpread("5Y10Y","5Y","10Y",4,1);
		this.sendButterfly("5Y10Y20Y","5Y","10Y","20Y", 4,8,1);

		this.app.setActive();
	}

	@Ignore("tmp ignored")
	@Test
	public void testTradeCaptureReport() {
		int id = sendPassiveOrder("BAR", true, 10, "10Y", 100.0);
		sendFill(true, id, 5, 100.0);

        FixMessage fixMessage = fixConnector.get(0);
		Assert.assertEquals("AE", fixMessage.getMsgTypeAsString());
		Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.NoSides));
		Assert.assertEquals(id + "_CLORDID", fixMessage.getValueAsString(FixTags.ClOrdID));
		Assert.assertEquals(FixConstants.Side.Buy, fixMessage.getValueAsChar(FixTags.Side));
		Assert.assertEquals("10Y", fixMessage.getValueAsString(FixTags.Symbol));
		Assert.assertEquals("BAR", fixMessage.getValueAsString(FixTags.Account));
		Assert.assertEquals(5.0 * 5, fixMessage.getValueAsPrice(FixTags.Commission), 0.001);
		Assert.assertEquals(FixConstants.CommissionType.Absolute, fixMessage.getValueAsChar(FixTags.CommissionType), 0.001);
		Assert.assertEquals("X1", fixMessage.getValueAsString(FixTags.ExecID));
		Assert.assertEquals(100.0, fixMessage.getValueAsPrice(FixTags.LastPx), 0.1);
		Assert.assertEquals(5 * MatchConstants.QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.LastShares));
		Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.TradeReportID));
		Assert.assertEquals(testTradeDate, fixMessage.getValueAsInt(FixTags.TradeDate));
		Assert.assertEquals(testSettlementDate, fixMessage.getValueAsInt(FixTags.SettlementDate));
		Assert.assertEquals('N', fixMessage.getValueAsChar(FixTags.PreviouslyReported));
//		Assert.assertEquals(5000025, fixMessage.getValueAsPrice(FixTags.NetMoney), 0.01);
	}

	@Test
	public void testAggregateAggressiveFills() {
		int id = sendAggressiveOrder("BAR", true, 10, "10Y", 100.0, false);
		sendFill(false, id, 3, 100.0);
		sendFill(false, id, 3, 101.0);
		sendFill(true, id, 3, 102.0);

		FixMessage fixMessage = fixConnector.get(0);
		Assert.assertEquals("AE", fixMessage.getMsgTypeAsString());
		Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.NoSides));
		Assert.assertEquals(id + "_CLORDID", fixMessage.getValueAsString(FixTags.ClOrdID));
		Assert.assertEquals("10Y", fixMessage.getValueAsString(FixTags.Symbol));
		Assert.assertEquals("BAR", fixMessage.getValueAsString(FixTags.Account));
		Assert.assertEquals(9.0 * 5, fixMessage.getValueAsPrice(FixTags.Commission), 0.001);
		Assert.assertEquals(FixConstants.CommissionType.Absolute, fixMessage.getValueAsChar(FixTags.CommissionType), 0.001);
		Assert.assertEquals("X3", fixMessage.getValueAsString(FixTags.ExecID));
		Assert.assertEquals(101.0, fixMessage.getValueAsPrice(FixTags.LastPx), 0.1);
		Assert.assertEquals(9 * MatchConstants.QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.LastShares));
		Assert.assertEquals(3, fixMessage.getValueAsInt(FixTags.TradeReportID));
		Assert.assertEquals(testTradeDate, fixMessage.getValueAsInt(FixTags.TradeDate));
		Assert.assertEquals(testSettlementDate, fixMessage.getValueAsInt(FixTags.SettlementDate));
		Assert.assertEquals('N', fixMessage.getValueAsChar(FixTags.PreviouslyReported));
	}

	@Ignore("tmp ignored")
	@Test
	public void testWeirdFillBehavior() {
		// This is really bad if this happens
		int id = sendPassiveOrder("BAR", false, 10, "10Y", 100.0);
		int id2 = sendPassiveOrder("BAR", false, 10, "10Y", 100.0);

		sendFill(false, id, 1, 100.0);
		sendFill(true, id2, 3, 100.0);
		sendFill(true, id, 2, 100.0);

		FixMessage fixMessage = fixConnector.get(0);
		Assert.assertEquals("AE", fixMessage.getMsgTypeAsString());
		Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.NoSides));
		Assert.assertEquals(id2 + "_CLORDID", fixMessage.getValueAsString(FixTags.ClOrdID));
		Assert.assertEquals("10Y", fixMessage.getValueAsString(FixTags.Symbol));
		Assert.assertEquals("BAR", fixMessage.getValueAsString(FixTags.Account));
		Assert.assertEquals(3.0 * 5, fixMessage.getValueAsPrice(FixTags.Commission), 0.001);
		Assert.assertEquals(FixConstants.CommissionType.Absolute, fixMessage.getValueAsChar(FixTags.CommissionType), 0.001);
		Assert.assertEquals("X2", fixMessage.getValueAsString(FixTags.ExecID));
		Assert.assertEquals(100.0, fixMessage.getValueAsPrice(FixTags.LastPx), 0.1);
		Assert.assertEquals(3 * MatchConstants.QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.LastShares));
		Assert.assertEquals(2, fixMessage.getValueAsInt(FixTags.TradeReportID));
		Assert.assertEquals(testTradeDate, fixMessage.getValueAsInt(FixTags.TradeDate));
		Assert.assertEquals(testSettlementDate, fixMessage.getValueAsInt(FixTags.SettlementDate));
		Assert.assertEquals('N', fixMessage.getValueAsChar(FixTags.PreviouslyReported));
//		Assert.assertEquals(2999985, fixMessage.getValueAsPrice(FixTags.NetMoney), 0.01);

		fixMessage = fixConnector.get(1);
		Assert.assertEquals("AE", fixMessage.getMsgTypeAsString());
		Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.NoSides));
		Assert.assertEquals(id + "_CLORDID", fixMessage.getValueAsString(FixTags.ClOrdID));
		Assert.assertEquals("10Y", fixMessage.getValueAsString(FixTags.Symbol));
		Assert.assertEquals("BAR", fixMessage.getValueAsString(FixTags.Account));
		Assert.assertEquals(2.0 * 5, fixMessage.getValueAsPrice(FixTags.Commission), 0.001);
		Assert.assertEquals(FixConstants.CommissionType.Absolute, fixMessage.getValueAsChar(FixTags.CommissionType), 0.001);
		Assert.assertEquals("X3", fixMessage.getValueAsString(FixTags.ExecID));
		Assert.assertEquals(100.0, fixMessage.getValueAsPrice(FixTags.LastPx), 0.1);
		Assert.assertEquals(2 * MatchConstants.QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.LastShares));
		Assert.assertEquals(3, fixMessage.getValueAsInt(FixTags.TradeReportID));
		Assert.assertEquals(testTradeDate, fixMessage.getValueAsInt(FixTags.TradeDate));
		Assert.assertEquals(testSettlementDate, fixMessage.getValueAsInt(FixTags.SettlementDate));
		Assert.assertEquals('N', fixMessage.getValueAsChar(FixTags.PreviouslyReported));
		//Assert.assertEquals(1999990, fixMessage.getValueAsPrice(FixTags.NetMoney), 0.01);
	}

	@Test
	public void testPassiveFills() {
		int id = sendPassiveOrder("BAR", true, 10, "10Y", 100.0);
		sendFill(false, id, 3, 100.0);
		sendFill(false, id, 3, 101.0);

		FixMessage fixMessage = fixConnector.get(0);
		Assert.assertEquals("AE", fixMessage.getMsgTypeAsString());
		Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.NoSides));
		Assert.assertEquals(id + "_CLORDID", fixMessage.getValueAsString(FixTags.ClOrdID));
		Assert.assertEquals("10Y", fixMessage.getValueAsString(FixTags.Symbol));
		Assert.assertEquals("BAR", fixMessage.getValueAsString(FixTags.Account));
		Assert.assertEquals(3.0 * 5, fixMessage.getValueAsPrice(FixTags.Commission), 0.001);
		Assert.assertEquals(FixConstants.CommissionType.Absolute, fixMessage.getValueAsChar(FixTags.CommissionType), 0.001);
		Assert.assertEquals("X1", fixMessage.getValueAsString(FixTags.ExecID));
		Assert.assertEquals(100.0, fixMessage.getValueAsPrice(FixTags.LastPx), 0.1);
		Assert.assertEquals(3 * MatchConstants.QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.LastShares));
		Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.TradeReportID));
		Assert.assertEquals(testTradeDate, fixMessage.getValueAsInt(FixTags.TradeDate));
		Assert.assertEquals(testSettlementDate, fixMessage.getValueAsInt(FixTags.SettlementDate));
		Assert.assertEquals('N', fixMessage.getValueAsChar(FixTags.PreviouslyReported));

        fixMessage = fixConnector.get(1);
        Assert.assertEquals("AE", fixMessage.getMsgTypeAsString());
        Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.NoSides));
        Assert.assertEquals(id + "_CLORDID", fixMessage.getValueAsString(FixTags.ClOrdID));
        Assert.assertEquals("10Y", fixMessage.getValueAsString(FixTags.Symbol));
        Assert.assertEquals("BAR", fixMessage.getValueAsString(FixTags.Account));
        Assert.assertEquals(3.0 * 5, fixMessage.getValueAsPrice(FixTags.Commission), 0.001);
        Assert.assertEquals(FixConstants.CommissionType.Absolute, fixMessage.getValueAsChar(FixTags.CommissionType), 0.001);
        Assert.assertEquals("X2", fixMessage.getValueAsString(FixTags.ExecID));
        Assert.assertEquals(101.0, fixMessage.getValueAsPrice(FixTags.LastPx), 0.1);
        Assert.assertEquals(3 * MatchConstants.QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.LastShares));
        Assert.assertEquals(2, fixMessage.getValueAsInt(FixTags.TradeReportID));
        Assert.assertEquals(testTradeDate, fixMessage.getValueAsInt(FixTags.TradeDate));
        Assert.assertEquals(testSettlementDate, fixMessage.getValueAsInt(FixTags.SettlementDate));
        Assert.assertEquals('N', fixMessage.getValueAsChar(FixTags.PreviouslyReported));
	}

	@Test
	public void testSpreadFills() {
		int id = sendPassiveOrder("BAR", true, 10, "5Y10Y", 100.0);
		sendFill(false, id, 10, 100.0);

		FixMessage fixMessage = fixConnector.get(0);
		Assert.assertEquals("AE", fixMessage.getMsgTypeAsString());
		Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.NoSides));
		Assert.assertEquals(id + "_CLORDID", fixMessage.getValueAsString(FixTags.ClOrdID));
		Assert.assertEquals("5Y", fixMessage.getValueAsString(FixTags.Symbol));
		Assert.assertEquals("BAR", fixMessage.getValueAsString(FixTags.Account));
		Assert.assertEquals(10 * 5, fixMessage.getValueAsPrice(FixTags.Commission), 0.001);
		Assert.assertEquals(FixConstants.CommissionType.Absolute, fixMessage.getValueAsChar(FixTags.CommissionType), 0.001);
		Assert.assertEquals("X1", fixMessage.getValueAsString(FixTags.ExecID));
		Assert.assertEquals(100.0, fixMessage.getValueAsPrice(FixTags.LastPx), 0.1);
		Assert.assertEquals(10 * MatchConstants.QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.LastShares));
		Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.TradeReportID));
		Assert.assertEquals(testTradeDate, fixMessage.getValueAsInt(FixTags.TradeDate));
		Assert.assertEquals(testSettlementDate, fixMessage.getValueAsInt(FixTags.SettlementDate));
		Assert.assertEquals('N', fixMessage.getValueAsChar(FixTags.PreviouslyReported));
		Assert.assertEquals(49.0, fixMessage.getValueAsChar(FixTags.NetMoney), 0.001);

		fixMessage = fixConnector.get(1);
		Assert.assertEquals("AE", fixMessage.getMsgTypeAsString());
		Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.NoSides));
		Assert.assertEquals(id + "_CLORDID", fixMessage.getValueAsString(FixTags.ClOrdID));
		Assert.assertEquals("10Y", fixMessage.getValueAsString(FixTags.Symbol));
		Assert.assertEquals("BAR", fixMessage.getValueAsString(FixTags.Account));
		Assert.assertEquals(2.0 * 5, fixMessage.getValueAsPrice(FixTags.Commission), 0.001);
		Assert.assertEquals(FixConstants.CommissionType.Absolute, fixMessage.getValueAsChar(FixTags.CommissionType), 0.001);
		Assert.assertEquals("X1", fixMessage.getValueAsString(FixTags.ExecID));
		Assert.assertEquals(100.0, fixMessage.getValueAsPrice(FixTags.LastPx), 0.1);
		Assert.assertEquals(2.5 * MatchConstants.QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.LastShares), 0.001);
		Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.TradeReportID));
		Assert.assertEquals(testTradeDate, fixMessage.getValueAsInt(FixTags.TradeDate));
		Assert.assertEquals(testSettlementDate, fixMessage.getValueAsInt(FixTags.SettlementDate));
		Assert.assertEquals('N', fixMessage.getValueAsChar(FixTags.PreviouslyReported));
		Assert.assertEquals(50.0, fixMessage.getValueAsChar(FixTags.NetMoney), 0.001);
	}

	@Test
	public void testButterflyFills() {
		int id = sendPassiveOrder("BAR", true, 10, "5Y10Y20Y", 100.0);
		sendFill(false, id, 10, 100.0);

		FixMessage fixMessage = fixConnector.get(0);
		Assert.assertEquals("AE", fixMessage.getMsgTypeAsString());
		Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.NoSides));
		Assert.assertEquals(id + "_CLORDID", fixMessage.getValueAsString(FixTags.ClOrdID));
		Assert.assertEquals("5Y", fixMessage.getValueAsString(FixTags.Symbol));
		Assert.assertEquals("BAR", fixMessage.getValueAsString(FixTags.Account));
		Assert.assertEquals(5 * 5, fixMessage.getValueAsPrice(FixTags.Commission), 0.001);
		Assert.assertEquals(FixConstants.CommissionType.Absolute, fixMessage.getValueAsChar(FixTags.CommissionType), 0.001);
		Assert.assertEquals("X1", fixMessage.getValueAsString(FixTags.ExecID));
		Assert.assertEquals(100.0, fixMessage.getValueAsPrice(FixTags.LastPx), 0.1);
		Assert.assertEquals(5 * MatchConstants.QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.LastShares));
		Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.TradeReportID));
		Assert.assertEquals(testTradeDate, fixMessage.getValueAsInt(FixTags.TradeDate));
		Assert.assertEquals(testSettlementDate, fixMessage.getValueAsInt(FixTags.SettlementDate));
		Assert.assertEquals('N', fixMessage.getValueAsChar(FixTags.PreviouslyReported));
		Assert.assertEquals(53.0, fixMessage.getValueAsChar(FixTags.NetMoney), 0.001);

		fixMessage = fixConnector.get(1);
		Assert.assertEquals("AE", fixMessage.getMsgTypeAsString());
		Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.NoSides));
		Assert.assertEquals(id + "_CLORDID", fixMessage.getValueAsString(FixTags.ClOrdID));
		Assert.assertEquals("10Y", fixMessage.getValueAsString(FixTags.Symbol));
		Assert.assertEquals("BAR", fixMessage.getValueAsString(FixTags.Account));
		Assert.assertEquals(10 * 5, fixMessage.getValueAsPrice(FixTags.Commission), 0.001);
		Assert.assertEquals(FixConstants.CommissionType.Absolute, fixMessage.getValueAsChar(FixTags.CommissionType), 0.001);
		Assert.assertEquals("X1", fixMessage.getValueAsString(FixTags.ExecID));
		Assert.assertEquals(100.0, fixMessage.getValueAsPrice(FixTags.LastPx), 0.1);
		Assert.assertEquals(10 * MatchConstants.QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.LastShares), 0.001);
		Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.TradeReportID));
		Assert.assertEquals(testTradeDate, fixMessage.getValueAsInt(FixTags.TradeDate));
		Assert.assertEquals(testSettlementDate, fixMessage.getValueAsInt(FixTags.SettlementDate));
		Assert.assertEquals('N', fixMessage.getValueAsChar(FixTags.PreviouslyReported));
		Assert.assertEquals(49, fixMessage.getValueAsChar(FixTags.NetMoney), 0.001);

		fixMessage = fixConnector.get(2);
		Assert.assertEquals("AE", fixMessage.getMsgTypeAsString());
		Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.NoSides));
		Assert.assertEquals(id + "_CLORDID", fixMessage.getValueAsString(FixTags.ClOrdID));
		Assert.assertEquals("20Y", fixMessage.getValueAsString(FixTags.Symbol));
		Assert.assertEquals("BAR", fixMessage.getValueAsString(FixTags.Account));
		Assert.assertEquals(1 * 5, fixMessage.getValueAsPrice(FixTags.Commission), 0.001);
		Assert.assertEquals(FixConstants.CommissionType.Absolute, fixMessage.getValueAsChar(FixTags.CommissionType), 0.001);
		Assert.assertEquals("X1", fixMessage.getValueAsString(FixTags.ExecID));
		Assert.assertEquals(100.0, fixMessage.getValueAsPrice(FixTags.LastPx), 0.1);
		Assert.assertEquals(1.25 * MatchConstants.QTY_MULTIPLIER, fixMessage.getValueAsInt(FixTags.LastShares), 0.001);
		Assert.assertEquals(1, fixMessage.getValueAsInt(FixTags.TradeReportID));
		Assert.assertEquals(testTradeDate, fixMessage.getValueAsInt(FixTags.TradeDate));
		Assert.assertEquals(testSettlementDate, fixMessage.getValueAsInt(FixTags.SettlementDate));
		Assert.assertEquals('N', fixMessage.getValueAsChar(FixTags.PreviouslyReported));
		Assert.assertEquals(49, fixMessage.getValueAsChar(FixTags.NetMoney), 0.001);
	}

}
