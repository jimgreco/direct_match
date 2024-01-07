package com.core.util.pool;

import com.core.util.datastructures.contracts.Linkable;

/**
 * Created by jgreco on 6/14/15.
 */
public class PoolableWrapper<T> implements Poolable<T>, Linkable<T> {
    private final T val;
    private T next;

    public PoolableWrapper(T val) {
        this.val = val;
    }

    @Override
    public void setNext(T next) {
        this.next = next;
    }

    @Override
    public T next() {
        return next;
    }

    @Override
    public void clear() {

    }

    public T get() {
        return val;
    }
}
