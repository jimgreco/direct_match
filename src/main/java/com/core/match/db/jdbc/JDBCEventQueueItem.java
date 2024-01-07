package com.core.match.db.jdbc;

import com.core.match.msgs.MatchCommonEvent;
import com.core.match.services.quote.Quote;

public class JDBCEventQueueItem {
    private MatchCommonEvent event;
    private JDBCOrder order;
    private Quote quote;

    public MatchCommonEvent getEvent() {
        return event;
    }

    public JDBCOrder getOrder() {
        return order;
    }

    public Quote getQuote() {
        return quote;
    }

    public void setEvent(MatchCommonEvent event) {
        this.event = event;
    }

    public void setOrder(JDBCOrder order) {
        this.order = order;
    }

    public void setQuote(Quote quote) {
        this.quote = quote;
    }
}