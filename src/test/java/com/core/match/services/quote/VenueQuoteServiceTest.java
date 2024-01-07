package com.core.match.services.quote;

import com.core.match.GenericAppTest;
import com.core.match.services.order.BaseOrder;
import com.core.match.services.security.Bond;
import com.core.match.util.MessageUtils;
import com.core.util.TimeUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalTime;

import static com.core.match.msgs.MatchConstants.Venue.Bloomberg;
import static com.core.match.msgs.MatchConstants.Venue.InteractiveData;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created by jgreco on 10/10/15.
 */
public class VenueQuoteServiceTest extends GenericAppTest<BaseOrder> {
    private final QuoteUpdateListener listener;
    private final Bond fiveYear;
    private final Bond tenYear;
    private final QuoteUpdatedFlags flags;

    public VenueQuoteServiceTest() {
        super(BaseOrder.class);

        listener = Mockito.mock(QuoteUpdateListener.class);
        quotes.addListener(listener);

        sendBond("5Y");
        sendBond("10Y");

        fiveYear = (Bond)securities.get("5Y");
        tenYear = (Bond)securities.get("10Y");

        flags = new QuoteUpdatedFlags();
        flags.setBestBidPriceUpdated(true);
        flags.setBestOfferPriceUpdated(true);
    }

    @Test
    public void testUnknownVenue() {
        sendQuote('F', "10Y", 100.0, 101.0);
    }

    @Test
    public void testVenueDispatch() {
        sendQuote(Bloomberg, "10Y", 100.0, 101.0);
        setUpdate(true, true);

        Quote quote = quotes.getQuote(Bloomberg, tenYear);
        verify(listener).onQuoteUpdate(quote, flags);
    }

    @Test
    public void testUpdates() {
        sendQuote(Bloomberg, "10Y", 100.0, 101.0);
        setUpdate(true, true);

        Quote quote = quotes.getQuote(Bloomberg, tenYear);
        verify(listener).onQuoteUpdate(quote, flags);

        Mockito.reset(listener);
        sendQuote(Bloomberg, "10Y", 100.0, 102.0);
        setUpdate(false, true);
        verify(listener).onQuoteUpdate(quote, flags);

        Mockito.reset(listener);
        sendQuote(Bloomberg, "10Y", 101.0, 102.0);
        setUpdate(true, false);
        verify(listener).onQuoteUpdate(quote, flags);
    }

    @Test
    public void testNoDispatchForSameQuote() {
        sendQuote(Bloomberg, "10Y", 100.0, 101.0);
        setUpdate(true, true);

        Quote quote = quotes.getQuote(Bloomberg, tenYear);
        verify(listener).onQuoteUpdate(quote, flags);
        Assert.assertEquals(1, quotes.getUpdates(Bloomberg));
        Assert.assertEquals(TimeUtils.toNanos(LocalDate.now(), LocalTime.MIDNIGHT, MessageUtils.zoneID()), quotes.getCoreTime(Bloomberg));

        advanceTime(1000);
        Mockito.reset(listener);
        sendQuote(Bloomberg, "10Y", 100.0, 101.0);
        verifyNoMoreInteractions(listener);
        Assert.assertEquals(2, quotes.getUpdates(Bloomberg));
        Assert.assertEquals(TimeUtils.toNanos(LocalDate.now(), LocalTime.MIDNIGHT.plusSeconds(1), MessageUtils.zoneID()), quotes.getCoreTime(Bloomberg));
    }

    @Test
    public void testMultipleVenueDispatch() {
        sendQuote(Bloomberg, "10Y", 100.0, 101.0);
        setUpdate(true, true);
        Quote quote = quotes.getQuote(Bloomberg, tenYear);
        verify(listener).onQuoteUpdate(quote, flags);
        Assert.assertEquals(1, quotes.getUpdates(Bloomberg));
        Assert.assertEquals(TimeUtils.toNanos(LocalDate.now(), LocalTime.MIDNIGHT, MessageUtils.zoneID()), quotes.getCoreTime(Bloomberg));

        advanceTime(1000);
        sendQuote(InteractiveData, "10Y", 100.0, 101.0);
        setUpdate(true, true);
        quote = quotes.getQuote(InteractiveData, tenYear);
        verify(listener).onQuoteUpdate(quote, flags);
        Assert.assertEquals(1, quotes.getUpdates(InteractiveData));
        Assert.assertEquals(TimeUtils.toNanos(LocalDate.now(), LocalTime.MIDNIGHT.plusSeconds(1), MessageUtils.zoneID()), quotes.getCoreTime(InteractiveData));
    }

    @Test
    public void testDifferentSecurities() {
        sendQuote(Bloomberg, "5Y", 100.0, 101.0);
        setUpdate(true, true);
        Quote quote = quotes.getQuote(Bloomberg, fiveYear);
        verify(listener).onQuoteUpdate(quote, flags);

        sendQuote(Bloomberg, "10Y", 100.0, 101.0);
        setUpdate(true, true);
        quote = quotes.getQuote(Bloomberg, tenYear);
        verify(listener).onQuoteUpdate(quote, flags);
    }

    private void setUpdate(boolean bid, boolean offer) {
        flags.clear();
        flags.setBestBidPriceUpdated(bid);
        flags.setBestOfferPriceUpdated(offer);
    }
}
