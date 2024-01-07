package com.core.match.sequencer;

import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.core.app.CommandException;
import com.core.match.msgs.MatchAccountEvent;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchContributorEvent;
import com.core.match.msgs.MatchSecurityEvent;
import com.core.match.msgs.MatchSystemEventEvent;
import com.core.match.msgs.MatchTraderEvent;
import com.core.match.util.MatchPriceUtils;

/**
 * Created by jgreco on 7/27/15.
 */
public class StaticsCommandHandlerTest extends HandlerTestBase {
    private final double previousClosePrice =99.17;
    @Test
    public void testAddSecurity() {
        Mockito.when(Short.valueOf(books.addBook())).thenReturn(Short.valueOf((short) 3));
        staticsCommandHandler.addSecurity("10Y", "10Y_CUSIP", 2.5, 20250515, 20150515, 2, 1000, 2, "TreasuryNote", previousClosePrice);

        MatchSecurityEvent lastMsg = getFirstMessage(MatchSecurityEvent.class);
        Assert.assertEquals(1, lastMsg.getContributorID());
        Assert.assertEquals(1, lastMsg.getContributorSeq());
        Assert.assertEquals(4, lastMsg.getSecurityID());
        Assert.assertEquals("10Y", lastMsg.getNameAsString());
        Assert.assertEquals("10Y_CUSIP", lastMsg.getCUSIPAsString());
        Assert.assertEquals(2.5, lastMsg.getCouponAsDouble(), 0.001);
        Assert.assertEquals(LocalDate.of(2015, 5, 15), lastMsg.getIssueDateAsDate());
        Assert.assertEquals(LocalDate.of(2025, 5, 15), lastMsg.getMaturityDateAsDate());
        Assert.assertEquals(2, lastMsg.getCouponFrequency());
        Assert.assertEquals("0-00+", lastMsg.getTickSizeAs32nd());
        Assert.assertEquals(MatchConstants.SecurityType.TreasuryNote, lastMsg.getType());
    }

    @Test
    public void testUpdateExistingSecurity() {
        staticsCommandHandler.addSecurity("2Y", "2Y_CUSIP", 1.0, 20170515, 20150515, 8, 1000, 2, "TreasuryNote", previousClosePrice);

        MatchSecurityEvent lastMsg = getFirstMessage(MatchSecurityEvent.class);
        Assert.assertEquals(1, lastMsg.getContributorID());
        Assert.assertEquals(1, lastMsg.getContributorSeq());
        Assert.assertEquals(1, lastMsg.getSecurityID());
        Assert.assertEquals("2Y", lastMsg.getNameAsString());
        Assert.assertEquals("2Y_CUSIP", lastMsg.getCUSIPAsString());
        Assert.assertEquals(1.0, lastMsg.getCouponAsDouble(), 0.001);
        Assert.assertEquals(LocalDate.of(2015, 5, 15), lastMsg.getIssueDateAsDate());
        Assert.assertEquals(LocalDate.of(2017, 5, 15), lastMsg.getMaturityDateAsDate());
        Assert.assertEquals(2, lastMsg.getCouponFrequency());
        Assert.assertEquals("0-001", lastMsg.getTickSizeAs32nd());
        Assert.assertEquals(MatchConstants.SecurityType.TreasuryNote, lastMsg.getType());
    }


    @Test
    public void addSpread_any_correctMatchSecurityMessageGenerated() {
        staticsCommandHandler.addSecurity("0123456789EFGH", "2Y_CUSIP", 1.0, 20170515, 20150515, 8, 1000, 2, "TreasuryNote",0);//This is the new security added
        staticsCommandHandler.addSecurity("10Y", "10Y_CUSIP", 1.0, 20170515, 20150515, 8, 1000, 2, "TreasuryNote",0);
        staticsCommandHandler.addSpread("2Y10YTest", "2Y", "10Y", 4, 1, 0.001, 1000, "DiscreteSpread");

        MatchSecurityEvent twoYearSecMessage = getFirstMessage(MatchSecurityEvent.class);
        MatchSecurityEvent tenYearSecMessage = getFirstMessage(MatchSecurityEvent.class);
        MatchSecurityEvent spreadMsg = getFirstMessage(MatchSecurityEvent.class);

        Assert.assertEquals(1, spreadMsg.getContributorID());
        Assert.assertEquals(3, spreadMsg.getContributorSeq());
        Assert.assertEquals(1, spreadMsg.getLeg1ID());
        Assert.assertEquals(5, spreadMsg.getLeg2ID());
        Assert.assertEquals(4, spreadMsg.getLeg1Size());
        Assert.assertEquals(1, spreadMsg.getLeg2Size());
        Assert.assertEquals("2Y10YTest", spreadMsg.getNameAsString());
        Assert.assertEquals(2, spreadMsg.getNumLegs());
        Assert.assertEquals(6, spreadMsg.getSecurityID());
        Assert.assertEquals(1000, spreadMsg.getLotSize(),0);






    }

