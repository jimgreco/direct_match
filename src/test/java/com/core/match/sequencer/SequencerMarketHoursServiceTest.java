package com.core.match.sequencer;

import com.core.GenericTest;
import com.core.util.TimeUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by jgreco on 6/27/15.
 */
public class SequencerMarketHoursServiceTest extends GenericTest {
    private SequencerMarketHoursServiceListener listener;
    private SequencerMarketHoursService marketHours;

    @SuppressWarnings("boxing")
	@Before
    public void setup() throws ParseException {
    	SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Date date = isoFormat.parse("2015-05-25T09:29:00");
        timeSource.setTimestamp(date.getTime() * TimeUtils.NANOS_PER_MILLI);

        listener = Mockito.mock(SequencerMarketHoursServiceListener.class);
        Mockito.when(listener.onOpen()).thenReturn(true);
        Mockito.when(listener.onClose()).thenReturn(true);
        marketHours = new SequencerMarketHoursService(timeSource, select, listener, "09:30", "16:00");
    }

    @Test
    public void testForceOpen() {
        marketHours.forceOpen();
        Assert.assertTrue(marketHours.isMarketOpen());

        Mockito.verify(listener).onOpen();
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testForceClose() {
        marketHours.forceClose();
        Assert.assertFalse(marketHours.isMarketOpen());

        Mockito.verify(listener).onClose();
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testTimeOpenMarket() {
        Assert.assertFalse(marketHours.isMarketOpen());
        Mockito.verifyNoMoreInteractions(listener);

        select.runFor(10000);

        Mockito.verifyNoMoreInteractions(listener);
        Assert.assertFalse(marketHours.isMarketOpen());

        select.runFor(55000);

        Assert.assertTrue(marketHours.isMarketOpen());
        Mockito.verify(listener).onOpen();
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testTimeCloseMarket() throws ParseException {
        Assert.assertFalse(marketHours.isMarketOpen());
        Mockito.verifyNoMoreInteractions(listener);

        select.runFor(65000);

        Assert.assertTrue(marketHours.isMarketOpen());
        Mockito.verify(listener).onOpen();
        Mockito.verifyNoMoreInteractions(listener);

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Date date = isoFormat.parse("2015-05-25T15:59:30");        
        
        timeSource.setTimestamp(date.getTime() * TimeUtils.NANOS_PER_MILLI);

        select.runOnce();
        Mockito.verifyNoMoreInteractions(listener);

        select.runFor(35000);

        Assert.assertFalse(marketHours.isMarketOpen());
        Mockito.verify(listener).onClose();
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testSetMarketOpen() {
        Assert.assertFalse(marketHours.isMarketOpen());
        Mockito.verifyNoMoreInteractions(listener);

        marketHours.setMarketOpen("09:00");
        select.runOnce();

        Assert.assertTrue(marketHours.isMarketOpen());
        Mockito.verify(listener).onOpen();
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testSetMarketClose() throws ParseException {
        Assert.assertFalse(marketHours.isMarketOpen());
        Mockito.verifyNoMoreInteractions(listener);

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Date date = isoFormat.parse("2015-05-25T15:00:00");        
        timeSource.setTimestamp(date.getTime() * TimeUtils.NANOS_PER_MILLI);

        select.runOnce();
        Mockito.verify(listener).onOpen();

        marketHours.setMarketClose("14:00");
        select.runOnce();

        Assert.assertFalse(marketHours.isMarketOpen());
        Mockito.verify(listener).onClose();
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testMoveOpen() throws ParseException {
        Assert.assertFalse(marketHours.isMarketOpen());
        Mockito.verifyNoMoreInteractions(listener);

        marketHours.setMarketOpen("10:00");
        select.runFor(65000);

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Date date = isoFormat.parse("2015-05-25T09:59:45");
        
        timeSource.setTimestamp(date.getTime() * TimeUtils.NANOS_PER_MILLI);
        select.runOnce();

        Assert.assertFalse(marketHours.isMarketOpen());

        select.runFor(20000);

        Assert.assertTrue(marketHours.isMarketOpen());
        Mockito.verify(listener).onOpen();
        Mockito.verifyNoMoreInteractions(listener);
    }
}
