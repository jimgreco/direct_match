package com.core.match.itch.client;

import com.core.match.itch.msgs.ITCHOrderCancelEvent;
import com.core.match.itch.msgs.ITCHOrderEvent;
import com.core.match.itch.msgs.ITCHOrderExecutedEvent;
import com.core.match.itch.msgs.ITCHSecurityEvent;
import com.core.match.msgs.MatchConstants;
import com.core.services.limit.LimitBook;
import com.gs.collections.impl.list.mutable.FastList;

import java.util.Iterator;
import java.util.List;

/**
 * Created by jgreco on 7/6/15.
 */
public class ITCHClientLimitBookService implements
        ITCHClientSecurityServiceListener,
        ITCHClientOrderServiceListener {
    private final List<LimitBook<ITCHClientOrder>> books = new FastList<>();

    public ITCHClientLimitBookService(ITCHClientSecurityService securityService, ITCHClientOrderService orderService) {
        securityService.addListener(this);
        orderService.addListener(this);
    }

    @Override
    public void onSecurity(ITCHClientSecurity security, ITCHSecurityEvent msg, boolean isNew) {
        if (isNew) {
            LimitBook<ITCHClientOrder> book = new LimitBook<>(MatchConstants.IMPLIED_DECIMALS, 1);
            books.add(book);
        }
    }

    public int size() {
        return books.size();
    }

    public LimitBook<ITCHClientOrder> getBook(int id) {
        return books.get(id - 1);
    }

    public Iterator<LimitBook<ITCHClientOrder>> iterator() {
        return books.iterator();
    }

    @Override
    public void onITCHOrder(ITCHClientOrder order, ITCHOrderEvent msg) {
        LimitBook<ITCHClientOrder> book = getBook(msg.getSecurityID());
        book.insertOrder(order);
    }

    @Override
    public void onITCHOrderCancel(ITCHClientOrder order, ITCHOrderCancelEvent msg) {
        LimitBook<ITCHClientOrder> book = getBook(msg.getSecurityID());
        if (!order.isLive()) {
            book.removeOrder(order);
        }
    }

    @Override
    public void onITCHOrderExecuted(ITCHClientOrder order, ITCHOrderExecutedEvent msg) {
        LimitBook<ITCHClientOrder> book = getBook(msg.getSecurityID());
        if (!order.isLive()) {
            book.removeOrder(order);
        }
    }
}
