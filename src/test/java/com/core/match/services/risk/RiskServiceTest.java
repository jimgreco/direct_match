package com.core.match.services.risk;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.core.match.GenericAppTest;
import com.core.match.services.account.Account;
import com.core.match.services.order.AbstractOrder;
import com.core.match.services.security.Bond;
import com.core.match.services.trader.Trader;

/**
 * Created by johnlevidy on 5/19/15.
 */
public class RiskServiceTest extends GenericAppTest<RiskServiceTest.MyRiskOrderTest> {
    private static final String ACCOUNT_NAME = "ACCOUNT";
    private static final String TRADER_NAME = "TRADER";
    private static final short ACCOUNT_ID = ( short ) 1;
    private Account account;
    private Trader trader;
    private Bond security10Y;
    private Bond security3Y;
    private final int TenYrLimit=100000;
    private final int acct_max_dv01=200000;
    private final int trader_max_dv01=20000000;
    private final int trader_max_dv01_2=2000000;

    private final String TRADER_NAME_2="HLI";

    private RiskAccount riskAccount;
    private Trader trader2;
    private RiskService riskService;


    public RiskServiceTest() {
        super(MyRiskOrderTest.class);
    }

    public static class MyRiskOrderTest extends AbstractOrder<MyRiskOrderTest> implements MyRiskOrderAttributes {
    	private double filledDV01;
        private double unfilledDV01;


        @Override
		public double getFilledNetDV01Contribution()
		{
			return filledDV01;
		}

		@Override
		public void setFilledNetDV01Contribution(double dv01Contribution)
		{
			this.filledDV01 = dv01Contribution;
		}

        @Override
        public double getOpenDV01Contribution() {
            return unfilledDV01;
        }

        @Override
        public void setOpenDV01Contribution(double unfilledDV01) {
            this.unfilledDV01=unfilledDV01;
        }
    }

    public interface MyRiskOrderAttributes extends RiskOrderAttributes {
    }

    @Before
    public void setup()
    {
         riskService = new RiskService<>(accounts, traders, securities, log);
        this.orders.addListener(riskService);
        this.accounts.addListener(riskService);
        riskService.isInterestedInAccount(ACCOUNT_NAME);
        timeSource.setDate(20150518);

        sendAccount(ACCOUNT_NAME, acct_max_dv01, "NULL", false, 2.5);
        sendTrader(TRADER_NAME, ACCOUNT_NAME, 100000, 100000, 100000, 100000, 100000, TenYrLimit);
        sendTrader(TRADER_NAME_2, ACCOUNT_NAME, 100000, 100000, 100000, 100000, 100000, TenYrLimit);

        timeSource.setDate(20150518);

        sendSecurity("10Y", 2.5, 20250515, 20150515);
        sendSecurity("3Y", 2.5, 20250515, 20150515);
        sendSecurity("30Y", 2.5, 20450515, 20150515);

        security10Y=(Bond)securities.get("10Y");
        security3Y=(Bond)securities.get("10Y");


        account = accounts.get(ACCOUNT_ID);
        trader = traders.get(TRADER_NAME);

        trader2 = traders.get(TRADER_NAME_2);
        riskAccount=riskService.getAccount(account.getID());
    }

    private int sendOrder(int qty) {
        return sendPassiveOrder(TRADER_NAME, true, qty, "10Y", 100.0);
    }
    
    private void swapLimits()
    {
    	sendAccount(ACCOUNT_NAME, trader_max_dv01, "NULL", false, 3.5);
        sendTrader(TRADER_NAME, ACCOUNT_NAME, 100000, 100000, 100000, 100000, 100000, 100000);
    }

    @Test
    public void testAccountOrders()
    {
        //orderRiskChecks(account);
    }

    @Test
    public void verifyRiskChecksWorksLogically_exceedsAccountAndFatFingerRiskChecks() {

        Assert.assertTrue(riskService.violatesDV01Limit(account, acct_max_dv01+10));
        Assert.assertTrue(RiskService.violatesFatFingerQuantityLimit(trader, TenYrLimit + 1, security10Y));
    }

    @Test
    public void order_exceedsTraderRiskChecks() {

        Assert.assertTrue(riskService.violatesDV01Limit(account, 1000000));
        Assert.assertTrue(RiskService.violatesFatFingerQuantityLimit(trader, 500000 + 1, security10Y));
    }


