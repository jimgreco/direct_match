package com.core.match.services.order;

import com.core.connector.Dispatcher;
import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchCancelListener;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchFillListener;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchOrderListener;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.match.msgs.MatchReplaceListener;
import com.core.util.Factory;
import com.core.util.log.Log;
import com.core.util.pool.ObjectPool;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.util.Iterator;
import java.util.List;

/**
 * User: jgreco
 */
public final class OrderService<T extends Order<T>> implements
        MatchOrderListener,
        MatchCancelListener,
        MatchReplaceListener,
        MatchFillListener {
    private final ReplaceUpdates replaceUpdates = new ReplaceUpdates();
    private final ObjectPool<T> pool;
    private final IntObjectHashMap<T> orderIDOrderMap;
    private final List<OrderServiceListener<T>> listeners = new FastList<>();
    private IsInterestedListener isInterestedListener;

    public static <T extends Order<T>> OrderService<T> create(final Class<T> clsReal, Log log, Dispatcher dispatcher) {
        return create(clsReal, log, dispatcher, MatchConstants.MAX_LIVE_ORDERS);
    }

    public static <T extends Order<T>> OrderService<T> create(final Class<T> clsReal, Log log, Dispatcher dispatcher, int size) {
        OrderService<T> orderService = new OrderService<>(() -> {
            try {
                return clsReal.newInstance();
            } catch (Exception ignored) {
                log.error(log.log().add("Creating new instance failed: ").add(ignored));
                return null;
            }
        }, log, size);
        dispatcher.subscribe(orderService);
        return orderService; 
    }

    private OrderService(Factory<T> factory, Log log, int size) {
        this.pool = new ObjectPool<T>(log, "Orders", factory, size);
        this.orderIDOrderMap = new IntObjectHashMap<>(10 * size);
    }

    public void setIsInterestedListener(IsInterestedListener listener) {
        isInterestedListener = listener;
    }

    public void addListener(OrderServiceListener<T> listener) {
        if (isInterestedListener == null) {
            isInterestedListener = listener;
        }

        listeners.add(listener);
    }

    public int[] getOrderIDs() {
        return orderIDOrderMap.keySet().toArray();
    }

    @Override
    public void onMatchOrder(MatchOrderEvent msg) {
        if (isInterestedListener == null || !isInterestedListener.isInterested(msg)) {
            return;
        }

        T order = pool.create();
        order.setID(msg.getOrderID());
        order.setBuy(msg.getBuy());
        order.setPrice(msg.getPrice());
        order.setQty(msg.getQty());
        order.setSecurityID(msg.getSecurityID());
        order.setTraderID(msg.getTraderID());

        orderIDOrderMap.put(order.getID(), order);

        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onOrder(order, msg);
        }
    }

    @Override
    public void onMatchCancel(MatchCancelEvent msg) {
        T order = orderIDOrderMap.remove(msg.getOrderID());
        if (order == null) {
            return;
        }

        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onCancel(order, msg);
        }

        pool.delete(order);
    }

    @Override
    public void onMatchFill(MatchFillEvent msg) {
        T order = orderIDOrderMap.get(msg.getOrderID());

        if (order == null) {
            return;
        }

        order.addCumQty(msg.getQty());

        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onFill(order, msg);
        }

        if (order.isFilled()) {
            orderIDOrderMap.remove(order.getID());
            pool.delete(order);
        }
    }

    @Override
    public void onMatchReplace(MatchReplaceEvent msg) {
        T order = orderIDOrderMap.get(msg.getOrderID());

        if (order == null) {
            return;
        }
        
        int oldQty = order.getQty();
        long oldPrice = order.getPrice();
        int newQty = msg.getQty();
        long newPrice = msg.getPrice();

        order.setQty(newQty);
        order.setPrice(newPrice);

        // old fields
        replaceUpdates.setOldPrice(oldPrice);
        replaceUpdates.setOldQty(oldQty);
        
        // boolean fields
        replaceUpdates.setPriceUpdated(oldPrice != newPrice);
        replaceUpdates.setQtyUpdated(oldQty != newQty);

        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onReplace(order, msg, replaceUpdates);
        }
    }

    public T get(int orderId) {
        return orderIDOrderMap.get(orderId);
    }

    public Iterator<T> iterator() {
        return orderIDOrderMap.iterator();
    }

    public T getFirst() {
        return orderIDOrderMap.getFirst();
    }

    public int size() {
        return orderIDOrderMap.size();
    }
}
