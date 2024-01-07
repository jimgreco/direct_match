package com.core.util.time;

import com.core.util.datastructures.contracts.Linkable;
import com.core.util.list.IntrusiveDoublyLinkedListItem;
import com.core.util.pool.Poolable;

/**
 * User: jgreco
 */
class Timer implements IntrusiveDoublyLinkedListItem<Timer>, Poolable<Timer>, Linkable<Timer> {
    private Timer next;
    private Timer prev;

    int ref;
    TimerHandler handler;
    long nanoTime;
    int internalID;

    @Override
    public void setNext(Timer next) {
        this.next = next;
    }


    @Override
    public void setPrev(Timer prev) {
        this.prev = prev;
    }

    @Override
    public int compare(Timer item) {
        if (nanoTime > item.nanoTime) {
            return 1;
        }

        if (nanoTime > item.nanoTime) {
            return -1;
        }

        return 0;
    }

    @Override
    public Timer next() {
        return next;
    }

    @Override
    public Timer prev() {
        return prev;
    }

    @Override
    public void clear() {
        next = null;
        prev = null;
        handler = null;
        nanoTime = 0;
        ref = 0;
    }
}
