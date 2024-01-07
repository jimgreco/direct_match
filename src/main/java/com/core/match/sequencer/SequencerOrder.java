package com.core.match.sequencer;

import com.core.services.limit.LimitOrder;
import com.core.util.datastructures.contracts.Linkable;
import com.core.util.pool.Poolable;

/**
 * User: jgreco
 */
final class SequencerOrder implements LimitOrder<SequencerOrder>, Poolable<SequencerOrder>, Linkable<SequencerOrder> {
    private SequencerOrder prev;
    private SequencerOrder next;

    int id;
    int externalOrderID;
    int qty;
    int cumQty;
    long price;
    short securityID;
    boolean buy;
    boolean ioc;
    boolean inBook;

    @Override
    public void clear() {
        prev = null;
        next = null;
        id = 0;
        externalOrderID = 0;
        qty = 0;
        cumQty = 0;
        price = 0;
        securityID = 0;
        buy = false;
        ioc = false;
        inBook = false;
    }

    @Override
    public void setNext(SequencerOrder next) {
        this.next = next;
    }

    @Override
    public void setPrev(SequencerOrder prev) {
        this.prev = prev;
    }

    @Override
    public int compare(SequencerOrder item) {
        if (getPrice() == item.getPrice()) {
            return 0;
        }
        if (isBuy()) {
            return getPrice() < item.getPrice() ? 1 : -1;
        }
        return getPrice() > item.getPrice() ? 1 : -1;
    }

    @Override
    public SequencerOrder next() {
        return next;
    }

    @Override
    public SequencerOrder prev() {
        return prev;
    }

    @Override
    public int getRemainingQty() {
        return getQty() - getCumQty();
    }

    int getID() {
        return id;
    }

    @Override
    public boolean isBuy() {
        return buy;
    }

    short getSecurityID() {
        return securityID;
    }

    public int getQty() {
        return qty;
    }

    int getCumQty() {
        return cumQty;
    }

    @Override
    public long getPrice() {
        return price;
    }

    final boolean isFilled() {
        return cumQty >= qty;
    }

    final int getExternalOrderID() {
        return externalOrderID;
    }

    public boolean isIOC() {
        return ioc;
    }

    public boolean isInBook() {
        return inBook;
    }
}
