package com.core.match.services.order;

/**
 * Created by jgreco on 10/11/15.
 */
public interface DisplayedOrderServiceListener<T extends Order<T> & DisplayedOrderAttributes> {
    void onDisplayedOrder(T order, long timestamp);
    void onDisplayedFill(T order, int fillQty, long fillPrice, int matchID, long timestamp);
    void onDisplayedReduced(T order, long oldPrice, int qtyReduced, boolean dead, long timestamp);
}
