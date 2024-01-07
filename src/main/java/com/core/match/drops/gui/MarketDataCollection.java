package com.core.match.drops.gui;

import com.core.match.drops.DropCollection;
import com.core.match.drops.LinearCounter;
import com.core.match.drops.gui.msgs.GUIPrice;
import com.core.match.msgs.MatchSecurityEvent;
import com.core.match.services.book.MatchDisplayedPriceLevelBookService;
import com.core.match.services.security.*;
import com.core.services.price.PriceLevel;
import com.core.services.price.PriceLevelBook;
import com.core.services.price.PriceLevelBookUpdateListener;
import com.gs.collections.impl.list.mutable.FastList;

import java.util.List;

import static com.core.match.msgs.MatchConstants.STATICS_START_INDEX;


/**
 * Created by jgreco on 2/18/16.
 */
class MarketDataCollection extends DropCollection implements
        PriceLevelBookUpdateListener,
        SecurityServiceListener<BaseSecurity> {
    private final int levels;
    private final SecurityService<BaseSecurity> securities;
    private final List<GUIPrice> bids;
    private final List<GUIPrice> offers;
    private final boolean rounded;

    public MarketDataCollection(SecurityService<BaseSecurity> securities, MatchDisplayedPriceLevelBookService book, LinearCounter versionCounter, LinearCounter itemCounter, int levels, boolean rounded) {
        super(versionCounter, itemCounter);

        securities.addListener(this);

        book.addListener(this);
        this.securities = securities;
        this.levels = levels;
        this.rounded = rounded;
        this.bids = new FastList<>();
        this.offers = new FastList<>();
    }

    public void updateQuote(BaseSecurity security, long timestamp, boolean bid, int position, int qty, long price, int orders, int internalOrders) {
        if (position >= levels) {
            return;
        }

        int index = levels * (security.getID() - STATICS_START_INDEX) + position;
        List<GUIPrice> collection = bid ? bids : offers;
        GUIPrice bidOffer = collection.get(index);
        bidOffer.setTime(timestamp);
        bidOffer.setQty(qty);
        bidOffer.setPx(price);
        bidOffer.setOrd(orders);
        bidOffer.setIord(internalOrders);
        updateVersion(bidOffer);
    }

    @Override
    public void onBond(Bond security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
        if (isNew) {
            for (int i=0; i<levels; i++) {
                GUIPrice bid;
                if (rounded) {
                    bid = new GUIPrice32(itemCounter.incVersion(), security.getName(), true, i);
                }
                else {
                    bid = new GUIPrice(itemCounter.incVersion(), security.getName(), true, i);
                }
                addVersion(bid);
                bids.add(bid);

                GUIPrice offer;
                if (rounded) {
                    offer = new GUIPrice32(itemCounter.incVersion(), security.getName(), false, i);
                }
                else {
                    offer = new GUIPrice(itemCounter.incVersion(), security.getName(), false, i);
                }
                addVersion(offer);
                offers.add(offer);
            }
        }
    }

    @Override
    public void onMultiLegSecurityInstrument(MultiLegSecurity security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
        if (isNew) {
            for (int i=0; i<levels; i++) {
                GUIPrice bid;
                if (rounded) {
                    bid = new GUIPrice32(itemCounter.incVersion(), security.getName(), true, i);
                }
                else {
                    bid = new GUIPrice(itemCounter.incVersion(), security.getName(), true, i);
                }
                addVersion(bid);
                bids.add(bid);

                GUIPrice offer;
                if (rounded) {
                    offer = new GUIPrice32(itemCounter.incVersion(), security.getName(), false, i);
                }
                else {
                    offer = new GUIPrice(itemCounter.incVersion(), security.getName(), false, i);
                }
                addVersion(offer);
                offers.add(offer);
            }
        }
    }

    @Override
    public void onPriceLevelAdded(PriceLevelBook book, boolean buy, PriceLevel level, int position) {
        if (position >= levels) {
            // only care about the first N levels
            return;
        }

        BaseSecurity security = securities.get(book.getSecurityID());
        PriceLevel currentLevel = level;
        for (int i=position; i<levels; i++) {
            if (currentLevel != null) {
                updateQuote(security, book.getTimestamp(), buy, i, currentLevel.getQty(), currentLevel.getPrice(), currentLevel.getOrders(), currentLevel.getInsideOrders());
                currentLevel = currentLevel.next();
            }
            else {
                updateQuote(security, book.getTimestamp(), buy, i, 0, 0, 0, 0);
            }
        }
    }

    @Override
    public void onPriceLevelRemoved(PriceLevelBook book, boolean buy, PriceLevel level, int position) {
        if (position >= levels) {
            // only care about the first N levels
            return;
        }

        // go to this position
        PriceLevel currentLevel = buy ? book.getBestBid() : book.getBestOffer();
        for (int i=0; i<position; i++) {
            if (currentLevel != null) {
                currentLevel = currentLevel.next();
            }
        }

        BaseSecurity security = securities.get(book.getSecurityID());

        for (int i=position; i<levels; i++) {
            if (currentLevel != null) {
                updateQuote(security, book.getTimestamp(), buy, i, currentLevel.getQty(), currentLevel.getPrice(), currentLevel.getOrders(), currentLevel.getInsideOrders());
                currentLevel = currentLevel.next();
            }
            else {
                updateQuote(security, book.getTimestamp(), buy, i, 0, 0, 0, 0);
            }
        }
    }

    @Override
    public void onPriceLevelChanged(PriceLevelBook book, boolean buy, PriceLevel level, int position) {
        if (position >= levels) {
            // only care about the first N levels
            return;
        }

        BaseSecurity security = securities.get(book.getSecurityID());
        updateQuote(security, book.getTimestamp(), buy, position, level.getQty(), level.getPrice(), level.getOrders(), level.getInsideOrders());
    }
}
