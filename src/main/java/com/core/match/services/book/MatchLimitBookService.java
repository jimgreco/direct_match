package com.core.match.services.book;

import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.match.msgs.MatchSecurityEvent;
import com.core.match.services.order.OrderService;
import com.core.match.services.order.OrderServiceListener;
import com.core.match.services.order.ReplaceUpdates;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.MultiLegSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.security.SecurityServiceListener;
import com.core.match.services.security.SecurityType;
import com.core.services.limit.LimitBook;
import com.gs.collections.impl.list.mutable.FastList;

import java.util.List;

/**
 * Created by jgreco on 12/16/14.
 */
public class MatchLimitBookService implements OrderServiceListener<MatchLimitOrder> {
    List<LimitBook<MatchLimitOrder>> books = new FastList<>();

    public MatchLimitBookService(OrderService<MatchLimitOrder> orderService, final SecurityService<BaseSecurity> securities) {
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

        orderService.addListener(this);
    }

    @Override
    public boolean isInterested(MatchOrderEvent msg) {
        return true;
    }

    @Override
    public void onOrder(MatchLimitOrder order, MatchOrderEvent msg) {
        LimitBook<MatchLimitOrder> book = getBook(order.getSecurityID());
        book.insertOrder(order);
    }

    @Override
    public void onCancel(MatchLimitOrder order, MatchCancelEvent msg) {
        LimitBook<MatchLimitOrder> book = getBook(order.getSecurityID());
        book.removeOrder(order);
    }

    @Override
    public void onReplace(MatchLimitOrder order, MatchReplaceEvent msg, ReplaceUpdates updates) {
        if (updates.isPriceUpdated() || order.getQty() > updates.getOldQty()) {
            LimitBook<MatchLimitOrder> book = getBook(order.getSecurityID());
            book.removeOrder(order);
            book.insertOrder(order);
        }
    }

    @Override
    public void onFill(MatchLimitOrder order, MatchFillEvent msg) {
        LimitBook<MatchLimitOrder> book = getBook(order.getSecurityID());

        if (order.isFilled()) {
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