    @Test
    public void testAccountCancels()
    {
        int orderID = sendOrder(60);
        Assert.assertFalse(riskService.violatesDV01Limit(account, 500));
        Assert.assertTrue(riskService.violatesDV01Limit(account, 1000000));

        sendCancel(orderID);
        Assert.assertFalse(riskService.violatesDV01Limit(account, 500));
        Assert.assertFalse(riskService.violatesDV01Limit(account, 100000));
    }

    @Test
    public void cancelRiskChecksAndDeleteOpenDv01() {
        int orderID = sendOrder(60);

        sendFill(true, orderID, 1, 100.0);
        MyRiskOrderTest buyOrder = orders.get(orderID);
        double openDv01=buyOrder.getOpenDV01Contribution();
        double filledDv01=buyOrder.getFilledNetDV01Contribution();

        sendCancel(orderID);

        assertEquals(0, buyOrder.getOpenDV01Contribution(), 0);
        assertEquals(filledDv01, buyOrder.getFilledNetDV01Contribution(), 0);

        //Since we have cancelled the remaining open qty, max exposed = all the filled dv01s
        assertEquals(filledDv01, riskAccount.getNetDV01(),0);
        assertEquals(filledDv01, riskAccount.getMaxExposedDV01(), 0);


    }
    @Test
    public void testAccountReplaces()
    {
        int orderID = sendOrder(60);

        Assert.assertFalse(riskService.violatesDV01Limit(account, 500));
        Assert.assertTrue(riskService.violatesDV01Limit(account, 1000000));

        sendPassiveReplace(orderID, 1, 100.0);
        Assert.assertFalse(riskService.violatesDV01Limit(account, 500));
        Assert.assertFalse(riskService.violatesDV01Limit(account, 197000));
    }

  public void testAccountFills()
    {
        int orderID = sendOrder(60);
        Assert.assertFalse(riskService.violatesDV01Limit(account, 500));
        Assert.assertTrue(riskService.violatesDV01Limit(account, 1000000));

        sendFill(true, orderID, 30, 100.0);
        // should just be exactly the same
        Assert.assertFalse(riskService.violatesDV01Limit(account, 500));
        Assert.assertTrue(riskService.violatesDV01Limit(account, 1100000));

        sendCancel(orderID);

        // even after the cancel, it still violates because of the filled qty
        Assert.assertFalse(riskService.violatesDV01Limit(account, 500));
        Assert.assertTrue(riskService.violatesDV01Limit(account, 1100000));
    }

    @Test
    public void testMaxExposedDV01_takesMaxOfBuyOrSell() {
        int buyOrderId = sendPassiveOrder(TRADER_NAME, true, 100, "10Y", 100.0);
        MyRiskOrderTest buyOrder=orders.get(buyOrderId);

        //Since we only have 1 open order, the max is the abs value of the open dv01 of the order
        assertEquals(buyOrder.getOpenDV01Contribution(), riskAccount.getMaxExposedDV01(), 0);
        //Fill is 0
        assertEquals(0, riskAccount.getNetDV01(), 0);

        //Send a smaller sell order
        int sellOrderId = sendPassiveOrder(TRADER_NAME, false, 10, "3Y", 100.0);
        MyRiskOrderTest sellOrder=orders.get(sellOrderId);

        //2 open orders, the max is the abs value of the open dv01 of the buy order
        assertEquals(buyOrder.getOpenDV01Contribution(), riskAccount.getMaxExposedDV01(), 0);
        //Fill is 0
        assertEquals(0, riskAccount.getNetDV01(), 0);
    }

    @Test
    public void testMaxExposedDV01_netDV01IsOnlyImpactedByFill() {
        int buyOrderId = sendPassiveOrder(TRADER_NAME, true, 100, "10Y", 100.0);
        int sellOrderId = sendPassiveOrder(TRADER_NAME, false, 10, "3Y", 100.0);

        //Fill is 0
        assertEquals(0, riskAccount.getNetDV01(),0);

        //Send a smaller sell order
        sendFill(true, buyOrderId, 30, 100.0);
        MyRiskOrderTest buyOrder=orders.get(buyOrderId);

        //netdv01 is the solely contributed by he fill
        assertEquals(buyOrder.getFilledNetDV01Contribution(), riskAccount.getNetDV01(), 0);
    }

