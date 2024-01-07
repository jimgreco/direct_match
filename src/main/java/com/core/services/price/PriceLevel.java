package com.core.services.price;

import com.core.util.datastructures.contracts.Linkable;
import com.core.util.pool.Poolable;

/**
 * User: jgreco
 */
public class PriceLevel implements Poolable<PriceLevel> , Linkable<PriceLevel>{
    public PriceLevel next;

    public long price;
    public int qty;
    public short orders;
    public short insideOrders;

    @Override
    public void setNext(PriceLevel next) {
        this.next = next;
    }

    @Override
	public String toString()
	{
		return "PriceLevel [price=" + this.price + ", qty=" + this.qty + "]";
	}

	@Override
    public PriceLevel next() {
        return next;
    }

    @Override
    public void clear() {
        next = null;
        price = 0;
        qty = 0;
        orders = 0;
        insideOrders = 0;
    }

    public int getQty() {
        return qty;
    }

    public long getPrice() {
        return price;
    }

    public int getInsideOrders() {
        return insideOrders;
    }



    public int getOrders() {
        return orders;
    }
}
