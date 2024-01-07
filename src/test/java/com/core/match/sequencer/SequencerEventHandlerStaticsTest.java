package com.core.match.sequencer;

import com.core.match.msgs.MatchAccountCommand;
import com.core.match.msgs.MatchContributorCommand;
import com.core.match.msgs.MatchSecurityCommand;
import com.core.match.msgs.MatchTraderCommand;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jgreco on 7/26/15.
 */
public class SequencerEventHandlerStaticsTest extends HandlerTestBase {
    @Test
    public void testSecurity() {
        MatchSecurityCommand cmd = messages.getMatchSecurityCommand();
        cmd.setContributorID((short) 1);
        cmd.setContributorSeq(1);
        cmd.setSecurityID((short) 4);
        cmd.setTickSize(1);
        cmd.setName("30Y");

        securities.onMatchSecurity(cmd.toEvent());

        Assert.assertTrue(securities.isValid(4));
        Assert.assertEquals("30Y", securities.getName(4));
    }

    @Test
    public void testFailToAddSecurityForInvalidSecurityID() {
        MatchSecurityCommand cmd = messages.getMatchSecurityCommand();
        cmd.setContributorID((short) 1);
        cmd.setContributorSeq(1);
        cmd.setSecurityID((short) 7);
        cmd.setTickSize(1);
        cmd.setName("10Y");

        securities.onMatchSecurity(cmd.toEvent());

        Assert.assertFalse(securities.isValid((short) 4));
    }

    @Test
    public void testUpdateSecurityTickSize() {
        MatchSecurityCommand cmd = messages.getMatchSecurityCommand();
        cmd.setContributorID((short) 1);
        cmd.setContributorSeq(1);
        cmd.setSecurityID((short) 1);
        cmd.setTickSize(2);
        cmd.setName("2Y");

        securities.onMatchSecurity(cmd.toEvent());

        Assert.assertTrue(securities.isValid(1));
        Assert.assertEquals("2Y", securities.getName(1));
    }

    @Test
    public void testAddAccount() {
        MatchAccountCommand cmd = messages.getMatchAccountCommand();
        cmd.setContributorID((short) 1);
        cmd.setContributorSeq(1);
        cmd.setAccountID((short) 3);
        cmd.setName("FOO");

        accounts.onMatchAccount(cmd.toEvent());

        Assert.assertEquals("FOO", accounts.getName(3));
        Assert.assertTrue(accounts.isValid(3));
    }

    @Test
    public void testFailToAddAccountForInvalidAccountID() {
        MatchAccountCommand cmd = messages.getMatchAccountCommand();
        cmd.setContributorID((short) 1);
        cmd.setContributorSeq(1);
        cmd.setAccountID((short) 4);
        cmd.setName("FOO");

        accounts.onMatchAccount(cmd.toEvent());

        Assert.assertFalse(accounts.isValid(3));
    }

    @Test
    public void testUpdateAccountDoesNothing() {
        MatchAccountCommand cmd = messages.getMatchAccountCommand();
        cmd.setContributorID((short) 1);
        cmd.setContributorSeq(1);
        cmd.setAccountID((short) 2);
        cmd.setName("FOO");

        accounts.onMatchAccount(cmd.toEvent());

        Assert.assertEquals("JIM", accounts.getName(2));
    }

    @Test
    public void testAddContributor() {
        MatchContributorCommand cmd = messages.getMatchContributorCommand();
        cmd.setContributorID((short) 1);
        cmd.setContributorSeq(1);
        cmd.setSourceContributorID((short) 3);
        cmd.setName("FOO");

        contribs.onMatchContributor(cmd.toEvent());

        Assert.assertEquals("FOO", contribs.getName(3));
        Assert.assertTrue(contribs.isValid(3));
    }

    @Test
    public void testFailToAddContributorForInvalidContributorID() {
        MatchContributorCommand cmd = messages.getMatchContributorCommand();
        cmd.setContributorID((short) 1);
        cmd.setContributorSeq(1);
        cmd.setSourceContributorID((short) 4);
        cmd.setName("FOO");

        contribs.onMatchContributor(cmd.toEvent());

        Assert.assertFalse(contribs.isValid(3));
        Assert.assertFalse(contribs.isValid(4));
    }

    @Test
    public void testUpdateContributorDoesNothing() {
        MatchContributorCommand cmd = messages.getMatchContributorCommand();
        cmd.setContributorID((short) 1);
        cmd.setContributorSeq(1);
        cmd.setSourceContributorID((short) 1);
        cmd.setName("FOO");

        contribs.onMatchContributor(cmd.toEvent());

        Assert.assertEquals("SEQ01", contribs.getName(1));
    }

    @Test
    public void testAddTrader() {
        MatchTraderCommand cmd = messages.getMatchTraderCommand();
        cmd.setContributorID((short) 1);
        cmd.setContributorSeq(1);
        cmd.setTraderID((short) 3);
        cmd.setAccountID((short) 2);
        cmd.setName("FOO");

        traders.onMatchTrader(cmd.toEvent());

        Assert.assertEquals("FOO", traders.getName(3));
        Assert.assertTrue(traders.isValid(3));
    }

    @Test
    public void testFailToAddTraderForInvalidTraderID() {
        MatchTraderCommand cmd = messages.getMatchTraderCommand();
        cmd.setContributorID((short) 1);
        cmd.setContributorSeq(1);
        cmd.setTraderID((short) 4);
        cmd.setAccountID((short) 2);
        cmd.setName("FOO");

        traders.onMatchTrader(cmd.toEvent());

        Assert.assertFalse(traders.isValid(3));
        Assert.assertFalse(traders.isValid(4));
    }

    @Test
    public void testUpdateTraderAccountID() {
        MatchTraderCommand cmd = messages.getMatchTraderCommand();
        cmd.setContributorID((short) 1);
        cmd.setContributorSeq(1);
        cmd.setTraderID((short) 1);
        cmd.setAccountID((short) 2);
        cmd.setName("FOO");

        Assert.assertEquals(1, traders.getAccountID(1));

        traders.onMatchTrader(cmd.toEvent());

        Assert.assertEquals("JOHN", traders.getName(1));
        Assert.assertEquals(2, traders.getAccountID(1));
    }
}
