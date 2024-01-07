package com.core;

import com.core.nio.SelectorService;
import com.core.util.TimeUtils;
import com.core.util.log.SystemOutLog;
import com.core.util.time.SimulatedTimeSource;
import com.core.util.time.SystemTimeSource;
import com.core.util.time.TimeSource;

import java.io.IOException;

/**
 * Created by jgreco on 6/25/15.
 */
public class GenericSelectorTest {
    protected SimulatedTimeSource simulatedTime;
    protected TimeSource time;
    protected final SystemOutLog log;
    protected SelectorService select;

    @SuppressWarnings("static-method")
	protected boolean isSimulatedTime() {
        return false;
    }

    public GenericSelectorTest()  {
        simulatedTime = new SimulatedTimeSource();
        time = isSimulatedTime() ? simulatedTime : new SystemTimeSource();
        this.log = new SystemOutLog("Test", "TEST", time);
        this.log.setDebug(true);
        try {
            this.select = new SelectorService(log, time);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void advanceTime(int millis) {
        simulatedTime.advanceTime(millis * TimeUtils.NANOS_PER_MILLI);
        run();
    }

    protected void run() {
        select.runOnce();
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
