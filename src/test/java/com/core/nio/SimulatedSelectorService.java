package com.core.nio;

import com.core.util.TimeUtils;
import com.core.util.log.Log;
import com.core.util.time.SimulatedTimeSource;
import com.core.util.time.TimerHandler;
import com.core.util.time.TimerService;
import com.core.util.time.TimerServiceImpl;

/**
 * Created by jgreco on 12/24/14.
 */
public class SimulatedSelectorService implements
        TimerService {
    private final SimulatedTimeSource time;
    private final TimerServiceImpl timerService;

    public SimulatedSelectorService(Log log) {
        this(log, new SimulatedTimeSource());
    }

    public SimulatedSelectorService(Log log, SimulatedTimeSource timeSource) {
        time = timeSource;
        timerService = new TimerServiceImpl(log, time);
    }

    public void runFor(int millis) {
        time.advanceTime(millis * TimeUtils.NANOS_PER_MILLI);

        runOnce();
    }

    public void runOnce() {
        timerService.triggerTimers();
    }

    public SimulatedTimeSource getTimeSource() {
        return time;
    }

    @Override
    public int scheduleTimer(long nanos, TimerHandler handler) {
        return timerService.scheduleTimer(nanos, handler);
    }

    @Override
    public int scheduleTimer(long nanos, TimerHandler handler, int ref) {
        return timerService.scheduleTimer(nanos, handler, ref);
    }

    @Override
    public int cancelTimer(int timer) {
        timerService.cancelTimer(timer);
        return TimerService.NULL_VALUE;
    }
}
