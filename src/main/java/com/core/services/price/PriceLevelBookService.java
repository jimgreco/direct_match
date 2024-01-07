package com.core.services.price;

import com.core.util.log.Log;
import com.core.util.pool.ObjectPool;
import com.gs.collections.impl.list.mutable.FastList;

import java.util.List;

/**
 * Created by jgreco on 7/6/15.
 */
public class PriceLevelBookService {
    protected final ObjectPool<PriceLevel> levelPool;
    protected final List<PriceLevelBook> books = new FastList<>();
    private final List<PriceLevelBookUpdateListener> listeners = new FastList<>();
    private final int impliedDecimals;
    private final int qtyMultiplier;

    public PriceLevelBookService(Log log, int impliedDecimals, int qtyMultiplier) {
        this.levelPool = new ObjectPool<>(log, "PriceLevel", PriceLevel::new, 1000);
        this.impliedDecimals = impliedDecimals;
        this.qtyMultiplier = qtyMultiplier;
    }

    public void addListener(PriceLevelBookUpdateListener listener) {
        listeners.add(listener);
    }

    protected void addBook(int id, String name) {
        books.add(new PriceLevelBook(levelPool, id, name, impliedDecimals, qtyMultiplier));
    }

    public PriceLevelBook getBook(int securityId) {
        return books.get(securityId - 1);
    }

    protected void add(PriceLevelBook book, boolean buy, PriceLevelBook.PriceLevelResult priceLevelResult) {
        if (priceLevelResult != null) {
            for (int i=0; i<listeners.size(); i++) {
                PriceLevelBookUpdateListener listener = listeners.get(i);
                if (priceLevelResult.isCreateOrDestroy()) {
                    listener.onPriceLevelAdded(book, buy, priceLevelResult.getLevel(), priceLevelResult.getPosition());
                }
                else {
                    listener.onPriceLevelChanged(book, buy, priceLevelResult.getLevel(), priceLevelResult.getPosition());
                }
            }
        }
    }

    protected void remove(PriceLevelBook book, boolean buy, PriceLevelBook.PriceLevelResult priceLevelResult) {
        if (priceLevelResult != null) {
            for (int i=0; i<listeners.size(); i++) {
                PriceLevelBookUpdateListener listener = listeners.get(i);
                if (priceLevelResult.isCreateOrDestroy()) {
                    listener.onPriceLevelRemoved(book, buy, priceLevelResult.getLevel(), priceLevelResult.getPosition());
                }
                else {
                    listener.onPriceLevelChanged(book, buy, priceLevelResult.getLevel(), priceLevelResult.getPosition());
                }
            }
        }
    }

    protected void addLevel(int securityID, long timestamp, boolean buy, long price, int qty, int id, boolean insidePrice) {
        PriceLevelBook book = getBook(securityID);
        PriceLevelBook.PriceLevelResult result = book.addLevel(timestamp, buy, price, qty, id, insidePrice);
        add(book, buy, result);
    }

    protected void removeLevel(int securityID, long timestamp, boolean buy, long price, int qty, int id, boolean insidePrice) {
        PriceLevelBook book = getBook(securityID);
        PriceLevelBook.PriceLevelResult result = book.removeLevel(timestamp, buy, price, qty, id, insidePrice);
        remove(book, buy, result);
    }
}
