package com.core.util.log;

import com.core.util.file.FileFactory;
import com.core.util.time.SystemTimeSource;

/**
 * Created by jgreco on 11/25/14.
 */
public class LogManagerFactory {
    public static LogManager get(SystemTimeSource timeSource, FileFactory service, String type, String coreName, String params) {
        if (type.equalsIgnoreCase("system")) {
            return new SystemOutLogManager(timeSource, coreName, true);
        }
        else if (type.equalsIgnoreCase("file")) {
            return new FileChannelLogManager(service, params);
        }
        return null;
    }
}
