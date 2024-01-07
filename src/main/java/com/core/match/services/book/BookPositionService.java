package com.core.match.services.book;

import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchSecurityEvent;
import com.core.match.services.order.DisplayedOrderService;
import com.core.match.services.order.DisplayedOrderServiceListener;
import com.core.match.services.security.*;
import com.core.services.limit.LimitBook;
import com.gs.collections.impl.list.mutable.FastList;

import java.util.List;

import static com.core.match.msgs.MatchConstants.IMPLIED_DECIMALS;
import static com.core.match.msgs.MatchConstants.QTY_MULTIPLIER;

/**
 * Created by jgreco on 2/22/16.
 */
public class BookPositionService implements
        SecurityServiceListener<BaseSecurity>,
        DisplayedOrderServiceListener<MatchLimitOrder>
{
    private final List<BookPositionServiceListener<MatchLimitOrder>> listeners = new FastList<>();
    private final List<LimitBook<MatchLimitOrder>> books = new FastList<>();
    private final int maxIndex;

    public BookPositionService(final DisplayedOrderService<MatchLimitOrder> orderService,
                               final SecurityService<BaseSecurity> securities,
                               int maxIndex) {
        securities.addListener(this);
        orderService.addListener(this);

        this.maxIndex = maxIndex;
    }

    public void addListener(BookPositionServiceListener<MatchLimitOrder> listener) {
        listeners.add(listener);
    }

    @Override
    public void onBond(Bond security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
        if (isNew) {
            books.add(new LimitBook<>(IMPLIED_DECIMALS, QTY_MULTIPLIER));

            for (int i=0; i<listeners.size(); i++) {
                listeners.get(i).onBookDefined(security, maxIndex);
            }
        }
    }

    @Override
    public void onMultiLegSecurityInstrument(MultiLegSecurity security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
        if (isNew) {
            books.add(new LimitBook<>(IMPLIED_DECIMALS, QTY_MULTIPLIER));

            for (int i=0; i<listeners.size(); i++) {
                listeners.get(i).onBookDefined(security, maxIndex);
            }
        }
    }

    @Override
    public void onDisplayedOrder(MatchLimitOrder order, long timestamp) {
        LimitBook<MatchLimitOrder> book = getBook(order.getSecurityID());
        int startPosition = book.insertOrder(order);
        iterateOrderUpdates(order, startPosition, timestamp, order.isBuy(), order.getSecurityID());
    }

    @Override
    public void onDisplayedFill(MatchLimitOrder order, int fillQty, long fillPrice, int matchID, long timestamp) {
        doReduced(order, timestamp, order.getRemainingQty() <= 0);
    }

    @Override
    public void onDisplayedReduced(MatchLimitOrder order, long oldPrice, int qtyReduced, boolean dead, long timestamp) {
        doReduced(order, timestamp, dead);
    }

    private void doReduced(MatchLimitOrder order, long timestamp, boolean dead) {
        LimitBook<MatchLimitOrder> book = getBook(order.getSecurityID());
        int qspot = book.getQueuePosition(order, maxIndex);
        short securityID = order.getSecurityID();
        boolean buy = order.isBuy();

        if (dead) {
            MatchLimitOrder nextOrder = order.next();
            book.removeOrder(order);
            iterateOrderUpdates(nextOrder, qspot, timestamp, buy, securityID);
        }
        else {
            if (qspot < maxIndex) {
                for (int i=0; i<listeners.size(); i++) {
                    listeners.get(i).onOrderChange(qspot, order, timestamp);
                }
            }
        }
    }

    private LimitBook<MatchLimitOrder> getBook(int securityId) {
        return books.get(securityId - MatchConstants.STATICS_START_INDEX);
    }

    private void iterateOrderUpdates(MatchLimitOrder order, int startPosition, long timestamp, boolean buy, short securityID) {
        for (int qspot=startPosition; qspot<maxIndex; qspot++) {
            for (int i=0; i<listeners.size(); i++) {
                if (order != null) {
                    listeners.get(i).onOrderChange(qspot, order, timestamp);
                }
                else {
                    listeners.get(i).onNoOrder(qspot, buy, securityID, timestamp);
                }
            }
            if (order != null) {
                order = order.next();
            }
        }
    }

    public int getLevels() {
        return maxIndex;
    }
}
