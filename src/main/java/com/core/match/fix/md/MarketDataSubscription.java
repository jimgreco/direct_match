package com.core.match.fix.md;

import com.core.services.price.PriceLevel;
import com.core.services.price.PriceLevelBook;
import com.core.util.datastructures.ArraySliceIterator;
import com.core.util.datastructures.CircularFifoQueue;
import com.core.util.datastructures.ClassFactory;
import com.core.util.datastructures.contracts.AbstractLinkable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jgreco on 11/24/14.
 */
public class MarketDataSubscription extends AbstractLinkable<MarketDataSubscription> {
    private final PriceLevel[] lastBids; 
    private final PriceLevel[] lastOffers; 
    private final CircularFifoQueue<PriceLevel> lastTrades; 
    
    private final String reqId;
    private final PriceLevelBook book;
    private final ArraySliceIterator<PriceLevel> lastBidsPriceLevelIterator;
    private final ArraySliceIterator<PriceLevel> lastOffersPriceLevelIterator;
    private final PriceLevelBook.PriceLevelIterator bidsIterator;
    private final PriceLevelBook.PriceLevelIterator offersIterator;
    private final MarketDataUpdate[] updates; 
    private int updatesSinceLastTimer = 0;
    private int updateLength;
	private final int marketDataLevels;
    private boolean sendMarketDataAsSnapshotMessage = true;
    private final List<MarketDataUpdate> updatesForSorting;

    public MarketDataSubscription(String reqId, PriceLevelBook book, int marketDataLevels) {
        this.reqId = reqId;
        this.book = book;
		this.marketDataLevels = marketDataLevels;
		this.lastBids = new PriceLevel[this.marketDataLevels];
		this.lastOffers = new PriceLevel[this.marketDataLevels];
        this.updatesForSorting = new ArrayList<MarketDataUpdate>();
		this.updates = new MarketDataUpdate[3 * 2 * marketDataLevels];
		this.lastTrades = new CircularFifoQueue<PriceLevel>(marketDataLevels, new ClassFactory<PriceLevel>(PriceLevel.class) {
	        @Override
	        public PriceLevel newInstance() {
	            return new PriceLevel();
	        }
	    });


        for (int i=0; i<lastBids.length; i++) {
            lastBids[i] = new PriceLevel();
            lastOffers[i] = new PriceLevel();
        }

        lastBidsPriceLevelIterator = new ArraySliceIterator<>(lastBids);
        lastOffersPriceLevelIterator = new ArraySliceIterator<>(lastOffers);

        bidsIterator = this.book.getBidsIterator();
        offersIterator = this.book.getOffersIterator();

        for (int i=0; i<updates.length; i++) {
            updates[i] = new MarketDataUpdate();
        }
    }

    public String getReqId() {
        return reqId;
    }

    public PriceLevelBook getBook() {
        return book;
    }

    public ArraySliceIterator<PriceLevel> getLastBidsIterator() {
        return lastBidsPriceLevelIterator;
    }

    public ArraySliceIterator<PriceLevel> getLastOffersIterator() {
        return lastOffersPriceLevelIterator;
    }

    public String getSecurityName() {
        return book.getSecurityName();
    }

    public void reset() {
        updateLength = 0;
    }

    public void addUpdate(char updateType, PriceLevel level, char entryType) {
        MarketDataUpdate update = updates[updateLength++];
        update.updateType = updateType;
        update.entryType = entryType;
        update.level = level;
    }

    public PriceLevelBook.PriceLevelIterator getBidsIterator() {
        bidsIterator.reset();
        return bidsIterator;
    }

    public PriceLevelBook.PriceLevelIterator getOffersIterator() {
        offersIterator.reset();
        return offersIterator;
    }

    public int getUpdateLength() {
        return updateLength;
    }

    public MarketDataUpdate getUpdate(int i) {
        return updates[i];
    }

    public void copyCurrentToLast() {
        int n = 0;

        PriceLevelBook.PriceLevelIterator iterator = getBidsIterator();
        while (iterator.hasNext() && n<lastBids.length) {
            PriceLevel next = iterator.next();
            lastBids[n].price = next.price;
            lastBids[n].qty = next.qty;
            lastBids[n].orders = next.orders;
            n++;
        }

        lastBidsPriceLevelIterator.setBoundary(n);

        n = 0;
        iterator = getOffersIterator();
        while (iterator.hasNext() && n<lastOffers.length) {
            PriceLevel next = iterator.next();
            lastOffers[n].price = next.price;
            lastOffers[n].qty = next.qty;
            lastOffers[n].orders = next.orders;
            n++;
        }

        lastOffersPriceLevelIterator.setBoundary(n);
    }

    public void addTrade(int execQty, long execPrice) {
        PriceLevel add = this.lastTrades.add();
        add.qty = execQty;
        add.price = execPrice;
    }

    public Iterator<PriceLevel> getLastTradesIterator() {
        return this.lastTrades.iterator();
    }

    
    public int incrementAndGetUpdatesSinceLastTimer() {
        return ++this.updatesSinceLastTimer;
    }

    public void resetUpdatesSinceLastTimer(){
        this.updatesSinceLastTimer = (0);
    }

    public int getUpdatesSinceLastTimer()
	{
		return updatesSinceLastTimer;
	}

	public static class MarketDataUpdate {
        public PriceLevel level;
        public char updateType;
        public char entryType;
    }

    public boolean getSendMarketDataAsSnapshotMessage(){
        return sendMarketDataAsSnapshotMessage;
    }

    public void setSendMarketDataAsSnapshotMessage(boolean sendMarketDataAsSnapshotMessage){
        this.sendMarketDataAsSnapshotMessage = sendMarketDataAsSnapshotMessage;
    }

    public List<MarketDataUpdate> getEntriesInOrder(char entryType, boolean ascending) {

        updatesForSorting.clear();

        for (int i = 0; i < updateLength; i++) {
            if (updates[i].entryType == entryType) {
                updatesForSorting.add(updates[i]);
            }
        }

        if(ascending) {
            Collections.sort(updatesForSorting, (left, right) -> left.level.price > right.level.price ? +1 : left.level.price < right.level.price ? -1 : 0);
        }else{
            Collections.sort(updatesForSorting, (left, right) -> left.level.price < right.level.price ? +1 : left.level.price > right.level.price ? -1 : 0);
        }

        return updatesForSorting;
    }
}