    @Test(expected=CommandException.class)
    public void testTooLongSecurityName() {
        staticsCommandHandler.addSecurity("0123456789ABCEFGH", "2Y_CUSIP", 1.0, 20170515, 20150515, 8, 1000, 2, "TreasuryNote", previousClosePrice);
    }

    @Test(expected=CommandException.class)
    public void testTooLongCUSIP() {
        staticsCommandHandler.addSecurity("10Y", "0123456789ABCDEFGH", 1.0, 20170515, 20150515, 8, 1000, 2, "TreasuryNote", previousClosePrice);
    }

    @Test(expected=CommandException.class)
    public void testInvalidSecurityType() {
        staticsCommandHandler.addSecurity("10Y", "10Y_CUSIP", 1.0, 20170515, 20150515, 8, 1000, 2, "Note", previousClosePrice);
    }

    @Test(expected=CommandException.class)
    public void testInvalidTickValue() {
        staticsCommandHandler.addSecurity("10Y", "10Y_CUSIP", 1.0, 20170515, 20150515, 7, 1000, 2, "Note", previousClosePrice);
    }

    @Test(expected=CommandException.class)
    public void testInvalidMaturityDate() {
        staticsCommandHandler.addSecurity("2Y", "2Y_CUSIP", 1.0, 20170229, 20150515, 8, 1000, 2, "TreasuryNote", previousClosePrice);
    }

    @Test(expected=CommandException.class)
    public void testInvalidIssueDate() {
        staticsCommandHandler.addSecurity("2Y", "2Y_CUSIP", 1.0, 20170515, 20150000, 8, 1000, 2, "TreasuryNote", previousClosePrice);
    }

    @Test
    public void testAddContributor() {
        staticsCommandHandler.addContributor("MYCONTRIB");

        MatchContributorEvent lastMsg = getFirstMessage(MatchContributorEvent.class);
        Assert.assertEquals(1, lastMsg.getContributorID());
        Assert.assertEquals(1, lastMsg.getContributorSeq());
        Assert.assertEquals(3, lastMsg.getSourceContributorID());
        Assert.assertEquals("MYCONTRIB", lastMsg.getNameAsString());
        Assert.assertTrue(lastMsg.getCancelOnDisconnect());
    }

    @Test
    public void testUpdateContributor() {
        staticsCommandHandler.addContributor("SEQ01");

        MatchContributorEvent lastMsg = getFirstMessage(MatchContributorEvent.class);
        Assert.assertEquals(1, lastMsg.getContributorID());
        Assert.assertEquals(1, lastMsg.getContributorSeq());
        Assert.assertEquals(1, lastMsg.getSourceContributorID());
        Assert.assertEquals("SEQ01", lastMsg.getNameAsString());
        Assert.assertTrue(lastMsg.getCancelOnDisconnect());
    }

    @Test(expected=CommandException.class)
    public void testContributorWithTooLongName() {
        staticsCommandHandler.addContributor("01234567890A");
    }

    @Test
    public void testAddNewTrader() {
        staticsCommandHandler.addTrader("TRADER", "JOHN", 2, 3, 5, 7, 10, 30);

        MatchTraderEvent lastMsg = getFirstMessage(MatchTraderEvent.class);
        Assert.assertEquals("TRADER", lastMsg.getNameAsString());
        Assert.assertEquals(3, lastMsg.getTraderID());
        Assert.assertEquals(1, lastMsg.getAccountID());
        Assert.assertEquals(7*1000, lastMsg.getFatFinger7YLimit());
    }

    @Test
    public void testUpdateTraderAccount() {
        staticsCommandHandler.addTrader("JOHN", "JIM", 2, 3, 5, 17, 10, 30);

        MatchTraderEvent lastMsg = getFirstMessage(MatchTraderEvent.class);
        Assert.assertEquals("JOHN", lastMsg.getNameAsString());
        Assert.assertEquals(1, lastMsg.getTraderID());
        Assert.assertEquals(2, lastMsg.getAccountID());
        Assert.assertEquals(17*1000, lastMsg.getFatFinger7YLimit());
    }

    @Test(expected=CommandException.class)
    public void testAddTraderWithUnknownAccount() {
        staticsCommandHandler.addTrader("TRADER", "UNKNOWN", 2, 3, 5, 17, 10, 30);
    }

