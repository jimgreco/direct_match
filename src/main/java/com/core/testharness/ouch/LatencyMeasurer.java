package com.core.testharness.ouch;

/**
 * Created by hli on 5/2/16.
 */
public interface LatencyMeasurer {
    void start(long id);

    void stop(long id);
}
