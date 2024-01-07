package com.core.services.bbo;

/**
 * Created by jgreco on 1/26/15.
 */
public class BBOUpdates {
    private boolean bid;
    private boolean qty;
    private boolean price;

    public boolean isBidUpdated() {
        return bid;
    }

    public boolean isQtyUpdated() {
        return qty;
    }

    public boolean isPriceUpdated() {
        return price;
    }

    public boolean isBidPriceUpdated() {
        return bid && price;
    }

    public boolean isBidQtyUpdated() {
        return bid && qty;
    }

    public boolean isOfferPriceUpdated() {
        return !bid && price;
    }

    public boolean isOfferQtyUpdated() {
        return !bid && qty;
    }

    public BBOUpdates update(boolean bidUpdate, boolean qtyUpdates, boolean priceUpdates) {
        this.bid = bidUpdate;
        this.qty = qtyUpdates;
        this.price = priceUpdates;
        return this;
    }
}
