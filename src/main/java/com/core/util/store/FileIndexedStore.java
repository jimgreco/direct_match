package com.core.util.store;

import com.core.connector.mold.Mold64UDPPacket;
import com.core.util.file.FileFactory;
import com.core.util.file.IndexedFile;
import com.core.util.log.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by jgreco on 6/15/15.
 */
public class FileIndexedStore implements IndexedStore {
    private final ByteBuffer buffer = ByteBuffer.allocate(Mold64UDPPacket.MTU_SIZE);
    private final IndexedFile file;
    private final Log log;

    public FileIndexedStore(Log log, FileFactory factory, String name) throws IOException {
        file = new IndexedFile(factory, log, name);
        this.log=log;
    }

    @Override
    public int size() {
        return file.getNextIndex();
    }

    @Override
    public int getSize(long index) {
        ByteBuffer bufferAtIndex=get((int)index);
        return bufferAtIndex.position();
    }

    @Override
    public void add(ByteBuffer msg) {
        file.write(msg);
    }

    @Override
    public ByteBuffer get(long index, ByteBuffer dest) {
        file.read(index, dest);
        return dest;
    }

    public ByteBuffer get(int index) {
        buffer.clear();
        return get(index, buffer);
    }

	@Override
	public int getCurrentIndex() {
		return file.getNextIndex() - 1;
	}
}
