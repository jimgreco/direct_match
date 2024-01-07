package com.core.match.drops.gui;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.core.match.GenericAppTest;
import com.core.match.drops.DropCollection;
import com.core.match.drops.LinearCounter;
import com.core.match.drops.gui.msgs.GUIAccountRiskValues;
import com.core.match.services.risk.RiskService;

public class RiskDropCollectionTest extends GenericAppTest<RiskDropOrder> {
	private static final String ACCOUNT = "TESTACCOUNT";
	private static final String TRADER = "TESTTRADER";

	private RiskDropCollection collection;
	private RiskService<RiskDropOrder> riskService;

	public RiskDropCollectionTest() {
		super(RiskDropOrder.class);
	}

	@Before
	public void setup() {
		riskService = new RiskService<>(accounts, traders, securities, log);
		orders.addListener(riskService);
		orders.setIsInterestedListener(msg -> {
			return true;
		});

		collection = new RiskDropCollection(new LinearCounter(), new LinearCounter(), accounts, riskService);

		this.sendBond("2Y");
		this.openMarket();
		this.sendAccount(ACCOUNT, 10000, "NULL", false, 2.5);
		this.sendTrader(TRADER, ACCOUNT, 1000, 1000, 1000, 1000, 1000, 1000);
	}

	@Test
	public void sendingAndCancelingOrderUpdatesExposure() {
		DropCollection.DropIterator iterator = collection.getIterator(0);
		GUIAccountRiskValues accountRiskValues = (GUIAccountRiskValues) iterator.next();

		Assert.assertEquals(0, accountRiskValues.getOrderExposure());

		int orderId = sendPassiveOrder(TRADER, true, 10, "2Y", 100);

		iterator = collection.getIterator(1);
		accountRiskValues = (GUIAccountRiskValues) iterator.next();

		Assert.assertNotEquals(0, accountRiskValues.getOrderExposure());
		
		sendCancel(orderId);
		
		iterator = collection.getIterator(2);
		accountRiskValues = (GUIAccountRiskValues) iterator.next();

		Assert.assertEquals(0, accountRiskValues.getOrderExposure());
	}

}
