package com.core.services.limit;

import com.core.util.PriceUtils;
import com.core.util.list.IntrusiveDoublyLinkedList;

/**
 * Created by jgreco on 12/16/14.
 */
public class LimitBook<ORDER extends LimitOrder<ORDER>> {
    private final IntrusiveDoublyLinkedList<ORDER> bids = new IntrusiveDoublyLinkedList<>();
    private final IntrusiveDoublyLinkedList<ORDER> offers = new IntrusiveDoublyLinkedList<>();
    private final int impliedDecimals;
    private final int qtyMultiplier;

    public LimitBook(int impliedDecimals, int qtyMultiplier) {
        this.impliedDecimals = impliedDecimals;
        this.qtyMultiplier = qtyMultiplier;
    }

    public int insertOrder(ORDER order) {
        if (order.isBuy()) {
            return bids.insert(order);
        }
        else {
            return offers.insert(order);
        }
    }

    public void removeOrder(ORDER order) {
        if (order.isBuy()) {
            bids.remove(order);
        }
        else {
            offers.remove(order);
        }
    }

    public int getQueuePosition(ORDER order, int maxIndex) {
        if (order.isBuy()) {
            return bids.getQueueSpot(order, maxIndex);
        }
        else {
            return offers.getQueueSpot(order, maxIndex);
        }
    }

    public ORDER getBestBid() {
        return bids.peek();
    }

    public ORDER getBestOffer() {
        return offers.peek();
    }

    public boolean hasBestBid() {
        return getBestBid() != null;
    }

    public boolean hasBestOffer() {
        return getBestOffer() != null;
    }

    public long getBestBidPrice() {
        return hasBestBid() ? getBestBid().getPrice() : 0;
    }

    public long getBestOfferPrice() {
        return hasBestOffer() ? getBestOffer().getPrice() : 0;
    }

    @Override
	public String toString() {
        StringBuilder builder = new StringBuilder();

        LimitOrder<ORDER> bid = getBestBid();
        LimitOrder<ORDER> offer = getBestOffer();

        builder.append('\n');
        builder.append(String.format("%14s", "Bids"));
        builder.append(" x ");
        builder.append("Offers");

        while (bid != null || offer != null) {
            builder.append('\n');

            if (bid != null) {
                builder.append(String.format("%6s", PriceUtils.toQtyRoundLot(bid.getRemainingQty(), qtyMultiplier)));
                builder.append(' ');
                builder.append(String.format("%7s", PriceUtils.to32ndPrice(bid.getPrice(), impliedDecimals)));
                bid = bid.next();
            }
            else {
                builder.append(String.format("%14s", ""));
            }

            builder.append(" x ");

            if (offer != null) {
                builder.append(String.format("%7s", PriceUtils.to32ndPrice(offer.getPrice(), impliedDecimals)));
                builder.append(' ');
                builder.append(String.format("%6s", PriceUtils.toQtyRoundLot(offer.getRemainingQty(), qtyMultiplier)));
                offer = offer.next();
            }
            else {
                builder.append(String.format("%14s", ""));
            }
        }

        return builder.toString();
    }
}