    @Test(expected=CommandException.class)
    public void testTraderWithTooLongName() {
        staticsCommandHandler.addTrader("01234567890ABC", "JIM", 2, 3, 5, 17, 10, 30);
    }

    @Test(expected=CommandException.class)
    public void testTraderNullName() {
        staticsCommandHandler.addTrader(null, "JIM", 2, 3, 5, 17, 10, 30);
    }

    @Test(expected=CommandException.class)
    public void testTraderEmptyName() {
        staticsCommandHandler.addTrader("", "JIM",2, 3, 5, 17, 10, 30);
    }

    @Test
    public void testAddNewAccount() {
        staticsCommandHandler.addAccount("FOO", 3,"01234ABC", false, 2.50);

        MatchAccountEvent lastMsg = getFirstMessage(MatchAccountEvent.class);
        Assert.assertEquals("FOO", lastMsg.getNameAsString());
        Assert.assertEquals(3, lastMsg.getAccountID());
        Assert.assertEquals(3, lastMsg.getNetDV01Limit());
        Assert.assertEquals((long) (2.5 * MatchPriceUtils.getPriceMultiplier()), lastMsg.getCommission());
    }

    @Test
    public void testAddNetting() {
        staticsCommandHandler.addAccount("FOO", 3,"01234ABC", true, 3.45);

        MatchAccountEvent lastMsg = getFirstMessage(MatchAccountEvent.class);
        Assert.assertEquals("FOO", lastMsg.getNameAsString());
        Assert.assertEquals(3, lastMsg.getAccountID());
        Assert.assertEquals(3, lastMsg.getNetDV01Limit());
        Assert.assertTrue(lastMsg.getNettingClearing());
        Assert.assertEquals((long) (3.45 * MatchPriceUtils.getPriceMultiplier()), lastMsg.getCommission());
    }

    @Test
    public void testAddNewAccount_hasSSInternalID() {
        staticsCommandHandler.addAccount("FOO", 3,"01234ABC", false, 1.52);

        MatchAccountEvent lastMsg = getFirstMessage(MatchAccountEvent.class);
        Assert.assertEquals("FOO", lastMsg.getNameAsString());
        Assert.assertEquals("01234ABC", lastMsg.getSSGMIDAsString());

        Assert.assertEquals(3, lastMsg.getAccountID());
        Assert.assertEquals(3, lastMsg.getNetDV01Limit());
        Assert.assertEquals((long) (1.52 * MatchPriceUtils.getPriceMultiplier()), lastMsg.getCommission());
    }

    @Test
    public void testUpdateAccount() {
        staticsCommandHandler.addAccount("JIM", 532,"01234ABC", false, 10.0);

        MatchAccountEvent lastMsg = getFirstMessage(MatchAccountEvent.class);
        Assert.assertEquals("JIM", lastMsg.getNameAsString());
        Assert.assertEquals(2, lastMsg.getAccountID());
        Assert.assertEquals(532, lastMsg.getNetDV01Limit());
        Assert.assertEquals((long) (10.0 * MatchPriceUtils.getPriceMultiplier()), lastMsg.getCommission());
    }

    @Test(expected=CommandException.class)
    public void testAddAccountWithTooLongName() {
        staticsCommandHandler.addAccount("01234567890ABC", 1,"01234ABC", false, 7.5);
    }

    @Test
    public void testDisableSecurity() {
        Assert.assertFalse(securities.isDisabled(1));
        staticsCommandHandler.enableSecurity("2Y", false);
        Assert.assertTrue(securities.isDisabled(1));
        staticsCommandHandler.enableSecurity("2Y", true);
        Assert.assertFalse(securities.isDisabled(1));
    }

    @Test
    public void testDisableTrader() {
        Assert.assertFalse(traders.isDisabled(1));
        staticsCommandHandler.enableTrader("JOHN", false);
        Assert.assertTrue(traders.isDisabled(1));
        staticsCommandHandler.enableTrader("JOHN", true);
        Assert.assertFalse(traders.isDisabled(1));
    }

    @Test
    public void testDisableAccount() {
        Assert.assertFalse(accounts.isDisabled(1));
        staticsCommandHandler.enableAccount("JOHN", false);
        Assert.assertTrue(accounts.isDisabled(1));
        staticsCommandHandler.enableAccount("JOHN", true);
        Assert.assertFalse(accounts.isDisabled(1));
    }

    @Test
    public void testSystemEvent() {
        staticsCommandHandler.systemEvent('G');

        MatchSystemEventEvent lastMsg = getFirstMessage(MatchSystemEventEvent.class);
        Assert.assertEquals(1, lastMsg.getContributorID());
        Assert.assertEquals(1, lastMsg.getContributorSeq());
        Assert.assertEquals('G', lastMsg.getEventType());
    }


}
