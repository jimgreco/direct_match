package com.core.match;

import com.core.match.msgs.MatchFillEvent;
import com.core.match.services.order.Order;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 2/5/16.
 */
public class STPHolderFactory<T extends Order<T>> {
    private final STPHolder<T> buy = new STPHolder<>();
    private final STPHolder<T> sell = new STPHolder<>();

    public STPHolder<T> getHolder(T order) {
        if (order.isBuy()) {
            return buy;
        }
        else {
            return sell;
        }
    }

    public STPHolder<T> addFill(T order, ByteBuffer clOrdID, MatchFillEvent msg) {
        STPHolder<T> holder = getHolder(order);
        holder.addFill(order, clOrdID, 0, msg);
        return holder;
    }

    public STPHolder<T> addFill(T order, long clOrdID, MatchFillEvent msg) {
        STPHolder<T> holder = getHolder(order);
        holder.addFill(order, null, clOrdID, msg);
        return holder;
    }
}
