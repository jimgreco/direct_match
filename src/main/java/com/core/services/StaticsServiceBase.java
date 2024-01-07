package com.core.services;

import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * Created by jgreco on 1/2/15.
 */
public abstract class StaticsServiceBase<T extends StaticsList.StaticsObject> implements StaticsService<T>, Iterable<T> {
    private final StaticsList<T> staticsObjects;
    private final int startIndex;

    public StaticsServiceBase(int startIndex) {
        this.staticsObjects = new StaticsList<>(startIndex);
        this.startIndex = startIndex;
    }

    @Override
    public void add(T obj) {
        assert obj.getID() == size() + startIndex;
        staticsObjects.add(obj);
    }

    @Override
    public int size() {
        return staticsObjects.size();
    }

    @Override
    public boolean isValid(int id) {
        return id >= startIndex && id < (size() + startIndex);
    }

    @Override
    public T get(int id) {
        if (!isValid(id)) {
            return null;
        }

        return staticsObjects.get(id);
    }

    @Override
    public T get(String name) {
        if (name == null) {
            return null;
        }

        return staticsObjects.get   (name);
    }

    @Override
    public T get(ByteBuffer name) {
        if (name == null) {
            return null;
        }

        return staticsObjects.get(name);
    }

    @Override
    public Iterator<T> iterator() {
        return staticsObjects.iterator();
    }
}
