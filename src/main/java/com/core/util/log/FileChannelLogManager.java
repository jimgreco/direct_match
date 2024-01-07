package com.core.util.log;

import com.core.util.file.File;
import com.core.util.file.FileFactory;
import com.gs.collections.impl.map.mutable.UnifiedMap;

import java.util.Map;

/**
 * Created by jgreco on 11/25/14.
 * Don't use, in progress
 */
public class FileChannelLogManager implements LogManager {
    private final Map<String, Log> logs = new UnifiedMap<>();
    private final File file;

    public FileChannelLogManager(FileFactory service, String fileName) {
        try {
            file = service.createFile("w", fileName);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Unable to create file");
        }
    }

    @Override
    public Log get(String name) {
        Log log = logs.get(name);
        if (log == null) {
            log = new FileChannelLog(file);
            logs.put(name, log);
        }
        return log;
    }

    @Override
    public void setDebugForAll(boolean debug) {
        for (Log log : logs.values()) {
            log.setDebug(debug);
        }
    }
}
