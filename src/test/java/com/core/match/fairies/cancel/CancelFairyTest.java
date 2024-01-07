package com.core.match.fairies.cancel;

import com.core.match.GenericAppTest;
import com.core.match.msgs.MatchCancelEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jgreco on 7/14/15.
 */
public class CancelFairyTest extends GenericAppTest<CancelFairyOrder> {
    private CancelFairy fairy;

    public CancelFairyTest() {
        super(CancelFairyOrder.class);
    }

    @Override
	@Before
    public void before() {
        fairy = new CancelFairy(log, sender, msgs, dispatcher, traders, accounts, securities, contributors, systemEventService);
        fairy.setActive();

        openMarket();

        sendContributor("OUCH01");
        sendContributor("OUCH02");
        sendContributor("OUCH03");
        sendAccount("JPMS");
        sendAccount("BCAP");
        sendTrader("JGRECO", "JPMS");
        sendTrader("JLEVIDY", "JPMS");
        sendTrader("KNELLER", "BCAP");
        sendBond("5Y");
        sendBond("10Y");

        setContributor("OUCH01");
        sendPassiveOrder("JGRECO", true, 1, "10Y", 100.0);
        sendPassiveOrder("JGRECO", false, 1, "10Y", 101.0);
        sendPassiveOrder("JGRECO", true, 1, "10Y", 100.0);
        sendPassiveOrder("JGRECO", false, 1, "5Y", 101.0);

        sendPassiveOrder("JLEVIDY", true, 1, "5Y", 100.0);

        setContributor("OUCH02");
        sendPassiveOrder("JLEVIDY", false, 1, "5Y", 100.0);
        sendPassiveOrder("JLEVIDY", true, 1, "10Y", 100.0);

        setContributor("OUCH03");
        sendPassiveOrder("KNELLER", true, 1, "10Y", 100.0);
    }

    @Test
    public void testCancelContributor() {
        fairy.cancelContributor("OUCH01");

        verifyCancel(1);
        verifyCancel(2);
        verifyCancel(3);
        verifyCancel(4);
        verifyCancel(5);
    }

    @Test
    public void testCancelContributor2() {
        fairy.cancelContributor("OUCH02");

        verifyCancel(6);
        verifyCancel(7);
    }

    @Test
    public void testCancelContributor3() {
        fairy.cancelContributor("OUCH03");

        verifyCancel(8);
    }

    @Test
    public void testCancelTrader1() {
        fairy.cancelTrader("JGRECO");

        verifyCancel(1);
        verifyCancel(2);
        verifyCancel(3);
        verifyCancel(4);
    }

    @Test
    public void testCancelTrader2() {
        fairy.cancelTrader("JLEVIDY");

        verifyCancel(5);
        verifyCancel(6);
        verifyCancel(7);
    }

    @Test
    public void testCancelTrader3() {
        fairy.cancelTrader("KNELLER");

        verifyCancel(8);
    }

    @Test
    public void testCancelSecurity1() {
        fairy.cancelSecurity("5Y");

        verifyCancel(4);
        verifyCancel(5);
        verifyCancel(6);
    }


    @Test
    public void testCancelSecurity2() {
        fairy.cancelSecurity("10Y");

        verifyCancel(1);
        verifyCancel(2);
        verifyCancel(3);
        verifyCancel(7);
        verifyCancel(8);
    }

    @Test
    public void testAccount1() {
        fairy.cancelAccount("JPMS");

        verifyCancel(1);
        verifyCancel(2);
        verifyCancel(3);
        verifyCancel(4);
        verifyCancel(5);
        verifyCancel(6);
        verifyCancel(7);
    }

    @Test
    public void testAccount2() {
        fairy.cancelAccount("BCAP");

        verifyCancel(8);
    }

    @Test
    public void testCanceled() {
        sender.setDispatchAllCommandsCleared(false);
        fairy.cancelAccount("JPMS");

        verifyCancel(1);

        sendCancel(2);
        sendCancel(4);
        sendCancel(7);

        sender.setDispatchAllCommandsCleared(true);
        fairy.onAllCommandsCleared();

        verifyCancel(3);
        verifyCancel(5);
        verifyCancel(6);
    }

    @Test
    public void testCancelAll() {
        fairy.cancelAll();

        // hash map order
        verifyCancel(1);
        verifyCancel(7);
        verifyCancel(8);
        verifyCancel(2);
        verifyCancel(6);
        verifyCancel(3);
        verifyCancel(5);
        verifyCancel(4);
    }

    @Test
    public void testCancelAllOnClose() {
        closeMarket();

        // hash map order
        verifyCancel(1);
        verifyCancel(7);
        verifyCancel(8);
        verifyCancel(2);
        verifyCancel(6);
        verifyCancel(3);
        verifyCancel(5);
        verifyCancel(4);
    }

    private void verifyCancel(int id) {
        Assert.assertEquals(id, sender.getMessage(MatchCancelEvent.class).getOrderID());
    }
}
