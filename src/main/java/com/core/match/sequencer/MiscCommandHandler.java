package com.core.match.sequencer;

import com.core.connector.mold.Mold64UDPEventSender;
import com.core.match.msgs.MatchCommonEvent;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchInboundCommand;
import com.core.match.msgs.MatchInboundEvent;
import com.core.match.msgs.MatchInboundListener;
import com.core.match.msgs.MatchMessages;
import com.core.match.msgs.MatchMiscRejectCommand;
import com.core.match.msgs.MatchOutboundCommand;
import com.core.match.msgs.MatchOutboundEvent;
import com.core.match.msgs.MatchOutboundListener;
import com.core.match.msgs.MatchQuoteCommand;
import com.core.match.msgs.MatchQuoteEvent;
import com.core.match.msgs.MatchQuoteListener;
import com.core.sequencer.BaseCommandHandler;
import com.core.util.log.Log;
import com.core.util.time.TimeSource;

/**
 * Created by jgreco on 7/26/15.
 */
class MiscCommandHandler extends BaseCommandHandler implements
        MatchQuoteListener,
        MatchInboundListener,
        MatchOutboundListener
{
    private final SequencerSecurityService securities;
    private final MatchMessages messages;

    public MiscCommandHandler(Log log,
                              TimeSource timeSource,
                                 MatchMessages messages,
                                 Mold64UDPEventSender listener,
                                 SequencerContributorService contributors,
                                 SequencerSecurityService securities) {
        super(log, timeSource, listener, contributors);

        this.securities = securities;
        this.messages = messages;
    }

    @Override
    public void onMatchInbound(MatchInboundEvent msg)
    {
        if (commonEventCheck(msg)) {
            return;
        }

        MatchInboundCommand inbound = messages.getMatchInboundCommand(sender.startMessage());
        inbound.copy(msg);
        sendMessage(inbound, true);

    }

    @Override
    public void onMatchOutbound(MatchOutboundEvent msg)
    {
        if (commonEventCheck(msg)) {
            return;
        }

        MatchOutboundCommand outbound = messages.getMatchOutboundCommand(sender.startMessage());
        outbound.copy(msg);
        sendMessage(outbound, true);
    }

    @Override
    public void onMatchQuote(MatchQuoteEvent msg)
    {
        if (commonEventCheck(msg)) {
            return;
        }

        short securityId = msg.getSecurityID();
        if (!securities.isValid(securityId))
        {
            log.error(log.log().add("Invalid SecurityID: ").add(msg.getSecurityID()));
            sendReject(msg, msg.getMsgType(), MatchConstants.MiscRejectReason.InvalidSecurityID);
            return;
        }

        if (msg.getBidPrice() <= 0)
        {
            log.error(log.log().add("Bid price <= 0: ").add(msg.getBidPrice()));
            sendReject(msg, msg.getMsgType(), MatchConstants.MiscRejectReason.InvalidBidPrice);
            return;
        }

        if (msg.getOfferPrice() <= 0)
        {
            log.error(log.log().add("Offer price <= 0: ").add(msg.getOfferPrice()));
            sendReject(msg, msg.getMsgType(), MatchConstants.MiscRejectReason.InvalidOfferPrice);
            return;
        }

        if (msg.getBidPrice() >= msg.getOfferPrice())
        {
            log.error(log.log().add("Crossed or locked market: ").add(msg.getBidPrice()).add(" >= ").add(msg.getOfferPrice()));
            sendReject(msg, msg.getMsgType(), MatchConstants.MiscRejectReason.LockedMarket);
            return;
        }

        MatchQuoteCommand quote = messages.getMatchQuoteCommand(sender.startMessage());
        quote.copy(msg);
        sendMessage(quote, true);
    }

    public void sendReject(MatchCommonEvent msg, char rejectedMsgType, char reason) {
        MatchMiscRejectCommand reject = messages.getMatchMiscRejectCommand(sender.startMessage());
        reject.setContributorID(msg.getContributorID());
        reject.setContributorSeq(msg.getContributorSeq());
        reject.setRejectedMsgType(rejectedMsgType);
        reject.setRejectReason(reason);
        sendMessage(reject, true);
    }
}
