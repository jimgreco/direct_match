package com.core.util.datastructures.contracts;

/**
 * Created by johnlevidy on 6/3/15.
 */
public class AbstractLinkable<T> implements Linkable<T>
{
    T next = null;

    @Override
    public T next() {
        return this.next;
    }

    @Override
    public void setNext(T next) {
        this.next = next;
    }
}
