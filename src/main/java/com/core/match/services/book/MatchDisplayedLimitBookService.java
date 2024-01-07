package com.core.match.services.book;

import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchSecurityEvent;
import com.core.match.services.order.DisplayedOrderService;
import com.core.match.services.order.DisplayedOrderServiceListener;
import com.core.match.services.security.*;
import com.core.services.limit.LimitBook;
import com.gs.collections.impl.list.mutable.FastList;

import java.util.List;

/**
 * Created by jgreco on 2/22/16.
 */
public class MatchDisplayedLimitBookService implements DisplayedOrderServiceListener<MatchLimitOrder> {
    private final List<LimitBook<MatchLimitOrder>> books = new FastList<>();

    public MatchDisplayedLimitBookService(final DisplayedOrderService<MatchLimitOrder> orderService, final SecurityService<BaseSecurity> securities) {
        securities.addListener(new SecurityServiceListener<BaseSecurity>() {
            @Override
            public void onBond(Bond security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
                if (isNew) {
                    books.add(new LimitBook<>(MatchConstants.IMPLIED_DECIMALS, MatchConstants.QTY_MULTIPLIER));
                }
            }

            @Override
            public void onMultiLegSecurityInstrument(MultiLegSecurity security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
                if (isNew) {
                    books.add(new LimitBook<>(MatchConstants.IMPLIED_DECIMALS, MatchConstants.QTY_MULTIPLIER));
                }
            }
        });
    }

    @Override
    public void onDisplayedOrder(MatchLimitOrder order, long timestamp) {
        add(order);
    }

    @Override
    public void onDisplayedFill(MatchLimitOrder order, int fillQty, long fillPrice, int matchID, long timestamp) {
        remove(order, order.getRemainingQty() <= 0);
    }

    @Override
    public void onDisplayedReduced(MatchLimitOrder order, long oldPrice, int qtyReduced, boolean dead, long timestamp) {
        remove(order, dead);
    }

    private void add(MatchLimitOrder order) {
        LimitBook<MatchLimitOrder> book = books.get(order.getSecurityID());
        book.insertOrder(order);
    }

    private void remove(MatchLimitOrder order, boolean dead) {
        if (dead) {
            LimitBook<MatchLimitOrder> book = books.get(order.getSecurityID());
            book.removeOrder(order);
        }
    }

    public LimitBook<MatchLimitOrder> getBook(Bond security) {
        return getBook(security.getID());
    }

    private LimitBook<MatchLimitOrder> getBook(int securityId) {
        return books.get(securityId - 1);
    }
}
