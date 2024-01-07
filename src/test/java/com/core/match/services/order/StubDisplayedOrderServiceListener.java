package com.core.match.services.order;

import com.gs.collections.impl.list.mutable.FastList;

import java.util.List;

/**
 * Created by jgreco on 10/11/15.
 */
public class StubDisplayedOrderServiceListener implements DisplayedOrderServiceListener<DisplayedOrder> {
    private final List<StubEvent> all = new FastList<>();

    @Override
    public void onDisplayedOrder(DisplayedOrder order, long timestamp) {
        StubDisplayedOrder ord = new StubDisplayedOrder();
        ord.order = order.copy();
        ord.timestamp = timestamp;
        all.add(ord);
    }

    @Override
    public void onDisplayedFill(DisplayedOrder order, int fillQty, long fillPrice, int matchID, long timestamp) {
        StubDisplayedFill fill = new StubDisplayedFill();
        fill.order = order.copy();
        fill.fillQty = fillQty;
        fill.fillPrice = fillPrice;
        fill.matchID = matchID;
        fill.timestamp = timestamp;
        all.add(fill);
    }

    @Override
    public void onDisplayedReduced(DisplayedOrder order, long oldPrice, int qtyReduced, boolean dead, long timestamp) {
        StubDisplayedReduced red = new StubDisplayedReduced();
        red.order = order.copy();
        red.qtyReduced = qtyReduced;
        red.timestamp = timestamp;
        all.add(red);
    }

    StubDisplayedOrder popOrder() {
        return (StubDisplayedOrder)all.remove(0);
    }

    StubDisplayedReduced popReduced() {
        return (StubDisplayedReduced)all.remove(0);
    }

    StubDisplayedFill popFill() {
        return (StubDisplayedFill)all.remove(0);
    }

    public int size() {
        return all.size();
    }

    class StubDisplayedReduced extends StubEvent {
        int qtyReduced;
    }

    class StubDisplayedFill extends StubEvent {
        int fillQty;
        long fillPrice;
        int matchID;
    }

    class StubDisplayedOrder extends StubEvent {
    }

    private class StubEvent {
        DisplayedOrder order;
        long timestamp;
    }
}
