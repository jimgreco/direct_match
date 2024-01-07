package com.core.match.monitors.account;

import com.core.match.services.order.AbstractOrder;
import com.core.match.services.risk.RiskOrderAttributes;

/**
 * Created by jgreco on 8/11/15.
 */
public class AccountStatsOrder extends AbstractOrder<AccountStatsOrder> implements RiskOrderAttributes {
    private double filledDV01;
    private double unfilledDV01;
    private short contribID;

    @Override
    public double getFilledNetDV01Contribution() {
        return filledDV01;
    }

    @Override
    public void setFilledNetDV01Contribution(double dv01Contribution) {
        filledDV01 = dv01Contribution;
    }

    @Override
    public double getOpenDV01Contribution() {
        return unfilledDV01;
    }

    @Override
    public void setOpenDV01Contribution(double unfilledDV01) {
        this.unfilledDV01=unfilledDV01;
    }

    public short getContribID() {
        return contribID;
    }

    public void setContribID(short contribID) {
        this.contribID = contribID;
    }

    @Override
    public void clear() {
        super.clear();

        this.contribID = 0;
        this.filledDV01 = 0;
        this.unfilledDV01 = 0;
    }
}

