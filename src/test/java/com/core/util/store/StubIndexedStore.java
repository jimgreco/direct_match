package com.core.util.store;

import com.core.util.BinaryUtils;
import com.gs.collections.impl.list.mutable.FastList;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by jgreco on 6/30/15.
 */
public class StubIndexedStore implements IndexedStore {
    private List<ByteBuffer> store = new FastList<>();

    @Override
    public int size() {
        return store.size();
    }

    @Override
    public void add(ByteBuffer msg) {
        store.add(BinaryUtils.createCopy(msg));
    }

    @Override
    public int getSize(long index) {
        return store.get((int)index).remaining();
    }

    @Override
    public ByteBuffer get(long index, ByteBuffer dest) {
        BinaryUtils.copy(dest, store.get((int)index));
        return dest;
    }

	@Override
	public int getCurrentIndex() {
		return size() - 1;
	}
}
