package com.core.services.price;

import com.core.util.PriceUtils;
import com.core.util.pool.ObjectPool;

import java.util.Iterator;

/**
 * User: jgreco
 */
public class PriceLevelBook {
    private final int securityID;
    private final String securityName;
    private final ObjectPool<PriceLevel> levelPool;
    private final int impliedDecimals;
    private final int qtyMultiplier;
    PriceLevel bidHead;
    PriceLevel offerHead;

    private long timestampLastLevelAdded;
    private int nBids;
    private int nOffers;

    private final PriceLevelResult result = new PriceLevelResult();
    private final PriceLevel deletedPriceLevel = new PriceLevel();

    public PriceLevelBook(ObjectPool<PriceLevel> levelPool, int securityID, String securityName, int impliedDecimals, int qtyMultiplier) {
        this.levelPool = levelPool;
        this.securityID = securityID;
        this.securityName = securityName;
        this.impliedDecimals = impliedDecimals;
        this.qtyMultiplier = qtyMultiplier;
    }

    public int getNumBids() {
        return nBids;
    }

    public int getNumOffers() {
        return nOffers;
    }

    public PriceLevelResult addLevel(long timestamp, boolean buy, long price, int qty, @SuppressWarnings("unused") int orderId, boolean insidePrice) {
        this.timestampLastLevelAdded = timestamp;
        int position = 0;

        if (qty<=0) return null;
        if (buy) {
            if (bidHead == null || price > bidHead.price) {
                nBids++;
                PriceLevel newLevel = levelPool.create();
                newLevel.price = price;
                newLevel.qty = qty;
                newLevel.next = bidHead;
                newLevel.orders = 1;

                if (insidePrice) {
                    newLevel.insideOrders = 1;
                }

                bidHead = newLevel;

                return result.update(newLevel, true, position);
            }
			PriceLevel bid0 = bidHead;
			while (true) {
			    if (bid0.price == price) {
			        // add to level
			        bid0.qty += qty;
			        bid0.orders++;

                    if (insidePrice) {
                        bid0.insideOrders++;
                    }

			        return result.update(bid0, false, position);
			    }
			    else if (bid0.next == null || bid0.next.price < price) {
                    position++;
			        nBids++;
			        PriceLevel newLevel = levelPool.create();
			        newLevel.price = price;
			        newLevel.qty = qty;
			        newLevel.next = bid0.next;
			        newLevel.orders = 1;

                    if (insidePrice) {
                        newLevel.insideOrders = 1;
                    }

			        bid0.next = newLevel;

			        return result.update(newLevel, true, position);
			    }

			    bid0 = bid0.next;
                position++;
			}
        }
        else {
            if (offerHead == null || price < offerHead.price) {
                nOffers++;
                PriceLevel newLevel = levelPool.create();
                newLevel.price = price;
                newLevel.qty = qty;
                newLevel.next = offerHead;
                newLevel.orders = 1;

                if (insidePrice) {
                    newLevel.insideOrders = 1;
                }

                offerHead = newLevel;

                return result.update(newLevel, true, position);
            }
			PriceLevel offer0 = offerHead;
			while (true) {
			    if (offer0.price == price) {
			        // add to level
			        offer0.qty += qty;
			        offer0.orders++;

                    if (insidePrice) {
                        offer0.insideOrders++;
                    }

			        return result.update(offer0, false, position);
			    }
			    else if (offer0.next == null || offer0.next.price > price) {
			        position++;
                    nOffers++;
			        PriceLevel newLevel = levelPool.create();
			        newLevel.price = price;
			        newLevel.qty = qty;
			        newLevel.next = offer0.next;
			        newLevel.orders = 1;

                    if (insidePrice) {
                        newLevel.insideOrders = 1;
                    }

			        offer0.next = newLevel;

			        return result.update(newLevel, true, position);
			    }

			    offer0 = offer0.next;
                position++;
			}
        }
    }

