package com.core.util.datastructures;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 6/17/15.
 */
public class CircularByteArrayTest {
    @SuppressWarnings("static-method")
	@Test
    public void testPut2x() {
        CircularByteArray array = new CircularByteArray(10, 2);
        array.add("FOO".getBytes());
        array.add("BAR".getBytes());

        Assert.assertEquals(2, array.getNumAdded());
        Assert.assertEquals(6, array.getTotalBytesAdded());
        Assert.assertEquals(6, array.getArrayIndex());
    }

    @SuppressWarnings("static-method")
	@Test
    public void testPutWrap() {
        CircularByteArray array = new CircularByteArray(10, 2);
        array.add("FOO".getBytes());
        array.add("BAR".getBytes());
        array.add("SOO".getBytes());
        array.add("DOO".getBytes());

        Assert.assertEquals(4, array.getNumAdded());
        Assert.assertEquals("OOOBARSOOD", new String(array.getArray()));
        Assert.assertEquals(12, array.getTotalBytesAdded());
        Assert.assertEquals(2, array.getArrayIndex());
    }

    @SuppressWarnings("static-method")
	@Test
    public void testPutWrapByteBuffer() {
        CircularByteArray array = new CircularByteArray(9, 3);
        array.add(ByteBuffer.wrap("FOO".getBytes()));
        array.add(ByteBuffer.wrap("BAR".getBytes()));
        array.add(ByteBuffer.wrap("SOO".getBytes()));
        array.add(ByteBuffer.wrap("DOO".getBytes()));

        Assert.assertEquals(4, array.getNumAdded());
        Assert.assertEquals("DOOBARSOO", new String(array.getArray()));
        Assert.assertEquals(12, array.getTotalBytesAdded());
        Assert.assertEquals(3, array.getArrayIndex());
    }
}
