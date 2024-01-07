package com.core.match.msgs;

public interface MatchQuoteCommand extends com.core.match.msgs.MatchCommonCommand {
    void copy(MatchQuoteEvent cmd);
    MatchQuoteEvent toEvent();

    void setSecurityID(short val);

    void setBidPrice(long val);
    void setBidPrice(double val);

    void setOfferPrice(long val);
    void setOfferPrice(double val);

    void setVenueCode(char val);

    void setSourceTimestamp(long val);
}
