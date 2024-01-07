package com.core.match.sequencer;

import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchInboundCommand;
import com.core.match.msgs.MatchInboundEvent;
import com.core.match.msgs.MatchMiscRejectEvent;
import com.core.match.msgs.MatchOutboundCommand;
import com.core.match.msgs.MatchOutboundEvent;
import com.core.match.msgs.MatchQuoteCommand;
import com.core.match.msgs.MatchQuoteEvent;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jgreco on 7/26/15.
 */
public class MiscCommandHandlerTest extends HandlerTestBase {
    @Test
    public void testInboundBadSequenceNum()  {
        MatchInboundCommand command = messages.getMatchInboundCommand();
        command.setContributorID((short) 1) ;
        command.setContributorSeq((short) 2);

        miscCommandHandler.onMatchInbound(command.toEvent());
        Assert.assertEquals(0, contribs.getSeqNum(1));
    }

    @Test
    public void testOutboundBadSequenceNum()  {
        MatchOutboundCommand command = messages.getMatchOutboundCommand();
        command.setContributorID((short) 1) ;
        command.setContributorSeq((short) 2);

        miscCommandHandler.onMatchOutbound(command.toEvent());
        Assert.assertEquals(0, contribs.getSeqNum(1));
    }

    @Test
    public void testInbound()  {
        MatchInboundCommand command = messages.getMatchInboundCommand();
        command.setContributorID((short) 1) ;
        command.setContributorSeq((short) 1);

        miscCommandHandler.onMatchInbound(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        MatchInboundEvent lastMsg = getFirstMessage(MatchInboundEvent.class);
        Assert.assertTrue(sender.isFlushed());
    }

    @Test
    public void testOutbound()  {
        MatchOutboundCommand command = messages.getMatchOutboundCommand();
        command.setContributorID((short) 1) ;
        command.setContributorSeq((short) 1);

        miscCommandHandler.onMatchOutbound(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        MatchOutboundEvent lastMsg = getFirstMessage(MatchOutboundEvent.class);
        Assert.assertTrue(sender.isFlushed());
    }

    @Test
    public void testQuote()  {
        MatchQuoteCommand command = messages.getMatchQuoteCommand();
        command.setContributorID((short) 1) ;
        command.setContributorSeq((short) 1);
        command.setSecurityID((short) 1);
        command.setBidPrice(100);
        command.setOfferPrice(101);

        miscCommandHandler.onMatchQuote(command.toEvent());

        Assert.assertEquals(1, contribs.getSeqNum(1));
        MatchQuoteEvent lastMsg = getFirstMessage(MatchQuoteEvent.class);
        Assert.assertTrue(sender.isFlushed());
    }

    @Test
    public void testInvalidSecurity()  {
        MatchQuoteCommand command = messages.getMatchQuoteCommand();
        command.setContributorID((short) 1) ;
        command.setContributorSeq((short) 1);
        command.setSecurityID((short) 7);
        command.setBidPrice(100);
        command.setOfferPrice(101);

        miscCommandHandler.onMatchQuote(command.toEvent());
        Assert.assertEquals(1, contribs.getSeqNum(1));
        MatchMiscRejectEvent lastMsg = getFirstMessage(MatchMiscRejectEvent.class);
        Assert.assertEquals(MatchConstants.MiscRejectReason.InvalidSecurityID, lastMsg.getRejectReason());
        Assert.assertEquals(MatchConstants.Messages.Quote, lastMsg.getRejectedMsgType());
    }

    @Test
    public void testInvalidBid()  {
        MatchQuoteCommand command = messages.getMatchQuoteCommand();
        command.setContributorID((short) 1) ;
        command.setContributorSeq((short) 1);
        command.setSecurityID((short) 1);
        command.setBidPrice(0);
        command.setOfferPrice(101);

        miscCommandHandler.onMatchQuote(command.toEvent());
        Assert.assertEquals(1, contribs.getSeqNum(1));
        MatchMiscRejectEvent lastMsg = getFirstMessage(MatchMiscRejectEvent.class);
        Assert.assertEquals(MatchConstants.MiscRejectReason.InvalidBidPrice, lastMsg.getRejectReason());
        Assert.assertEquals(MatchConstants.Messages.Quote, lastMsg.getRejectedMsgType());
    }

    @Test
    public void testInvalidOffer()  {
        MatchQuoteCommand command = messages.getMatchQuoteCommand();
        command.setContributorID((short) 1) ;
        command.setContributorSeq((short) 1);
        command.setSecurityID((short) 1);
        command.setBidPrice(100);
        command.setOfferPrice(0);

        miscCommandHandler.onMatchQuote(command.toEvent());
        Assert.assertEquals(1, contribs.getSeqNum(1));
        MatchMiscRejectEvent lastMsg = getFirstMessage(MatchMiscRejectEvent.class);
        Assert.assertEquals(MatchConstants.MiscRejectReason.InvalidOfferPrice, lastMsg.getRejectReason());
        Assert.assertEquals(MatchConstants.Messages.Quote, lastMsg.getRejectedMsgType());
    }

    @Test
    public void testLockedBidOffer()  {
        MatchQuoteCommand command = messages.getMatchQuoteCommand();
        command.setContributorID((short) 1) ;
        command.setContributorSeq((short) 1);
        command.setSecurityID((short) 1);
        command.setBidPrice(100);
        command.setOfferPrice(100);

        miscCommandHandler.onMatchQuote(command.toEvent());
        Assert.assertEquals(1, contribs.getSeqNum(1));
        MatchMiscRejectEvent lastMsg = getFirstMessage(MatchMiscRejectEvent.class);
        Assert.assertEquals(MatchConstants.MiscRejectReason.LockedMarket, lastMsg.getRejectReason());
        Assert.assertEquals(MatchConstants.Messages.Quote, lastMsg.getRejectedMsgType());
    }

    @Test
    public void testCrossedBidOffer()  {
        MatchQuoteCommand command = messages.getMatchQuoteCommand();
        command.setContributorID((short) 1) ;
        command.setContributorSeq((short) 1);
        command.setSecurityID((short) 1);
        command.setBidPrice(100);
        command.setOfferPrice(99);

        miscCommandHandler.onMatchQuote(command.toEvent());
        Assert.assertEquals(1, contribs.getSeqNum(1));
        MatchMiscRejectEvent lastMsg = getFirstMessage(MatchMiscRejectEvent.class);
        Assert.assertEquals(MatchConstants.MiscRejectReason.LockedMarket, lastMsg.getRejectReason());
        Assert.assertEquals(MatchConstants.Messages.Quote, lastMsg.getRejectedMsgType());
    }
}
