package com.core.match.services.trades;

import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.match.services.order.Order;
import com.core.match.services.order.OrderService;
import com.core.match.services.order.OrderServiceListener;
import com.core.match.services.order.ReplaceUpdates;
import com.gs.collections.impl.list.mutable.FastList;

import java.util.List;

/**
 * Created by jgreco on 1/28/15.
 */

public class TradeService<T extends Order<T>> implements
		OrderServiceListener<T> {
    private int lastMatchID;
    private final List<TradeServiceListener<T>> listeners = new FastList<>();

    public TradeService(OrderService<T> orders) {
        orders.addListener(this);
	}

	public void addListener(TradeServiceListener<T> listener) {
		listeners.add(listener);
	}

	@Override
	public boolean isInterested(MatchOrderEvent msg) {
		return true;
	}

	@Override
	public void onOrder(T order, MatchOrderEvent msg) {
    }

	@Override
	public void onCancel(T order, MatchCancelEvent msg) {
	}

	@Override
	public void onReplace(T order, MatchReplaceEvent msg, ReplaceUpdates updates) {
	}

	@Override
	public void onFill(T order, MatchFillEvent msg) {
        boolean isSecondSide = msg.getMatchID() == lastMatchID;
        lastMatchID = msg.getMatchID();

		for (int i = 0; i < listeners.size(); i++) {
			TradeServiceListener<T> listener = listeners.get(i);
			listener.onTrade(msg.getTimestamp(), msg.getMatchID(), msg.getPrice(), msg.getQty(), order,  !msg.getPassive());

            if (isSecondSide) {
                // aggressor is first (sequencer detail)
                listener.onMatch(msg.getTimestamp(), msg.getMatchID(), msg.getPrice(), msg.getQty(), order.getSecurityID());
            }
		}
	}
}
