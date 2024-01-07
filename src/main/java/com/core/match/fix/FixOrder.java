package com.core.match.fix;

import com.core.match.msgs.MatchConstants;
import com.core.match.services.order.AbstractOrder;
import com.core.match.services.risk.RiskOrderAttributes;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 8/11/15.
 */
public class FixOrder extends AbstractOrder<FixOrder> implements RiskOrderAttributes, FIXAttributes {
    private final ByteBuffer clOrdId = ByteBuffer.allocate(MatchConstants.CLORDID_LENGTH);
    private double notional;
    private boolean replaced;
    private double filledDV01;
    private double openDV01;

    private boolean triedDisconnect;
    private boolean ioc;

    public FixOrder() {
        super();
    }

    public FixOrder(int id, boolean buy, long price, int qty, int cumQty, short securityID, short traderID) {
        super(id, buy, price, qty, cumQty, securityID, traderID);
    }

    @Override
    public void clear() {
        super.clear();

        clOrdId.clear();
        clOrdId.limit(0);
        notional = 0;
        replaced = false;
        filledDV01 = 0;
        openDV01 = 0;

        triedDisconnect = false;
        ioc = false;
    }

    public ByteBuffer getClOrdID() {
        return this.clOrdId.slice();
    }

    public void setClOrdID(ByteBuffer buffer) {
        buffer.mark();
        clOrdId.clear();
        clOrdId.put(buffer);
        clOrdId.flip();
        buffer.reset();
    }

	public boolean hasClOrdID() {
        return clOrdId.hasRemaining();
    }

    @Override
    public double getFilledNetDV01Contribution()
    {
        return filledDV01;
    }

    @Override
    public void setFilledNetDV01Contribution(double dv01Contribution)
    {
        this.filledDV01 = dv01Contribution;
    }

    @Override
    public double getOpenDV01Contribution() {
        return openDV01;
    }

    @Override
    public void setOpenDV01Contribution(double unfilledDV01) {
        this.openDV01 = unfilledDV01;
    }

    public boolean isTriedDisconnect()
    {
        return triedDisconnect;
    }

    public void setTriedDisconnect(boolean triedDisconnect)
    {
        this.triedDisconnect = triedDisconnect;
    }

    public double getNotional() {
        return notional;
    }

    public void setNotional(double notional) {
        this.notional = notional;
    }

    public boolean isReplaced() {
        return replaced;
    }

    public void setReplaced(boolean replaced) {
        this.replaced = replaced;
    }

    @Override
    public boolean isIOC() {
        return ioc;
    }

    public void setIOC(boolean ioc) {
        this.ioc = ioc;
    }
}
