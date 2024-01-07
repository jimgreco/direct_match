package com.core.match.fix;

import com.core.connector.Dispatcher;
import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.match.services.order.OrderServiceListener;
import com.core.match.services.order.OrderServiceWithRejectsContribIDFiltered;
import com.core.match.services.order.ReplaceUpdates;
import com.core.match.util.MatchPriceUtils;
import com.core.util.log.Log;

/**
 * User: jgreco
 */
public class FixOrderService extends OrderServiceWithRejectsContribIDFiltered<FixOrder> implements OrderServiceListener<FixOrder> {
    public FixOrderService(Log log, Dispatcher dispatcher, int size) {
        super(FixOrder.class, log, dispatcher, size);

        addListener(this);
    }

    @Override
    public void onOrder(FixOrder order, MatchOrderEvent msg) {
    }

    @Override
    public void onCancel(FixOrder order, MatchCancelEvent msg) {

    }

    @Override
    public void onReplace(FixOrder order, MatchReplaceEvent msg, ReplaceUpdates updates) {
        order.setReplaced(true);
    }

    @Override
    public void onFill(FixOrder order, MatchFillEvent msg) {
        order.setNotional(order.getNotional() + msg.getQty() * MatchPriceUtils.toDouble(msg.getPrice()));
    }
}
