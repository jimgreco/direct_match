package com.core.util.store;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 6/16/15.
 */
public class MemoryIndexedStore1Index implements IndexedStore {
    private final MemoryIndexedStore store;

    public MemoryIndexedStore1Index(int messages) {
        store = new MemoryIndexedStore(messages, 64);
    }

    @Override
    public int size() {
        return store.size();
    }

    @Override
    public void add(ByteBuffer msg) {
        store.add(msg);
    }

    @Override
    public int getSize(long index) {
        return store.getSize(index - 1);
    }

    @Override
    public ByteBuffer get(long index, ByteBuffer dest) {
        return store.get(index - 1, dest);
    }

    public ByteBuffer get(int index) {
        return store.get(index - 1);
    }

	@Override
	public int getCurrentIndex() {
		return store.getCurrentIndex();
	}
}
