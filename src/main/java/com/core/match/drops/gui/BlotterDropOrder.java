package com.core.match.drops.gui;

import com.core.match.services.order.AbstractOrder;
import com.core.match.services.order.DisplayedOrderAttributes;

/**
 * Created by jgreco on 10/12/15.
 */
public class BlotterDropOrder extends AbstractOrder<BlotterDropOrder> implements DisplayedOrderAttributes {
    private int externalOrderID;
    String clordid;
    long created;
    long updated;
    long notional;
    short contributorID;
    private boolean inBook;

    @Override
    public void clear() {
        super.clear();
        clordid = "";
        externalOrderID = 0;
        created = 0;
        updated = 0;
        notional = 0;
        contributorID = 0;
        inBook = false;
    }

    public short getContributorID() {
        return contributorID;
    }

    public long getCreated() {
        return created;
    }

    public long getUpdated() {
        return updated;
    }

    public long getNotional() {
        return notional;
    }

    @Override
    public int getExternalOrderID() {
        return externalOrderID;
    }

    @Override
    public void setExternalOrderID(int displayedOrderID) {
        this.externalOrderID = displayedOrderID;
    }

    @Override
    public boolean isInBook() {
        return inBook;
    }

    @Override
    public void setInBook(boolean inBook) {
        this.inBook = inBook;
    }

    public String getClOrdID() {
        return clordid;
    }
}
