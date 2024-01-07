package com.core.match.services.order;

import com.core.connector.Dispatcher;
import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.util.log.Log;
import com.gs.collections.impl.list.mutable.FastList;

import java.util.List;

/**
 * Created by jgreco on 10/10/15.
 */
public class DisplayedOrderService<T extends Order<T> & DisplayedOrderAttributes> implements
        OrderServiceListener<T> {
    private final List<DisplayedOrderServiceListener<T>> listeners = new FastList<>();
    private final Log log;

    public DisplayedOrderService(Class<T> cls, Log log, Dispatcher dispatcher) {
        this(cls, log, dispatcher, MatchConstants.MAX_LIVE_ORDERS);
    }

    public DisplayedOrderService(Class<T> cls, Log log, Dispatcher dispatcher, int size) {
        OrderService<T> orders = OrderService.create(cls, log, dispatcher, size);
        orders.addListener(this);
        dispatcher.subscribe(this);
        this.log = log;
    }

    public DisplayedOrderService(OrderService<T> orders, Log log) {
        orders.addListener(this);
        this.log = log;
    }

    public void addListener(DisplayedOrderServiceListener<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void onOrder(T order, MatchOrderEvent msg) {
        order.setExternalOrderID(msg.getExternalOrderID());
        order.setInBook(msg.getInBook());

        sendDisplayedOrder(order, msg.getTimestamp());
    }

    @Override
    public void onCancel(T order, MatchCancelEvent msg) {
        // we only need to send a cancel if the order was displayed
        sendDisplayedCancel(order, order.getPrice(), order.getRemainingQty(), msg.getTimestamp(), true);
    }

    @Override
    public void onReplace(T order, MatchReplaceEvent msg, ReplaceUpdates updates) {
        if (order.getExternalOrderID() == msg.getExternalOrderID()) {
            if (order.isInBook()) {
                // replace down
                int canceledQty = updates.getOldQty() - msg.getQty();
                sendDisplayedCancel(order, updates.getOldPrice(), canceledQty, msg.getTimestamp(), false);
            }
        }
        else {
            // price was updated or we increased the max displayed qty or we increased the qty
            // cancel everything shown to the user
            int canceledQty = updates.getOldQty() - order.getCumQty();
            sendDisplayedCancel(order, updates.getOldPrice(), canceledQty, msg.getTimestamp(), true);

            // send out a new order for new displayed qty
            order.setExternalOrderID(msg.getExternalOrderID());
            order.setInBook(msg.getInBook());

            sendDisplayedOrder(order, msg.getTimestamp());
        }
    }

    @Override
    public void onFill(T order, MatchFillEvent msg) {
        boolean wasInBook = order.isInBook();

        if (wasInBook) {
            // this is a passive order
            for (int i=0; i<listeners.size(); i++) {
                listeners.get(i).onDisplayedFill(order, msg.getQty(), msg.getPrice(), msg.getMatchID(), msg.getTimestamp());
            }
        }

        order.setInBook(msg.getInBook());

        if (!wasInBook) {
            // if it wasn't in the book, it now is and we need to send a new order
            sendDisplayedOrder(order, msg.getTimestamp());
        }
    }

    @Override
    public boolean isInterested(MatchOrderEvent msg) {
        return true;
    }

    private void sendDisplayedOrder(T order, long timestamp) {
        if (order.isInBook()) {
            if (order.getRemainingQty() > 0) {
                for (int i = 0; i < listeners.size(); i++) {
                    listeners.get(i).onDisplayedOrder(order, timestamp);
                }
            }
        }
    }

    private void sendDisplayedCancel(T order, long price, int qtyCanceled, long timestamp, boolean deadOrder) {
        if (order.isInBook()) {
            if (qtyCanceled > 0) {
                for (int i = 0; i < listeners.size(); i++) {
                    listeners.get(i).onDisplayedReduced(order, price, qtyCanceled, deadOrder, timestamp);
                }
            }
            else {
                log.error(log.log().add("Displayed order replaced with greater displayed qty. OrderID=").add(order.getID()));
            }
        }
    }
}
