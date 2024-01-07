package com.core.util.store;

import com.core.util.BinaryUtils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 6/17/15.
 */
public class MemoryIndexedStoreTest {
    @SuppressWarnings("static-method")
	@Test
    public void testWriteWrap1() {
        MemoryIndexedStore blocks = new MemoryIndexedStore(3, 5);
        blocks.add(ByteBuffer.wrap("DO".getBytes()));
        blocks.add(ByteBuffer.wrap("BAR".getBytes()));
        blocks.add(ByteBuffer.wrap("FOOO".getBytes()));
        //blocks.add(ByteBuffer.wrap("COOLIO".getBytes()));

        Assert.assertEquals(4, blocks.getNumBlocks());
        Assert.assertEquals(20, blocks.getTotalBytesAdded());

        Assert.assertEquals(0, blocks.getSize(0));
        Assert.assertEquals(3, blocks.getSize(1));
        Assert.assertEquals(4, blocks.getSize(2));
    }

    @SuppressWarnings("static-method")
    @Test
    public void testWriteWrap2() {
        MemoryIndexedStore blocks = new MemoryIndexedStore(3, 5);
        blocks.add(ByteBuffer.wrap("DO".getBytes()));
        blocks.add(ByteBuffer.wrap("BAR".getBytes()));
        blocks.add(ByteBuffer.wrap("FOOO".getBytes()));
        blocks.add(ByteBuffer.wrap("COOLIO".getBytes()));

        Assert.assertEquals(6, blocks.getNumBlocks());
        Assert.assertEquals(30, blocks.getTotalBytesAdded());

        Assert.assertEquals(0, blocks.getSize(0));
        Assert.assertEquals(0, blocks.getSize(1));
        Assert.assertEquals(0, blocks.getSize(2));
        Assert.assertEquals(6, blocks.getSize(3));
    }

    @SuppressWarnings("static-method")
    @Test
    public void testWriteWrap3() {
        MemoryIndexedStore blocks = new MemoryIndexedStore(3, 5);
        blocks.add(ByteBuffer.wrap("DO".getBytes()));
        blocks.add(ByteBuffer.wrap("BAR".getBytes()));
        blocks.add(ByteBuffer.wrap("FOOO".getBytes()));
        blocks.add(ByteBuffer.wrap("COOLIOOOO".getBytes()));

        Assert.assertEquals(7, blocks.getNumBlocks());
        Assert.assertEquals(35, blocks.getTotalBytesAdded());

        Assert.assertEquals(0, blocks.getSize(0));
        Assert.assertEquals(0, blocks.getSize(1));
        Assert.assertEquals(0, blocks.getSize(2));
        Assert.assertEquals(9, blocks.getSize(3));
    }

    @SuppressWarnings("static-method")
    @Test
    public void testRead() {
        MemoryIndexedStore blocks = new MemoryIndexedStore(3, 5);
        blocks.add(ByteBuffer.wrap("DO".getBytes()));
        blocks.add(ByteBuffer.wrap("BAR".getBytes()));
        blocks.add(ByteBuffer.wrap("FOOO".getBytes()));

        Assert.assertEquals("", getString(blocks, 0));
        Assert.assertEquals("BAR", getString(blocks, 1));
        Assert.assertEquals("FOOO", getString(blocks, 2));
    }

    @SuppressWarnings("static-method")
    @Test
    public void testRead2() {
        MemoryIndexedStore blocks = new MemoryIndexedStore(3, 5);
        blocks.add(ByteBuffer.wrap("DO".getBytes()));
        blocks.add(ByteBuffer.wrap("BAR".getBytes()));
        blocks.add(ByteBuffer.wrap("FOOOOOOOO".getBytes()));

        Assert.assertEquals("", getString(blocks, 0));
        Assert.assertEquals("", getString(blocks, 1));
        Assert.assertEquals("FOOOOOOOO", getString(blocks, 2));
    }

    private static String getString(MemoryIndexedStore blocks, int index) {
        ByteBuffer value = blocks.get(index, ByteBuffer.allocate(100));
        value.flip();
        return new String(BinaryUtils.toBytes(value));
    }
}
