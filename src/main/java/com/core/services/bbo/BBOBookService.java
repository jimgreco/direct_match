package com.core.services.bbo;

import com.core.match.msgs.MatchConstants;
import com.core.services.price.PriceLevel;
import com.core.services.price.PriceLevelBook;
import com.core.services.price.PriceLevelBookService;
import com.core.services.price.PriceLevelBookUpdateListener;
import com.gs.collections.impl.list.mutable.FastList;

import java.util.Iterator;
import java.util.List;

/**
 * Created by jgreco on 7/6/15.
 */
public class BBOBookService implements PriceLevelBookUpdateListener {
    private final BBOUpdates updates = new BBOUpdates();
    private final List<BBOBook> books = new FastList<>();
    private final List<BBOUpdateListener> listeners = new FastList<>();
    private final int impliedDecimals;
    private final int qtyMultiplier;
    private long lastUpdateTime;
    private int numUpdates;

    public BBOBookService(PriceLevelBookService bookService, int impliedDecimals, int qtyMultiplier) {
        bookService.addListener(this);
        this.impliedDecimals = impliedDecimals;
        this.qtyMultiplier = qtyMultiplier;
    }

    protected void addBook(short id, String name) {
        assert id == books.size() + MatchConstants.STATICS_START_INDEX;
        books.add(new BBOBook(id, name, impliedDecimals, qtyMultiplier));
    }

    public void addListener(BBOUpdateListener listener) {
        listeners.add(listener);
    }

    public BBOBook get(int id) {
        return books.get(id - MatchConstants.STATICS_START_INDEX);
    }

    public Iterator<BBOBook> iterator() {
        return books.iterator();
    }

    @Override
    public void onPriceLevelAdded(PriceLevelBook book, boolean buy, PriceLevel level, int position) {
        lastUpdateTime = book.getTimestamp();
        numUpdates++;

        BBOBook bboBook = getBook(book);
        if (buy) {
            if (!bboBook.hasBid() || level.price > bboBook.getBidPrice()) {
                bboBook.setBidPrice(level.price);
                bboBook.setBidQty(level.qty);
                bboBook.setUpdated(book.getTimestamp());
                update(bboBook, updates.update(true, true, true));
            }
            else if (level.price == bboBook.getBidPrice()) {
                bboBook.setBidPrice(level.qty);
                bboBook.setUpdated(book.getTimestamp());
                update(bboBook, updates.update(true, true, false));
            }
        }
        else {
            if (!bboBook.hasOffer() || level.price < bboBook.getOfferPrice()) {
                bboBook.setOfferPrice(level.price);
                bboBook.setOfferQty(level.qty);
                bboBook.setUpdated(book.getTimestamp());
                update(bboBook, updates.update(false, true, true));
            }
            else if (level.price == bboBook.getOfferPrice()) {
                bboBook.setBidPrice(level.qty);
                bboBook.setUpdated(book.getTimestamp());
                update(bboBook, updates.update(false, true, false));
            }
        }
    }

    @Override
    public void onPriceLevelRemoved(PriceLevelBook book, boolean buy, PriceLevel level, int position) {
        lastUpdateTime = book.getTimestamp();
        numUpdates++;

        BBOBook bboBook = getBook(book);
        if (buy) {
            if (level.price == bboBook.getBidPrice()) {
                bboBook.setBidQty(book.getBestBidQty());
                bboBook.setBidPrice(book.getBestBidPrice());
                bboBook.setUpdated(book.getTimestamp());
                update(bboBook, updates.update(true, true, true));
            }
        }
        else {
            if (level.price == bboBook.getOfferPrice()) {
                bboBook.setOfferQty(book.getBestAskQty());
                bboBook.setOfferPrice(book.getBestAskPrice());
                bboBook.setUpdated(book.getTimestamp());
                update(bboBook, updates.update(false, true, true));
            }
        }
    }

    @Override
    public void onPriceLevelChanged(PriceLevelBook book, boolean buy, PriceLevel level, int position) {
        lastUpdateTime = book.getTimestamp();
        numUpdates++;

        BBOBook bboBook = getBook(book);
        if (buy) {
            if (level.price == bboBook.getBidPrice()) {
                bboBook.setBidQty(book.getBestBidQty());
                bboBook.setUpdated(book.getTimestamp());
                update(bboBook, updates.update(true, true, false));
            }
        }
        else {
            if (level.price == bboBook.getOfferPrice()) {
                bboBook.setOfferQty(book.getBestAskQty());
                bboBook.setUpdated(book.getTimestamp());
                update(bboBook, updates.update(false, true, false));
            }
        }
    }

    private BBOBook getBook(PriceLevelBook book) {
        return books.get(book.getSecurityID() - 1);
    }

    private void update(BBOBook bboBook, BBOUpdates bboUpdates) {
        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onBBOChange(bboBook, bboUpdates);
        }
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public int getNumUpdates() {
        return numUpdates;
    }
}
