package com.core.util.log;

import com.core.util.time.TimeSource;
import com.gs.collections.impl.map.mutable.UnifiedMap;

import java.util.Map;

/**
 * Created by jgreco on 11/25/14.
 */
public class SystemOutLogManager implements LogManager {
    private final Map<String, Log> logs = new UnifiedMap<>();
    private final String coreName;
    private final TimeSource source;
    private final boolean color;
    private boolean allDebug;

    public SystemOutLogManager(TimeSource source, String coreName, boolean color) {
        this.coreName = coreName;
        this.source = source;
        this.color = color;
    }

    @Override
    public Log get(String name) {
        Log log = logs.get(name);
        if (log == null) {
            log = new SystemOutLog(coreName, name, source);
            log.setDebug(allDebug);
            logs.put(name, log);
        }
        return log;
    }

    @Override
    public void setDebugForAll(boolean debug) {
        this.allDebug = debug;
        for (Log log : logs.values()) {
            log.setDebug(debug);
        }
    }
}
