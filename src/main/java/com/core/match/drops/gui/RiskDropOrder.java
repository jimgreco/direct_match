package com.core.match.drops.gui;

import com.core.match.services.order.AbstractOrder;
import com.core.match.services.risk.RiskOrderAttributes;

/**
 * Created by jgreco on 11/10/16.
 */
public class RiskDropOrder extends AbstractOrder<RiskDropOrder> implements RiskOrderAttributes {
    private double filledDV01;
    private double unfilledDV01;

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
}
