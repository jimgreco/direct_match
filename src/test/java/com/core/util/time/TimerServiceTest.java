package com.core.util.time;

import com.core.util.log.SystemOutLog;
import org.junit.Test;
import org.mockito.internal.verification.AtMost;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * User: jgreco
 */
public class TimerServiceTest {
    @SuppressWarnings({ "static-method", "unused" })
	@Test
    public void testScheduleTimerAndRemoveAfterFiring() {
        TimerHandler handler = mock(TimerHandler.class);
        TimeSource source = mock(TimeSource.class);

        TimerServiceImpl timer = new TimerServiceImpl(new SystemOutLog("CORE03-1", "TEST", source), source);

        // CurrentTime = 2
        when(Long.valueOf(source.getTimestamp())).thenReturn(Long.valueOf(2L));

        // Timer = 5
        int timerInstance = timer.scheduleTimer(3, handler);
        verifyZeroInteractions(handler);

        timer.triggerTimers();
        verifyZeroInteractions(handler);

        // CurrentTime = 4
        when(Long.valueOf(source.getTimestamp())).thenReturn(Long.valueOf(4L));
        timer.triggerTimers();
        verifyZeroInteractions(handler);

        // CurrentTime = 5
        when(Long.valueOf(source.getTimestamp())).thenReturn(Long.valueOf(5L));
        timer.triggerTimers();
        verify(handler, new AtMost(1)).onTimer(0, 0);

        when(Long.valueOf(source.getTimestamp())).thenReturn(Long.valueOf(10L));
        assertEquals(0, timer.triggerTimers());
    }
    @SuppressWarnings("static-method")
	@Test
    public void testRemoveTimer() {
        TimerHandler handler = mock(TimerHandler.class);
        TimeSource source = mock(TimeSource.class);
        TimerServiceImpl timer = new TimerServiceImpl(new SystemOutLog("CORE03-1", "TEST", source), source);

        // CurrentTime = 2
        when(Long.valueOf(source.getTimestamp())).thenReturn(Long.valueOf(2L));

        int timerInstance5 = timer.scheduleTimer(3, handler); // 5
        timer.scheduleTimer(2, handler); // 4
        int timerInstance8 = timer.scheduleTimer(8, handler); // 8

        // CurrentTime = 4
        when(Long.valueOf(source.getTimestamp())).thenReturn(Long.valueOf(4L));
        timer.triggerTimers();
        verify(handler, new AtMost(1)).onTimer(0, 0);

        // Remove timer
        timer.cancelTimer(timerInstance5);

        // CurrentTime = 6
        when(Long.valueOf(source.getTimestamp())).thenReturn(Long.valueOf(6L));
        timer.triggerTimers();
        verify(handler, new AtMost(1)).onTimer(0, 0);

        // Remove timer
        timer.cancelTimer(timerInstance8);

        // CurrentTime = 6
        when(Long.valueOf(source.getTimestamp())).thenReturn(Long.valueOf(12L));
        assertEquals(0, timer.triggerTimers());
        verify(handler, new AtMost(1)).onTimer(0, 0);
    }
}