    public PriceLevelResult removeLevel(long orderTime, boolean buy, long price, int qty, int orderId, boolean insidePrice) {
        this.timestampLastLevelAdded = orderTime;
        int position = 0;

        if (buy) {
            PriceLevel bid0 = bidHead;
            PriceLevel bid1 = null;
            while (bid0 != null) {
                if (bid0.price == price) {
                    // add to level
                    bid0.qty -= qty;
                    if (orderId != 0) {
                        bid0.orders--;

                        if (insidePrice) {
                            bid0.insideOrders--;
                        }
                    }

                    if (bid0.qty <= 0) {
                        if (bid1 == null) {
                            bidHead = bid0.next;
                        }
                        else {
                            bid1.next = bid0.next;
                        }

                        deletedPriceLevel.price = bid0.price;

                        levelPool.delete(bid0);
                        nBids--;

                        return result.update(deletedPriceLevel, true, position);
                    }
					return result.update(bid0, false, position);
                }
                else if (price > bid0.price) {
                    // throw exception?
                    break;
                }

                bid1 = bid0;
                bid0 = bid0.next;
                position++;
            }
        }
        else {
            PriceLevel offer0 = offerHead;
            PriceLevel offer1 = null;
            while (offer0 != null) {
                if (offer0.price == price) {
                    // add to level
                    offer0.qty -= qty;
                    if (orderId != 0) {
                        offer0.orders--;

                        if (insidePrice) {
                            offer0.insideOrders--;
                        }
                    }

                    if (offer0.qty <= 0) {
                        if (offer1 == null) {
                            offerHead = offer0.next;
                        }
                        else {
                            offer1.next = offer0.next;
                        }

                        deletedPriceLevel.price = offer0.price;

                        levelPool.delete(offer0);
                        nOffers--;

                        return result.update(deletedPriceLevel, true, position);
                    }
					return result.update(offer0, false, position);
                }
                else if (price < offer0.price) {
                    // throw exception?
                    break;
                }

                offer1 = offer0;
                offer0 = offer0.next;
                position++;
            }
        }
        return null;
    }

    public long getTimestamp() {
        return timestampLastLevelAdded;
    }

    public int getBestBidQty() {
        return hasBestBid() ? bidHead.qty : 0;
    }

    public int getBestAskQty() {
        return hasBestAsk() ? offerHead.qty : 0;
    }

    public long getBestBidPrice() {
        return hasBestBid() ? bidHead.price : 0;
    }

    public long getBestAskPrice() {
        return hasBestAsk() ? offerHead.price : 0;
    }

    public boolean hasBestBid() {
        return bidHead != null;
    }

    public boolean hasBestAsk() {
        return offerHead != null;
    }

    public String getSecurityName() {
        return securityName;
    }

    public int getSecurityID() {
        return securityID;
    }

    @Override
	public String toString() {
        StringBuilder builder = new StringBuilder();

        PriceLevel bid = bidHead;
        PriceLevel offer = offerHead;

        builder.append('\n');
        builder.append(String.format("%16s", "Bids"));
        builder.append(" x ");
        builder.append("Offers");

        while (bid != null || offer != null) {
            builder.append('\n');

            if (bid != null) {
                builder.append(String.format("%6s", PriceUtils.toQtyRoundLot(bid.getQty(), qtyMultiplier)));
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
                builder.append(String.format("%6s", PriceUtils.toQtyRoundLot(offer.getQty(), qtyMultiplier)));
                offer = offer.next();
            }
            else {
                builder.append(String.format("%14s", ""));
            }
        }

        return builder.toString();
    }

    public static class PriceLevelIterator implements Iterator<PriceLevel> {
        private final IteratorReset resetFunction;
        PriceLevel current;

        public PriceLevelIterator(IteratorReset resetFunction) {
            this.resetFunction = resetFunction;
            this.resetFunction.reset(this);
        }

        public void reset() {
            resetFunction.reset(this);
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public PriceLevel next() {
            PriceLevel level = current;
            current = level.next;
            return level;
        }

        @Override
        public void remove() { }
    }

    public PriceLevelIterator getBidsIterator() {
        return new PriceLevelIterator(priceLevelIterator -> priceLevelIterator.current = bidHead);
    }

    public PriceLevelIterator getOffersIterator() {
        return new PriceLevelIterator(priceLevelIterator -> priceLevelIterator.current = offerHead);
    }

    private interface IteratorReset {
        void reset(PriceLevelIterator priceLevelIterator);
    }

    public PriceLevel getBestBid() {
        return bidHead;
    }

    public PriceLevel getBestOffer() {
        return offerHead;
    }

    public static class PriceLevelResult {
        private PriceLevel level;
        private boolean createOrDestroy;
        private int position;

        public PriceLevelResult update(PriceLevel priceLevel, boolean shouldCreateOrDestroy, int position) {
            this.level = priceLevel;
            this.createOrDestroy = shouldCreateOrDestroy;
            this.position = position;
            return this;
        }

        public PriceLevel getLevel() {
            return level;
        }

        public boolean isCreateOrDestroy() {
            return createOrDestroy;
        }

        public int getPosition() {
            return position;
        }
    }
}
