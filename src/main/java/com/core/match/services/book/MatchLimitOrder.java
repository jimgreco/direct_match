package com.core.match.services.book;

import com.core.match.services.order.AbstractOrder;
import com.core.match.services.order.DisplayedOrderAttributes;
import com.core.services.limit.LimitOrder;

/**
 * Created by jgreco on 12/16/14.
 */
public class MatchLimitOrder extends AbstractOrder<MatchLimitOrder> implements
        LimitOrder<MatchLimitOrder>,
        DisplayedOrderAttributes
{
    private MatchLimitOrder prev;
    private int externalOrderID;
    private boolean inBook;

    @Override
    public void clear() {
        super.clear();
        prev = null;
        externalOrderID = 0;
        inBook = false;
    }

    @Override
    public MatchLimitOrder next() {
        return next;
    }

    @Override
    public MatchLimitOrder prev() {
        return prev;
    }

    @Override
    public void setNext(MatchLimitOrder next) {
        this.next = next;
    }

    @Override
    public void setPrev(MatchLimitOrder prev) {
        this.prev = prev;
    }

    @Override
    public int compare(MatchLimitOrder item) {
        if (getPrice() == item.getPrice()) {
            return 0;
        }
        if (isBuy()) {
            return getPrice() < item.getPrice() ? 1 : -1;
        }
        return getPrice() > item.getPrice() ? 1 : -1;
    }

    @Override
    public int getExternalOrderID() {
        return externalOrderID;
    }

    @Override
    public void setExternalOrderID(int externalOrderID) {
        this.externalOrderID = externalOrderID;
    }

    @Override
    public boolean isInBook() {
        return inBook;
    }

    @Override
    public void setInBook(boolean inBook) {
        this.inBook = inBook;
    }
}
