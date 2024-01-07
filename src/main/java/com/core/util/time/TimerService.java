package com.core.util.time;

/**
 * User: jgreco
 */
public interface TimerService {
    int NULL_VALUE = 0;
    int scheduleTimer(long nanos, TimerHandler handler);
    int scheduleTimer(long nanos, TimerHandler handler, int ref);
    int cancelTimer(int timerID);
}
