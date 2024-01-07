package com.core.util.datastructures.contracts;

/**
 * Created by johnlevidy on 6/3/15.
 */
public interface Linkable<T> {
    T next();
    void setNext(T next);
}
