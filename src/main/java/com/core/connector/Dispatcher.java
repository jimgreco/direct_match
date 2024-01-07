package com.core.connector;

import com.core.util.time.TimeSource;

/**
 * User: jgreco
 */
public interface Dispatcher extends TimeSource {
    void subscribe(Object listener);

    @Override
    long getTimestamp();
}
