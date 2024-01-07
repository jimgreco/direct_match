package com.core.match.services.quote;

import com.core.match.services.security.BaseSecurity;
import com.core.util.list.IntrusiveDoublyLinkedListItem;

/**
 * Created by jgreco on 12/24/14.
 */
public class Quote implements IntrusiveDoublyLinkedListItem<Quote> {
    private Quote prev;
    private Quote next;

    private final BaseSecurity security;
    private long bidPrice;
    private long offerPrice;
    private char venue;

    public Quote(BaseSecurity security) {
        this.security = security;
    }

    public Quote copy() {
        Quote quote = new Quote(this.security);
        quote.bidPrice = this.bidPrice;
        quote.offerPrice = this.offerPrice;
        quote.venue = this.venue;
        return quote;
    }

    public BaseSecurity getSecurity() {
        return security;
    }

    public long getBidPrice() {
        return bidPrice;
    }

    public long getOfferPrice() {
        return offerPrice;
    }

    public void setBidOffer(long bid, long offer) {
        bidPrice = bid;
        offerPrice = offer;
    }

    public boolean isValid() {
        return bidPrice != 0 && offerPrice != 0 && bidPrice < offerPrice;
    }

    @Override
    public Quote next() {
        return next;
    }

    @Override
    public Quote prev() {
        return prev;
    }

    @Override
    public void setNext(Quote next) {
        this.next = next;
    }

    @Override
    public void setPrev(Quote prev) {
        this.prev = prev;
    }

    @Override
    public int compare(Quote item) {
        return Integer.compare(security.getID(), item.security.getID());
    }

    public void setVenue(char venue) {
        this.venue = venue;
    }

    public char getVenue(){
        return venue;
    }
}
