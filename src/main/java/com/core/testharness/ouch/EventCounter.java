package com.core.testharness.ouch;

/**
 * Created by jgreco on 1/30/16.
 */
public interface EventCounter {
    void createCounter(Object id, int num);
    void countDown(Object id);
    void wait(Object id) throws InterruptedException;
}
