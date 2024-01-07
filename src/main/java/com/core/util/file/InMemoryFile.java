package com.core.util.file;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by jgreco on 1/4/15.
 * This is crap and in progress, don't use
 */
public class InMemoryFile implements File {
    private ByteBuffer backingBuffer;

    public InMemoryFile() {
        this(10 * 1024 * 1024); // 10 MB
    }

    public InMemoryFile(int initialSize) {
        backingBuffer = ByteBuffer.allocate(initialSize);
    }

    @Override
    public long size() {
        return backingBuffer.position();
    }

    @Override
    public int write(ByteBuffer buf, long offset) throws IOException {
        throw new RuntimeException("Not implemented!!!");
    }

    @Override
    public int write(ByteBuffer buf) throws IOException {
        if (backingBuffer.remaining() < buf.remaining()) {
            ByteBuffer newBuf = ByteBuffer.allocate(2 * backingBuffer.capacity());
            backingBuffer.flip();
            newBuf.put(backingBuffer);
            backingBuffer = newBuf;
        }

        int size = buf.remaining();
        backingBuffer.put(buf);
        return size;
    }

    @Override
    public int read(ByteBuffer buf, long offset) throws IOException {
        if (offset >= size()) {
            return 0;
        }

        int maxLength = Math.min((int) (size() - offset), buf.remaining());

        int oldPos = backingBuffer.position();
        int oldLim = backingBuffer.limit();

        backingBuffer.position((int) offset);
        backingBuffer.limit((int) (offset + maxLength));

        buf.put(backingBuffer);

        backingBuffer.clear();
        backingBuffer.position(oldPos);
        backingBuffer.limit(oldLim);

        return maxLength;
    }

    @Override
    public int read(ByteBuffer buffer) throws IOException {
        int count = 0;
        while (buffer.hasRemaining() && backingBuffer.hasRemaining()) {
            buffer.put(backingBuffer.get());
            count++;
        }
        return count;
    }
}
