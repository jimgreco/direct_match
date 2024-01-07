package com.core.match.fix.stp;

import com.core.match.fix.FIXAttributes;
import com.core.match.msgs.MatchConstants;
import com.core.match.services.order.AbstractOrder;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 2/15/16.
 */
public class FIXSTPOrder extends AbstractOrder<FIXSTPOrder> implements FIXAttributes {
    private final ByteBuffer clOrdId = ByteBuffer.allocate(MatchConstants.CLORDID_LENGTH);
    private boolean replaced;
    private double notional;
    private boolean ioc;

    @Override
    public void clear() {
        super.clear();

        clOrdId.clear();
        clOrdId.limit(0);

        replaced = false;
        notional = 0;
        ioc = false;
    }

    @Override
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

    @Override
    public boolean hasClOrdID() {
        return clOrdId.hasRemaining();
    }

    public void setReplaced(boolean replaced) {
        this.replaced = replaced;
    }

    @Override
    public boolean isReplaced() {
        return replaced;
    }

    public void addNotional(double notional) {
        this.notional += notional;
    }

    @Override
    public double getNotional() {
        return notional;
    }

    public void setIOC(boolean ioc) {
        this.ioc = ioc;
    }

    @Override
    public boolean isIOC() {
        return ioc;
    }
}