    @Test
    public void testMaxExposedDV01_rejectTraderLimit() {
        int buyOrderId = sendPassiveOrder(TRADER_NAME_2, true, 100, "30Y", 100.0);
        MyRiskOrderTest order=orders.get(buyOrderId);

        //Fill is 0
        assertEquals(0, riskAccount.getNetDV01(),0);

        //Send a smaller sell order
        sendFill(true, buyOrderId, 30, 100.0);
        MyRiskOrderTest buyOrder=orders.get(buyOrderId);

        //netdv01 is the solely contributed by he fill
        assertEquals(buyOrder.getFilledNetDV01Contribution(), riskAccount.getNetDV01(), 0);
    }

    @Test
    public void testMaxExposedDV01_1buy1sell1fill_calculatesCorrectMaxExposedDV01() {
        int buyOrderId = sendPassiveOrder(TRADER_NAME, true, 100, "10Y", 100.0);
        int sellOrderId = sendPassiveOrder(TRADER_NAME, false, 10, "3Y", 100.0);

        //Fill is 0
        assertEquals(0, riskAccount.getNetDV01(), 0);

        //Send a smaller sell order
        sendFill(true, buyOrderId, 30, 100.0);
        MyRiskOrderTest buyOrder=orders.get(buyOrderId);
        MyRiskOrderTest sellOrder=orders.get(sellOrderId);

        double expectedMaxDV01=Math.max(Math.abs(buyOrder.getFilledNetDV01Contribution() + sellOrder.getFilledNetDV01Contribution() + buyOrder.getOpenDV01Contribution()), Math.abs(-sellOrder.getOpenDV01Contribution() + buyOrder.getFilledNetDV01Contribution() + sellOrder.getFilledNetDV01Contribution()));

        //netdv01 is the solely contributed by he fill
        assertEquals(expectedMaxDV01, riskAccount.getMaxExposedDV01(), 0);
    }

    @Test
    public void testMaxExposedDV01_2buy2sell2fill_calculatesCorrectMaxExposedDV01() {
        int buyOrderId = sendPassiveOrder(TRADER_NAME, true, 100, "10Y", 100.0);
        int sellOrderId = sendPassiveOrder(TRADER_NAME, false, 10, "3Y", 100.0);
        int buyOrderId2 = sendPassiveOrder(TRADER_NAME, true, 100, "10Y", 100.0);
        int sellOrderId2 = sendPassiveOrder(TRADER_NAME, false, 10, "3Y", 100.0);

        //Fill is 0
        assertEquals(0, riskAccount.getNetDV01(), 0);

        //Send a smaller sell order
        sendFill(true, buyOrderId, 30, 100.0);
        sendFill(true, sellOrderId2, 3, 100.0);

        MyRiskOrderTest buyOrder=orders.get(buyOrderId);
        MyRiskOrderTest sellOrder=orders.get(sellOrderId);
        MyRiskOrderTest buyOrder2=orders.get(buyOrderId2);
        MyRiskOrderTest sellOrder2=orders.get(sellOrderId2);

        //maxExposed= max{Netof B1,NetOf B2, Net}
        double expectedMaxDV01=Math.max(Math.abs(buyOrder.getFilledNetDV01Contribution() +sellOrder.getFilledNetDV01Contribution() + buyOrder2.getFilledNetDV01Contribution()+sellOrder2.getFilledNetDV01Contribution()
                        + buyOrder.getOpenDV01Contribution()+buyOrder2.getOpenDV01Contribution()),
                Math.abs(-sellOrder.getOpenDV01Contribution()-sellOrder2.getOpenDV01Contribution() + buyOrder.getFilledNetDV01Contribution() +sellOrder.getFilledNetDV01Contribution()+ buyOrder2.getFilledNetDV01Contribution() +sellOrder2.getFilledNetDV01Contribution()));

        //netdv01 is the solely contributed by he fill use e-2 epilson due to rounding erros in cal
        assertEquals(expectedMaxDV01, riskAccount.getMaxExposedDV01(), 1e-2);
    }

}
