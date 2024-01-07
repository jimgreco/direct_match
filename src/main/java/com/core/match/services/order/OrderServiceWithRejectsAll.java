package com.core.match.services.order;

import com.core.connector.ContributorDefinedListener;
import com.core.connector.Dispatcher;
import com.core.match.msgs.MatchCancelReplaceRejectEvent;
import com.core.match.msgs.MatchCancelReplaceRejectListener;
import com.core.match.msgs.MatchClientCancelReplaceRejectEvent;
import com.core.match.msgs.MatchClientCancelReplaceRejectListener;
import com.core.match.msgs.MatchClientOrderRejectEvent;
import com.core.match.msgs.MatchClientOrderRejectListener;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchOrderRejectEvent;
import com.core.match.msgs.MatchOrderRejectListener;
import com.core.util.log.Log;
import com.gs.collections.impl.list.mutable.FastList;

import java.util.List;

/**
 * Created by jgreco on 1/3/15.
 */
public class OrderServiceWithRejectsAll<T extends Order<T>> implements
        ContributorDefinedListener,
        IsInterestedListener,
        MatchOrderRejectListener,
        MatchClientOrderRejectListener,
        MatchCancelReplaceRejectListener,
        MatchClientCancelReplaceRejectListener {
    private final OrderService<T> orders;
    private final List<OrderServiceRejectListener<T>> listeners = new FastList<>();

    public static <T extends Order<T>> OrderServiceWithRejectsAll<T> create(Class<T> clsReal, Log log, Dispatcher dispatcher) {
        return new OrderServiceWithRejectsAll<>(clsReal, log, dispatcher, MatchConstants.MAX_LIVE_ORDERS);
    }

    public OrderServiceWithRejectsAll(Class<T> clsReal, Log log, Dispatcher dispatcher, int size) {
        this.orders = OrderService.create(clsReal, log, dispatcher, size);
        orders.setIsInterestedListener(this);
        dispatcher.subscribe(this);
    }

    public OrderServiceWithRejectsAll(OrderService<T> orders, Dispatcher dispatcher) {
        this.orders = orders;
        orders.setIsInterestedListener(this);
        dispatcher.subscribe(this);
    }

    public void addRejectListener(OrderServiceRejectListener<T> listener) {
        listeners.add(listener);
    }

    public void addListener(OrderServiceListener<T> listener) {
        orders.addListener(listener);
    }

    @Override
    public void onContributorDefined(short contribID, String name) {
    }

    @Override
    public void onMatchCancelReplaceReject(MatchCancelReplaceRejectEvent msg) {
        T order = orders.get(msg.getOrderID());

        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onCancelReplaceReject(order, msg);
        }
    }

    @Override
    public void onMatchClientCancelReplaceReject(MatchClientCancelReplaceRejectEvent msg) {
        T order = orders.get(msg.getOrderID());

        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onClientCancelReplaceReject(order, msg);
        }
    }

    @Override
    public void onMatchClientOrderReject(MatchClientOrderRejectEvent msg) {
        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onClientOrderReject(msg);
        }
    }

    @Override
    public void onMatchOrderReject(MatchOrderRejectEvent msg) {
        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onOrderReject(msg);
        }
    }

    @Override
    public boolean isInterested(MatchOrderEvent msg) {
        return true;
    }

    public void setIsInterestedListener(IsInterestedListener listener) {
        orders.setIsInterestedListener(listener);
    }

    public long size() {
        return orders.size();
    }

	public OrderService<T> getOrders()
	{
		return orders;
	}
}
