package com.core.match.fix;

import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchCancelReplaceRejectEvent;
import com.core.match.msgs.MatchClientCancelReplaceRejectEvent;
import com.core.match.msgs.MatchClientOrderRejectEvent;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchOrderRejectEvent;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.match.services.order.OrderServiceListener;
import com.core.match.services.order.OrderServiceRejectListener;
import com.core.match.services.order.ReplaceUpdates;
import com.core.util.BinaryUtils;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import com.gs.collections.impl.set.mutable.UnifiedSet;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

/**
 * User: jgreco
 */
public class FixOrderRepository implements
        OrderServiceRejectListener<FixOrder>,
        OrderServiceListener<FixOrder> {
    private final Set<ByteBuffer> clOrdIDs;
    private final Map<ByteBuffer, FixOrder> activeOrdersByClOrdIDs;

    public FixOrderRepository(int initialSize) {
        clOrdIDs = new UnifiedSet<>(initialSize);
        activeOrdersByClOrdIDs = new UnifiedMap<>();
    }

    @Override
    public void onClientOrderReject(MatchClientOrderRejectEvent msg) {
        updateClOrdID(msg.getClOrdID(), null, false);
    }

    @Override
    public void onCancelReplaceReject(FixOrder order, MatchCancelReplaceRejectEvent msg) {
        updateClOrdID(msg.getClOrdID(), null, false);
    }

    @Override
    public void onClientCancelReplaceReject(FixOrder order, MatchClientCancelReplaceRejectEvent msg) {
        updateClOrdID(msg.getClOrdID(), null, false);
    }

    @Override
    public void onOrderReject(MatchOrderRejectEvent msg) {
        updateClOrdID(msg.getClOrdID(), null, false);
    }

    @Override
    public boolean isInterested(MatchOrderEvent msg) {
        // taken care of by rejects
        return false;
    }

    @Override
    public void onOrder(FixOrder order, MatchOrderEvent msg) {
        updateClOrdID(msg.getClOrdID(), order, false);
    }

    @Override
    public void onCancel(FixOrder order, MatchCancelEvent msg) {
        updateClOrdID(msg.getClOrdID(), order, true);
    }

    @Override
    public void onReplace(FixOrder order, MatchReplaceEvent msg, ReplaceUpdates updates) {
        updateClOrdID(msg.getClOrdID(), order, false);
    }

    @Override
    public void onFill(FixOrder order, MatchFillEvent msg) {
        updateClOrdID(null, order, order.isFilled());
    }

    public boolean seenClOrdID(ByteBuffer buffer) {
        return clOrdIDs.contains(buffer);
    }

    private void updateClOrdID(ByteBuffer msgClOrdId, FixOrder order, boolean dead) {
        if (order != null && order.hasClOrdID() && dead) {
            activeOrdersByClOrdIDs.remove(order.getClOrdID());
        }

        if (msgClOrdId != null && msgClOrdId.hasRemaining()) {
            ByteBuffer clOrdIdWrap = ByteBuffer.wrap(BinaryUtils.toBytes(msgClOrdId));
            clOrdIDs.add(clOrdIdWrap);

            if (order != null) {
                if (!dead) {
                    if (order.hasClOrdID()) {
                        activeOrdersByClOrdIDs.remove(order.getClOrdID());
                    }

                    // add the clOrdId to the active orders
                    order.setClOrdID(clOrdIdWrap);
                    activeOrdersByClOrdIDs.put(clOrdIdWrap, order);
                }
                else {
                    order.setClOrdID(clOrdIdWrap);
                }
            }
        }
    }

    public FixOrder getOrder(ByteBuffer clOrdId) {
        return activeOrdersByClOrdIDs.get(clOrdId);
    }
}
