package com.core.match.ouch2.controller;

import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.match.ouch.OUCHOrder;
import com.gs.collections.impl.map.mutable.primitive.LongObjectHashMap;
import com.gs.collections.impl.set.mutable.primitive.LongHashSet;

/**
 * Created by hli on 4/6/16.
 */
public class OUCHOrdersRepository {
    private final LongHashSet clientOrderIDStore = new LongHashSet();
    private final LongObjectHashMap<OUCHOrder> orderMapByClientOrderID = new LongObjectHashMap<>();

    public int getNumberOfLiveOrders() {
        return this.orderMapByClientOrderID.size();
    }

    public void add(OUCHOrder order, MatchOrderEvent msg) {
        long clOrdID = msg.getClOrdID().getLong();
        order.setClOrdID(clOrdID);
        order.setExternalOrderID(msg.getExternalOrderID());
        orderMapByClientOrderID.put(clOrdID, order);
        clientOrderIDStore.add(clOrdID);
    }

    public void removeOrder(OUCHOrder order) {
        orderMapByClientOrderID.remove(order.getClOrdID());

    }

    public void replaceAndRefresh(OUCHOrder order, MatchReplaceEvent msg) {
        long oldClOrdID = order.getClOrdID();
        long newClOrdID = msg.getClOrdID().getLong();
        order.setClOrdID(newClOrdID);
        order.setExternalOrderID(msg.getExternalOrderID());
        //Remove old client order id
        orderMapByClientOrderID.remove(oldClOrdID);
        //Add to New
        clientOrderIDStore.add(newClOrdID);
        orderMapByClientOrderID.put(newClOrdID, order);

    }

    public void removeOrder(long clOrdId) {
        orderMapByClientOrderID.remove(clOrdId);
    }

    public OUCHOrder getByClientOrderID(long clientOrderID) {
        return orderMapByClientOrderID.get(clientOrderID);
    }

    public boolean contains(long clientOrderID) {
        return clientOrderIDStore.contains(clientOrderID);
    }
}
