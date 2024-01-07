package com.core.match.itch.client;

import com.core.match.itch.msgs.ITCHConstants;
import com.core.match.itch.msgs.ITCHOrderCancelEvent;
import com.core.match.itch.msgs.ITCHOrderCancelListener;
import com.core.match.itch.msgs.ITCHOrderEvent;
import com.core.match.itch.msgs.ITCHOrderExecutedEvent;
import com.core.match.itch.msgs.ITCHOrderExecutedListener;
import com.core.match.itch.msgs.ITCHOrderListener;
import com.core.util.log.Log;
import com.core.util.pool.ObjectPool;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.util.List;

/**
 * Created by jgreco on 7/6/15.
 */
public class ITCHClientOrderService implements
        ITCHOrderListener,
        ITCHOrderCancelListener,
        ITCHOrderExecutedListener {
    private final List<ITCHClientOrderServiceListener> listeners = new FastList<>();
    private final IntObjectHashMap<ITCHClientOrder> orders = new IntObjectHashMap<>();
    private final ObjectPool<ITCHClientOrder> pool;

    public ITCHClientOrderService(Log log) {
        this.pool = new ObjectPool<>(log, "ITCHCLIENT", ITCHClientOrder::new, 1000);
    }

    public void addListener(ITCHClientOrderServiceListener listener) {
        listeners.add(listener);
    }

    @Override
    public void onITCHOrder(ITCHOrderEvent msg) {
        ITCHClientOrder order = pool.create();
        order.setBuy(msg.getSide() == ITCHConstants.Side.Buy);
        order.setPrice(msg.getPrice());
        order.setQty(msg.getQty());
        orders.put(msg.getOrderID(), order);

        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onITCHOrder(order, msg);
        }
    }

    @Override
    public void onITCHOrderCancel(ITCHOrderCancelEvent msg) {
        ITCHClientOrder order = orders.get(msg.getOrderID());
        order.removeQty(msg.getQtyCanceled());

        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onITCHOrderCancel(order, msg);
        }

        if (!order.isLive()) {
            orders.remove(msg.getOrderID());
            pool.delete(order);
        }
    }

    @Override
    public void onITCHOrderExecuted(ITCHOrderExecutedEvent msg) {
        ITCHClientOrder order = orders.get(msg.getOrderID());
        order.removeQty(msg.getQty());

        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onITCHOrderExecuted(order, msg);
        }

        if (!order.isLive()) {
            orders.remove(msg.getOrderID());
            pool.delete(order);
        }
    }
}
