package com.core.util.log;

/**
 * Created by jgreco on 11/25/14.
 */
public interface LogManager {
    Log get(String name);
    void setDebugForAll(boolean debug);
}
