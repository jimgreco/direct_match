package com.core.services;

/**
 * Created by jgreco on 1/2/15.
 */
public interface StaticsFactory<T> {
    T create(short id, String name);
}
