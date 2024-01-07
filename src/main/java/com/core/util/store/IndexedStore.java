package com.core.util.store;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 6/15/15.
 */
public interface IndexedStore {
    int size();
    int getCurrentIndex();
    void add(ByteBuffer msg);
    int getSize(long index);
    ByteBuffer get(long index, ByteBuffer dest);
}
