package com.core.util.store;

import com.core.util.file.FileFactory;
import com.core.util.log.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by hli on 9/23/15.
 */
public class FileIndexStore1Index implements IndexedStore {
    private final FileIndexedStore   store;

    public FileIndexStore1Index(Log log, FileFactory factory, String name) throws IOException {
        store = new FileIndexedStore(log, factory, name);
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
