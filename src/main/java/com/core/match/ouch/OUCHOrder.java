package com.core.match.ouch;

import com.core.match.services.order.AbstractOrder;
import com.core.match.services.order.DisplayedOrderAttributes;
import com.core.match.services.risk.RiskOrderAttributes;

/**
 * Created by jgreco on 8/11/15.
 */
public class OUCHOrder extends AbstractOrder<OUCHOrder> implements RiskOrderAttributes , DisplayedOrderAttributes{
    private long clOrdID;
    private long lastClOrdId;
    private boolean triedDisconnect;
    private double unfilledDV01;
    private double filledDV01;
    private int externalOrderID;
    private boolean inBook;

    public long getClOrdID() {
        return clOrdID;
    }

	public void setClOrdID(long clOrdID) {
        this.clOrdID = clOrdID;
    }

    @Override
    public void clear() {
        setClOrdID(0);
        setFilledNetDV01Contribution(0);
        setTriedDisconnect(false);
        setExternalOrderID(0);
        setLastClOrdId(0);
        setInBook(false);
        super.clear();
    }

    @Override
    public double getFilledNetDV01Contribution()
    {
        return this.filledDV01;
    }

    @Override
    public void setFilledNetDV01Contribution(double dv01Contribution)
    {
        this.filledDV01 = dv01Contribution;
    }

    @Override
    public double getOpenDV01Contribution() {
        return unfilledDV01;
    }

    @Override
    public void setOpenDV01Contribution(double unfilledDV01) {
        this.unfilledDV01=unfilledDV01;
    }


	public boolean hasTriedDisconnect() {
        return triedDisconnect;
    }

	public void setTriedDisconnect(boolean triedDisconnect) {
        this.triedDisconnect = triedDisconnect;
    }

    public long getLastClOrdId()
    {
        return lastClOrdId;
    }

    public void setLastClOrdId(long lastClOrdId)
    {
        this.lastClOrdId = lastClOrdId;
    }
    @Override
    public int getExternalOrderID() {
        return externalOrderID;
    }

    @Override
    public void setExternalOrderID(int displayedOrderID) {
        this.externalOrderID =displayedOrderID;
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
