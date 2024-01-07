package com.core.match.services.quote;

/**
 * Created by jgreco on 12/25/14.
 */
public interface QuoteUpdateListener {
    void onQuoteUpdate(Quote quote, QuoteUpdatedFlags flags);
}
