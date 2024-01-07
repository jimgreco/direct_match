package com.core.match.db.jdbc;

import java.util.concurrent.LinkedBlockingQueue;

import com.core.match.msgs.MatchCommonEvent;
import com.core.match.msgs.MatchConstants;
import com.core.match.services.quote.Quote;
import com.core.match.services.quote.VenueQuoteService;

public class JDBCEventQueue {

    private final LinkedBlockingQueue<JDBCEventQueueItem> eventQueue;
    private final VenueQuoteService venueQuoteService;
    
    public JDBCEventQueue(VenueQuoteService venueQuoteService) {
        this.eventQueue = new LinkedBlockingQueue<>();
        this.venueQuoteService = venueQuoteService;
    }

    public void add(MatchCommonEvent event, JDBCOrder order) {
        JDBCEventQueueItem item = new JDBCEventQueueItem();
        item.setEvent(event);
        item.setOrder(null);
        item.setQuote(null);
        if (order != null) {
            item.setOrder(order.copy());
            // TODO: We need InteractiveData & Bloomberg separately
            Quote quote = venueQuoteService.getQuote(MatchConstants.Venue.InteractiveData, order.getSecurityID());
            if (quote != null) {
                item.setQuote(quote.copy());
            }
        }
        eventQueue.add(item);
    }

    public JDBCEventQueueItem take() throws InterruptedException {
        return eventQueue.take();
    }

    public JDBCEventQueueItem poll() {
        return eventQueue.poll();
    }
}
