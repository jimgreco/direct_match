package com.core.services;

import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * Created by jgreco on 1/2/15.
 */
public interface StaticsService<T> {
    void add(T obj);
    boolean isValid(int id);
    T get(int id);
    T get(String name);
    T get(ByteBuffer name);
    int size();
    Iterator<T> iterator();
}
