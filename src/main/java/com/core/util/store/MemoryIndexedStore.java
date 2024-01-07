package com.core.util.store;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 6/17/15.
 */
public class MemoryIndexedStore implements IndexedStore {
    private final int blockSize;
    private final int blocksInArray;

    private int firstMessageIndex;
    private int nextMessageIndex;

    private int currentBlock;

    private final int[] indexes;
    private final byte[] bytes;

    public MemoryIndexedStore(int blocks, int blockSize) {
        this.blockSize = blockSize;
        this.bytes = new byte[blocks * blockSize];
        this.indexes = new int[blocks];
        this.blocksInArray = bytes.length / blockSize;
    }

    private int getByteOffset(int index) {
        return (index * blockSize) % bytes.length;
    }

    @Override
    public void add(ByteBuffer buffer) {
        // 2 byte for length
        int length = buffer.remaining() + Short.BYTES;
        byte nBlocks = (byte)Math.ceil(1.0 * length / blockSize);

        // remove old blocks
        int oldEndBlock = currentBlock - blocksInArray + nBlocks;
        while (firstMessageIndex < nextMessageIndex && indexes[firstMessageIndex % indexes.length] < oldEndBlock) {
            firstMessageIndex++;
        }

        int startByteOffset = getByteOffset(currentBlock);
        int endByteOffset = getByteOffset(currentBlock + nBlocks);

        // store the length as first two bytes
        bytes[startByteOffset] = (byte) ((length - Short.BYTES) & 0xFF);
        bytes[startByteOffset + 1] = (byte) ((length - Short.BYTES) >> 8);

        if (endByteOffset != 0 && endByteOffset <= startByteOffset) {
            int sizeToCopy = bytes.length - (startByteOffset + Short.BYTES);
            buffer.get(bytes, startByteOffset + Short.BYTES, sizeToCopy);
            startByteOffset = -Short.BYTES;
        }
        buffer.get(bytes, startByteOffset + Short.BYTES, buffer.remaining());

        indexes[nextMessageIndex % indexes.length] = currentBlock;
        nextMessageIndex++;
        currentBlock += nBlocks;
    }

    @Override
    public ByteBuffer get(long idx, ByteBuffer dest) {
        int index = (int)idx;
        int numBytes = getSize(index);
        if (numBytes <= 0) {
            return dest;
        }

        int block = indexes[index];
        int startByteOffset = getByteOffset(block) + Short.BYTES;
        int endByteOffset = (startByteOffset + numBytes) % bytes.length;

        if (endByteOffset <= startByteOffset) {
            dest.put(bytes, startByteOffset, bytes.length - startByteOffset);
            dest.put(bytes, 0, endByteOffset);
        }
        else {
            dest.put(bytes, startByteOffset, endByteOffset - startByteOffset);
        }

        return dest;
    }

    public ByteBuffer get(int index) {
        ByteBuffer buffer = ByteBuffer.allocate(getSize(index));
        get(index, buffer);
        return buffer;
    }

    int getNumBlocks() {
        return currentBlock;
    }

    int getTotalBytesAdded() {
        return currentBlock * blockSize;
    }

    @Override
    public int size() {
        return nextMessageIndex;
    }

    @Override
    public int getSize(long idx) {
        int index = (int)idx;
        if (index < firstMessageIndex || index >= nextMessageIndex) {
            return 0;
        }

        int block = indexes[index % indexes.length];
        int startByteOffset = getByteOffset(block);
        return (bytes[startByteOffset + 1] << 8) | (bytes[startByteOffset] & 0xFF);
    }

	@Override
	public int getCurrentIndex() {
		return nextMessageIndex - 1;
	}
}
