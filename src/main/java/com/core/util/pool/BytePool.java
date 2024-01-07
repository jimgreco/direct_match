package com.core.util.pool;

import java.util.Arrays;

/**
 * Created by jgreco on 8/11/15.
 */
public class BytePool {
    private static final byte NULL = -1;

    private final int blockSize;
    private final byte[] nullObject;

    private final byte[] bytes;
    private int head;

    public BytePool(int size, int payloadSize) {
        this.blockSize = Integer.SIZE + payloadSize;
        this.bytes = new byte[blockSize * size];

        this.nullObject = new byte[payloadSize];
        Arrays.fill(nullObject, NULL);

        head = 0;
        for (int i=0; i<size-1; i++) {
            writeInt(getIndex(i), i + 1);
        }
        writeInt(getIndex(size-1), NULL);
    }

    public int create() {
        // TODO: Growing

        // move the head to the next
        int headIndex = getIndex(head);
        head = readInt(headIndex);
        // overwrite pointer to next
        writeInt(headIndex, NULL);
        // bound ByteBuffer on size
        return headIndex + Integer.SIZE;
    }

    public void delete(int index) {
        // get start of the block including integer pointer
        int block = getBlock(index - Integer.SIZE);
        // point to old head
        writeInt(index - Integer.SIZE, head);
        // null object out
        System.arraycopy(nullObject, 0, bytes, index, nullObject.length);
        // assign a new head
        head = block;
    }

    private void writeInt(int index, int val) {
        bytes[index++] = (byte) (val & 0xFF);
        val >>= 8;
        bytes[index++] = (byte) (val & 0xFF);
        val >>= 8;
        bytes[index++] = (byte) (val & 0xFF);
        val >>= 8;
        bytes[index] = (byte) (val & 0xFF);
    }

    private int readInt(int index) {
        return bytes[index++] | (bytes[index++] << 8) | (bytes[index++] << 16) | (bytes[index] << 24);
    }

    private int getIndex(int block) {
        return block * blockSize;
    }

    private int getBlock(int index) {
        return index / blockSize;
    }
}
