package com.core.match.drops.gui;

import com.core.match.drops.DropCollection;
import com.core.match.drops.LinearCounter;
import com.core.match.drops.gui.msgs.GUIOrder;
import com.core.match.msgs.MatchConstants;
import com.core.match.services.book.BookPositionService;
import com.core.match.services.book.BookPositionServiceListener;
import com.core.match.services.book.MatchLimitOrder;
import com.core.match.services.security.BaseSecurity;
import com.gs.collections.impl.list.mutable.FastList;


/**
 * Created by jgreco on 2/23/16.
 */
class OrderBookCollection extends DropCollection implements BookPositionServiceListener<MatchLimitOrder> {
    private FastList<GUIOrder[]> bidsBySecurity = new FastList<>();
    private FastList<GUIOrder[]> offersBySecurity = new FastList<>();

    public OrderBookCollection(LinearCounter versionCounter, LinearCounter itemCounter, BookPositionService bookService) {
        super(versionCounter, itemCounter);

        bookService.addListener(this);
    }

    @Override
    public void onBookDefined(BaseSecurity security, int levels) {
        GUIOrder[] bids = new GUIOrder[levels];
        bidsBySecurity.add(bids);
        GUIOrder[] offers = new GUIOrder[levels];
        offersBySecurity.add(offers);

        for (int i=0; i<levels; i++) {
            GUIOrder bid = new GUIOrder(itemCounter.incVersion(), security.getName(), true, i);
            bids[i] = bid;
            addVersion(bid);
        }

        for (int i=0; i<levels; i++) {
            GUIOrder offer = new GUIOrder(itemCounter.incVersion(), security.getName(), false, i);
            offers[i] = offer;
            addVersion(offer);
        }
    }

    @Override
    public void onOrderChange(int qspot, MatchLimitOrder order, long timestamp) {
        FastList<GUIOrder[]> list = order.isBuy() ? bidsBySecurity : offersBySecurity;
        GUIOrder[] orders = list.get(order.getSecurityID() - MatchConstants.STATICS_START_INDEX);
        GUIOrder guiOrder = orders[qspot];

        guiOrder.setTime(timestamp);
        guiOrder.setOrderID(order.getExternalOrderID());
        guiOrder.setQty(order.getRemainingQty());
        guiOrder.setPx(order.getPrice());
        updateVersion(guiOrder);
    }

    @Override
    public void onNoOrder(int qspot, boolean buy, short securityID, long timestamp) {
        FastList<GUIOrder[]> list = buy ? bidsBySecurity : offersBySecurity;
        GUIOrder[] orders = list.get(securityID - MatchConstants.STATICS_START_INDEX);
        GUIOrder guiOrder = orders[qspot];

        guiOrder.setTime(timestamp);
        guiOrder.setOrderID(0);
        guiOrder.setQty(0);
        guiOrder.setPx(0);

        updateVersion(guiOrder);
    }
}
