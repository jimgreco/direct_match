package com.core.testharness.ouch;

import com.gs.collections.impl.map.mutable.UnifiedMap;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by jgreco on 1/30/16.
 */
public class CountDownLatchEventCounter implements EventCounter {
    private UnifiedMap<Object, CountDownLatch> latches = new UnifiedMap<>();

    @Override
    public void createCounter(Object id, int num) {
        latches.put(id, new CountDownLatch(num));
    }

    @Override
    public void countDown(Object id) {
        CountDownLatch countDownLatch = latches.get(id);
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    @Override
    public void wait(Object id) throws InterruptedException {
        CountDownLatch countDownLatch = latches.get(id);
        if (countDownLatch != null) {
            countDownLatch.await(20, TimeUnit.SECONDS);
        }
    }
}
