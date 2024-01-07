package com.core.match.services.quote;

/**
 * Created by jgreco on 12/25/14.
 */
public class QuoteUpdatedFlags {
    public static int BEST_BID_PRICE = 0x01;
    public static int BEST_OFFER_PRICE = 0x02;

    private int flags;

    public void clear() {
        flags = 0;
    }

    public void setBestBidPriceUpdated(boolean val) {
        if (val) {
            flags |= BEST_BID_PRICE;
        }
        else {
            flags &= ~BEST_BID_PRICE;
        }
    }

    public void setBestOfferPriceUpdated(boolean val) {
        if (val) {
            flags |= BEST_OFFER_PRICE;
        }
        else {
            flags &= ~BEST_OFFER_PRICE;
        }
    }

    public boolean isBestBidPriceUpdated() {
        return (flags & BEST_BID_PRICE) > 0;
    }

    public boolean isBestOfferPriceUpdated() {
        return (flags & BEST_OFFER_PRICE) > 0;
    }

    public boolean isFlagSet() {
        return flags > 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != QuoteUpdatedFlags.class) {
            return false;
        }

        QuoteUpdatedFlags flags = (QuoteUpdatedFlags) obj;
        return isBestBidPriceUpdated() == flags.isBestBidPriceUpdated() && isBestOfferPriceUpdated() == flags.isBestOfferPriceUpdated();
    }

    @Override
    public int hashCode() {
        return (isBestBidPriceUpdated() ? 2 : 0) + (isBestOfferPriceUpdated() ? 1 : 0);
    }
}
