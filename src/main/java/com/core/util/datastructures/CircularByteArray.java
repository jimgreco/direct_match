package com.core.util.datastructures;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 6/16/15.
 */
public class CircularByteArray {
    private int totalBytesAdded;
    private final byte[] bytes;
    private int numAdded;

    public CircularByteArray(int size, int minSize) {
        if (size % minSize != 0) {
            throw new RuntimeException("Invalid size. Must be multiple of 16.");
        }

        bytes = new byte[size];
    }

    public int add(byte[] bytesToAdd) {
        int arrayIndex = getArrayIndex();
        numAdded++;

        int lengthToCopy = Math.min(bytesToAdd.length, bytes.length - arrayIndex);
        System.arraycopy(bytesToAdd, 0, bytes, arrayIndex, lengthToCopy);
        totalBytesAdded += lengthToCopy;

        // if we are circling the buffer
        int remainingLength = bytesToAdd.length - lengthToCopy;
        if (remainingLength > 0) {
            System.arraycopy(bytesToAdd, lengthToCopy, bytes, 0, remainingLength);
        }
        totalBytesAdded += remainingLength;

        return arrayIndex;
    }

    public int add(ByteBuffer bytesToAdd) {
        int arrayIndex = getArrayIndex();
        numAdded++;

        int lengthToCopy = Math.min(bytesToAdd.remaining(), bytes.length - arrayIndex);
        bytesToAdd.get(bytes, arrayIndex, lengthToCopy);
        totalBytesAdded += lengthToCopy;

        // if we are circling the buffer
        int remainingLength = bytesToAdd.remaining();
        if (remainingLength > 0) {
            bytesToAdd.get(bytes, 0, remainingLength);
        }
        totalBytesAdded += remainingLength;

        return arrayIndex;
    }

    int getTotalBytesAdded() {
        return totalBytesAdded;
    }

    int getArrayIndex() {
        return getTotalBytesAdded() % bytes.length;
    }

    int getNumAdded() {
        return numAdded;
    }

    byte[] getArray() {
        return bytes;
    }
}
