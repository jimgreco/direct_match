package com.core.util.pool;

import com.core.util.Factory;
import com.core.util.datastructures.contracts.Linkable;
import com.core.util.log.Log;

/**
 * User: jgreco
 */
public class ObjectPool<T extends Poolable<T> & Linkable<T>> {
    private final Log log;
    private final String name;

    private final Factory<T> factory;
    private int allocated;
    private int remaining;
    private T head;

    public ObjectPool(Log log, String name, Factory<T> factory, int initialSize) {
        this.factory = factory;
        this.log = log;
        this.name = name;

        allocate(initialSize);
    }


    public int getAllocated() {
        return allocated;
    }

    public int getRemaining() {
        return remaining;
    }

    @SuppressWarnings("unchecked")
	public T create() {
        if (remaining == 0) {
            allocate(allocated);
        }

        T createdObj = head;

        if (createdObj.next() != null) {
            head = createdObj.next();
        }
        else {
            head = null;
        }

        remaining--;
        createdObj.clear();
        createdObj.setNext(null);
        return createdObj;
    }

    public void delete(T obj) {
        if (obj == null) {
            return;
        }

        remaining++;
        obj.clear();
        obj.setNext(head);
        head = obj;
    }

    private void allocate(int size) {
        if (log == null) {
            System.out.println(name + " reallocating pool from " + allocated + " to " + (allocated + size));
        }
        else {
            log.info(log.log().add(name).add(" reallocating pool from ").add(allocated).add(" to ").add(allocated + size));
        }

        for (int i=0; i<size; i++) {
            T newHead = factory.create();
            newHead.setNext(head);
            head = newHead;
        }

        remaining += size;
        allocated += size;
    }
}
