package com.core.services.price;

/**
 * User: jgreco
 */
public interface PriceLevelBookUpdateListener {
    void onPriceLevelAdded(PriceLevelBook book, boolean buy, PriceLevel level, int position);
    void onPriceLevelRemoved(PriceLevelBook book, boolean buy, PriceLevel level, int position);
    void onPriceLevelChanged(PriceLevelBook book, boolean buy, PriceLevel level, int position);
}
