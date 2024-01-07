package com.core.util.time;

import com.core.util.list.IntrusiveDoublyLinkedList;
import com.core.util.log.Log;
import com.core.util.pool.ObjectPool;
import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;

/**
 * User: jgreco
 */
public class TimerServiceImpl implements TimerService {
    private final TimeSource source;
    private final IntrusiveDoublyLinkedList<Timer> timers;
    private final ObjectPool<Timer> timerPool;

    private int internalID = 1;
    private final IntObjectHashMap<Timer> internalIDToTimer = new IntObjectHashMap<>();

    public TimerServiceImpl(Log log, TimeSource source) {
        this.source = source;
        this.timers = new IntrusiveDoublyLinkedList<>();
        this.timerPool  = new ObjectPool<>(log, "Timers", Timer::new, 1024);
    }

    @Override
    public int scheduleTimer(long nanos, TimerHandler handler) {
        return scheduleTimer(nanos, handler, 0);
    }

    @Override
    public int scheduleTimer(long timeout, TimerHandler handler, int ref) {

        if (timeout < 0) {
            throw new IllegalArgumentException("Cannot have timeout < 0: " + timeout);
        }

        if (handler == null) {
            throw new IllegalArgumentException("Cannot have null timer handler");
        }

        Timer timer = timerPool.create();
        timer.nanoTime = source.getTimestamp() + timeout;
        timer.handler = handler;
        timer.ref = ref;
        timer.internalID = internalID;
        internalIDToTimer.put(internalID, timer);

        timers.insert(timer);
        return internalID++;
    }

    @Override
    public int cancelTimer(int timerID) {
        if (timerID == 0) {
            return TimerService.NULL_VALUE;
        }

        Timer timer = internalIDToTimer.remove(timerID);

        if (timer != null) {
            timers.remove(timer);
            timerPool.delete(timer);
        }

        return TimerService.NULL_VALUE;
    }

    public long triggerTimers() {
        long currentTime = source.getTimestamp();

        Timer timer;
        while ((timer = timers.peek()) != null) {
            if (timer.nanoTime > currentTime) {
                return timer.nanoTime - currentTime;
            }

            // remove
            timers.poll();

            TimerHandler handler = timer.handler;
            int ref = timer.ref;
            int internalID = timer.internalID;

            if (internalIDToTimer.remove(internalID) != null) {
                timerPool.delete(timer);
                handler.onTimer(internalID, ref);
            }
        }

        return 0;
    }
}
