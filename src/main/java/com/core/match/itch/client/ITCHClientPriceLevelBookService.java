package com.core.match.itch.client;

import com.core.match.itch.msgs.ITCHOrderCancelEvent;
import com.core.match.itch.msgs.ITCHOrderEvent;
import com.core.match.itch.msgs.ITCHOrderExecutedEvent;
import com.core.match.itch.msgs.ITCHSecurityEvent;
import com.core.match.msgs.MatchConstants;
import com.core.services.price.PriceLevelBookService;
import com.core.util.log.Log;

/**
 * Created by jgreco on 7/6/15.
 */
public class ITCHClientPriceLevelBookService extends PriceLevelBookService implements
        ITCHClientSecurityServiceListener,
        ITCHClientOrderServiceListener {
    public ITCHClientPriceLevelBookService(Log log, ITCHClientSecurityService securities, ITCHClientOrderService orders) {
        super(log, MatchConstants.IMPLIED_DECIMALS, 1);

        securities.addListener(this);
        orders.addListener(this);
    }

    @Override
    public void onSecurity(ITCHClientSecurity security, ITCHSecurityEvent msg, boolean isNew) {
        if (isNew) {
            addBook(security.getID(), security.getName());
        }
    }

    @Override
    public void onITCHOrder(ITCHClientOrder order, ITCHOrderEvent msg) {
        addLevel(msg.getSecurityID(), msg.getTimestamp(), order.isBuy(), order.getPrice(), order.getRemainingQty(), msg.getOrderID(), false);
    }

    @Override
    public void onITCHOrderCancel(ITCHClientOrder order, ITCHOrderCancelEvent msg) {
        removeLevel(msg.getSecurityID(), msg.getTimestamp(), order.isBuy(), order.getPrice(), msg.getQtyCanceled(), msg.getOrderID(), false);
    }

    @Override
    public void onITCHOrderExecuted(ITCHClientOrder order, ITCHOrderExecutedEvent msg) {
        removeLevel(msg.getSecurityID(), msg.getTimestamp(), order.isBuy(), order.getPrice(), msg.getQty(), msg.getOrderID(), false);
    }
}
