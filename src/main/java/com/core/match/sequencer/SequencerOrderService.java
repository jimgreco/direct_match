package com.core.match.sequencer;

import com.core.match.msgs.MatchConstants;
import com.core.util.log.Log;
import com.core.util.pool.ObjectPool;
import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;

/**
 * Created by jgreco on 1/2/15.
 */
class SequencerOrderService {
    private final IntObjectHashMap<SequencerOrder> orders = new IntObjectHashMap<>(10 * MatchConstants.MAX_LIVE_ORDERS);
    private final ObjectPool<SequencerOrder> orderPool;

    public SequencerOrderService(Log log) {
        orderPool = new ObjectPool<>(log, "Orders", SequencerOrder::new, MatchConstants.MAX_LIVE_ORDERS);
    }

    public long size() {
        return orders.size();
    }

    public SequencerOrder get(int orderID) {
        return orders.get(orderID);
    }

    public SequencerOrder create(int orderID) {
        SequencerOrder order = orderPool.create();
        orders.put(orderID, order);
        return order;
    }

    public SequencerOrder remove(int orderID) {
        return orders.remove(orderID);
    }

    public void delete(SequencerOrder order) {
        orderPool.delete(order);
    }
}
