package com.core.match.services.trades;


import com.core.match.services.order.Order;

/**
 * Created by jgreco on 1/28/15.
 */

// TODO: do people ever use this with something *other than* a normal order? 
public interface TradeServiceListener<T extends Order<T>> {
	void onTrade(long timestamp, int matchID, long execPrice, int execQty, T associatedOrder, boolean aggressor);
    void onMatch(long timestamp, int matchID, long execPrice, int execQty, short securityID);
}
