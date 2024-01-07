package com.core.match.services.order;

/**
 * Created by jgreco on 12/29/14.
 */
public class ReplaceUpdates {
    private int oldQty;
    private long oldPrice;

    private boolean qtyUpdated;
    private boolean priceUpdated;
    
    public int getOldQty() {
        return oldQty;
    }

    public void setOldQty(int oldQty) {
        this.oldQty = oldQty;
    }

    public long getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(long oldPrice) {
        this.oldPrice = oldPrice;
    }

    public boolean isQtyUpdated() {
        return qtyUpdated;
    }

    public void setQtyUpdated(boolean isOldQtyUpdated) {
        this.qtyUpdated = isOldQtyUpdated;
    }

    public boolean isPriceUpdated() {
        return priceUpdated;
    }

    public void setPriceUpdated(boolean isOldPriceUpdated) {
        this.priceUpdated = isOldPriceUpdated;
    }

}
