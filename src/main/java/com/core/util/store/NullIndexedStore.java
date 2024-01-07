package com.core.util.store;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 6/28/15.
 */
public class NullIndexedStore implements IndexedStore {
    @Override
    public int size() {
        return 0;
    }

    @Override
    public void add(ByteBuffer msg) {

    }

    @Override
    public int getSize(long index) {
        return 0;
    }

    @Override
    public ByteBuffer get(long index, ByteBuffer dest) {
        return dest;
    }

	@Override
	public int getCurrentIndex() {
		return 0;
	}
}
