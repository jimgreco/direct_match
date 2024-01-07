package com.core.match.services.order;

/**
 * Created by hli on 9/22/15.
 */
public class BaseOrder extends AbstractOrder<BaseOrder> {
    
	public BaseOrder()
	{
		// empty 
	}
	
	public BaseOrder(int id, boolean buy, long price, int qty, int cumQty, short securityID, short traderID) {
		super(id, buy, price, qty, cumQty, securityID, traderID);
	}
}

