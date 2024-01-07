package com.core.latencytesting;

import com.core.nio.SelectorService;
import com.core.util.log.SystemOutLog;
import com.core.util.time.SystemTimeSource;

import java.io.IOException;

/**
 * Created by hli on 4/17/16.
 */
public class LatencyTestFactory {
    SystemTimeSource timeSource;
    SystemOutLog log;
    SelectorService select;

    public SystemTimeSource getTimeSource() {
        return timeSource;
    }

    public SystemOutLog getLogger() {
        return log;
    }

    public SelectorService getSelectorService() {
        return select;
    }

    public LatencyTestFactory(String targetInstance,String testInstance) throws IOException {
         timeSource = new SystemTimeSource();
         log = new SystemOutLog(targetInstance,testInstance, timeSource, 10);
         select = new SelectorService(log, timeSource);
    }

}
