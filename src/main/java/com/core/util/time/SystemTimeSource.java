package com.core.util.time;


/**
 * User: jgreco
 */
public class SystemTimeSource implements TimeSource {
    private long startTime;
    private long startNanos;

    public SystemTimeSource() {
        this.startTime = System.currentTimeMillis() * 1000 * 1000;
        this.startNanos = System.nanoTime();
    }

    @Override
    public long getTimestamp() {
        long elapsedNanos = System.nanoTime() - startNanos;
        return startTime + elapsedNanos;
    }
}
