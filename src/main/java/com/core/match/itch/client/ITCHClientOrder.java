package com.core.match.itch.client;

import com.core.services.limit.LimitOrder;
import com.core.util.datastructures.contracts.Linkable;
import com.core.util.pool.Poolable;

/**
 * Created by jgreco on 7/6/15.
 */
public class ITCHClientOrder implements
        LimitOrder<ITCHClientOrder>,
        Poolable<ITCHClientOrder> ,
        Linkable<ITCHClientOrder>{
    private ITCHClientOrder prev;
    private ITCHClientOrder next;
    private boolean buy;
    private int qty;
    private long price;

    @Override
	public boolean isBuy() {
        return buy;
    }

    public void setBuy(boolean buy) {
        this.buy = buy;
    }


    @Override
    public int getRemainingQty() {
        return qty;
    }


    public void setQty(int qty) {
        this.qty = qty;
    }

    public void removeQty(int qty) {
        setQty(this.qty - qty);
    }

    public boolean isLive() {
        return getRemainingQty() > 0;
    }

    @Override
	public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    @Override
    public void clear() {
        setQty(0);
        setPrice(0);
    }

    @Override
    public ITCHClientOrder next() {
        return next;
    }

    @Override
    public ITCHClientOrder prev() {
        return prev;
    }


    @Override
    public void setNext(ITCHClientOrder next) {
        this.next = next;
    }

    @Override
    public void setPrev(ITCHClientOrder prev) {
        this.prev = prev;
    }

    @Override
    public int compare(ITCHClientOrder item) {
        if (getPrice() == item.getPrice()) {
            return 0;
        }
        if (isBuy()) {
            return getPrice() < item.getPrice() ? 1 : -1;
        }
        return getPrice() > item.getPrice() ? 1 : -1;
    }
}
