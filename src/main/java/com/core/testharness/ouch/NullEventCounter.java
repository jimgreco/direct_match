package com.core.testharness.ouch;

/**
 * Created by jgreco on 1/30/16.
 */
public class NullEventCounter implements EventCounter {

    @Override
    public void createCounter(Object id, int num) {

    }

    @Override
    public void countDown(Object id) {

    }

    @Override
    public void wait(Object id) throws InterruptedException {

    }
}
