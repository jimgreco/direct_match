package com.core.match.monitors.orders;

import com.core.match.services.order.AbstractOrder;

/**
 * Created by jgreco on 12/7/15.
 */
public class OrderHistoryMonitorOrder extends AbstractOrder<OrderHistoryMonitorOrder> {
    private int clOrdIDIndex;
    private long created;
    private long updated;
    private boolean ioc;
    private short contributorID;
    private boolean live;
    private long notional;

    @Override
    public void clear() {
        super.clear();
        clOrdIDIndex = 0;
        created = 0;
        updated = 0;
        ioc = false;
        contributorID = 0;
        notional = 0;
        live = false;
    }

    public boolean isIOC() {
        return ioc;
    }

    public void setIOC(boolean ioc) {
        this.ioc = ioc;
    }

    public short getContributorID() {
        return contributorID;
    }

    public void setContributorID(short contributorID) {
        this.contributorID = contributorID;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public int getClOrdIDIndex() {
        return clOrdIDIndex;
    }

    public void setClOrdIDIndex(int clOrdIDIndex) {
        this.clOrdIDIndex = clOrdIDIndex;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public boolean isLive() {
        return live;
    }

    public void addNotional(long notional) {
        this.notional += notional;
    }

    public long getNotional() {
        return notional;
    }
}
