package com.core.match.services.quote;

import com.core.match.msgs.MatchConstants;
import com.core.match.services.security.BaseSecurity;
import com.gs.collections.impl.list.mutable.FastList;

import java.util.List;

/**
 * Created by jgreco on 10/8/15.
 */
public class Venue {
    private final String name;
    private int updates;
    private long coreTime;
    private long sourceTime;
    private final List<Quote> quotes = new FastList<>();
    private final QuoteUpdatedFlags flags = new QuoteUpdatedFlags();

    public Venue(String name) {
        this.name = name;
    }

    public int getUpdates() {
        return updates;
    }

    public long getCoreTime() {
        return coreTime;
    }

    public long getSourceTime() {
        return sourceTime;
    }

    public void addSecurity(BaseSecurity security) {
        quotes.add(new Quote(security));
    }

    public QuoteUpdatedFlags update(int securityID, long bid, long offer, long coreTimestamp, long sourceTimestamp) {
        Quote quote = quotes.get(securityID - MatchConstants.STATICS_START_INDEX);

        flags.clear();
        flags.setBestBidPriceUpdated(bid != quote.getBidPrice());
        flags.setBestOfferPriceUpdated(offer != quote.getOfferPrice());

        quote.setBidOffer(bid, offer);
        coreTime = coreTimestamp;
        sourceTime = sourceTimestamp;
        updates++;

        return flags;
    }

    public String getName() {
        return name;
    }

    public Quote get(int securityID) {
        return quotes.get(securityID - MatchConstants.STATICS_START_INDEX);
    }
}
