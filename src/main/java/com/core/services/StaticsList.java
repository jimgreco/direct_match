package com.core.services;

import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.UnifiedMap;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by jgreco on 1/1/15.
 */
public class StaticsList<T extends StaticsList.StaticsObject> {
    final FastList<T> list;
    private final Map<String, T> nameMap = new UnifiedMap<>();
    private final Map<ByteBuffer, T> bbNameMap = new UnifiedMap<>();
    private final StaticsIterator iterator = new StaticsIterator();
    final int startIndex;

    public StaticsList(int startIndex) {
        this.list = new FastList<>();
        this.startIndex = startIndex;
    }

    public boolean add(T item) {
        assert item.getID() >= startIndex && item.getID() <= (list.size() + startIndex);

        if (item.getID() == list.size() + startIndex) {
            nameMap.put(item.getName(), item);
            bbNameMap.put(ByteBuffer.wrap(item.getName().getBytes()), item);
            return list.add(item);
        }
		list.set(item.getID() - startIndex, item);
		return false;
    }

    public T get(int id) {
        assert id >= startIndex && id < (list.size() + startIndex);

        return list.get(id - 1);
    }

    public T get(String name) {
        return nameMap.get(name);
    }

    public T get(ByteBuffer name) {
        return bbNameMap.get(name);
    }

    public int size() {
        return list.size();
    }

    public Iterator<T> iterator() {
        return iterator.reset();
    }

    public interface StaticsObject {
        short getID();
        String getName();
    }

    public class StaticsIterator implements Iterator<T> {
        private int currentId;

        public StaticsIterator reset() {
            currentId = startIndex;
            return this;
        }

        @Override
        public boolean hasNext() {
            return currentId < (list.size() + startIndex);
        }

        @Override
        public T next() {
            return get(currentId++);
        }

        @Override
        public void remove() {
            throw new RuntimeException("Not implemented");
        }
    }
}
