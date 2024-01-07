package com.core.services.bbo;

import com.core.util.PriceUtils;

/**
 * Created by jgreco on 1/26/15.
 */
public class BBOBook {
    private final String securityName;
    private final int securityID;
    private final int impliedDecimals;
    private final int qtyMultiplier;

    private long updated;
    private int bidQty;
    private int offerQty;
    private long bidPrice;
    private long offerPrice;

    public BBOBook(int securityID, String securityName, int impliedDecimals, int qtyMultiplier) {
        this.securityID = securityID;
        this.securityName = securityName;
        this.impliedDecimals = impliedDecimals;
        this.qtyMultiplier = qtyMultiplier;
    }

    public int getBidQty() {
        return bidQty;
    }

    public void setBidQty(int bidQty) {
        this.bidQty = bidQty;
    }

    public int getOfferQty() {
        return offerQty;
    }

    public void setOfferQty(int offerQty) {
        this.offerQty = offerQty;
    }

    public long getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(long bidPrice) {
        this.bidPrice = bidPrice;
    }

    public long getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(long offerPrice) {
        this.offerPrice = offerPrice;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public boolean hasBid() {
        return bidQty != 0;
    }

    public boolean hasOffer() {
        return offerQty != 0;
    }

    public boolean isValid() {
        return hasBid() && hasOffer();
    }

    public String getSecurityName() {
        return securityName;
    }

    public int getSecurityID() {
        return securityID;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%6s", Double.valueOf(PriceUtils.toQtyRoundLot(getBidQty(), qtyMultiplier))));
        builder.append(' ');
        builder.append(String.format("%7s", PriceUtils.to32ndPrice(getBidPrice(), impliedDecimals)));
        builder.append(" x ");
        builder.append(String.format("%7s", PriceUtils.to32ndPrice(getOfferPrice(), impliedDecimals)));
        builder.append(' ');
        builder.append(String.format("%6s", Double.valueOf(PriceUtils.toQtyRoundLot(getOfferQty(), qtyMultiplier))));
        return builder.toString();
    }
}
