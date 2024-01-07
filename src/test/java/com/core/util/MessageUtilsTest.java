package com.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 5/17/15.
 */
public class MessageUtilsTest {
    @Test
    @SuppressWarnings("static-method")
    public void testSetStringByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(50);
        ByteBuffer value =  ByteBuffer.wrap("FOO".getBytes());

        Assert.assertEquals(5, MessageUtils.setVariableString(buffer, 10, value, 20));
        Assert.assertEquals(20, buffer.getShort(10));
        Assert.assertEquals(3, buffer.getShort(20));
        Assert.assertEquals('F', buffer.get(22));
        Assert.assertEquals('O', buffer.get(23));
        Assert.assertEquals('O', buffer.get(24));
    }

    @Test
    @SuppressWarnings("static-method")
    public void testSetStringString() {
        ByteBuffer buffer = ByteBuffer.allocate(50);
        String value = "FOO";

        Assert.assertEquals(5, MessageUtils.setVariableString(buffer, 10, value, 20));
        Assert.assertEquals(20, buffer.getShort(10));
        Assert.assertEquals(3, buffer.getShort(20));
        Assert.assertEquals('F', buffer.get(22));
        Assert.assertEquals('O', buffer.get(23));
        Assert.assertEquals('O', buffer.get(24));
    }

    @Test
    @SuppressWarnings("static-method")
    public void testSetStringEmpty() {
        ByteBuffer buffer = ByteBuffer.allocate(50);
        ByteBuffer value =  ByteBuffer.wrap("".getBytes());

        Assert.assertEquals(2, MessageUtils.setVariableString(buffer, 10, value, 20));
        Assert.assertEquals(20, buffer.getShort(10));
        Assert.assertEquals(0, buffer.getShort(20));
    }

    @Test
    @SuppressWarnings("static-method")
    public void testSetStringOutOfBounds() {
        ByteBuffer buffer = ByteBuffer.allocate(50);
        ByteBuffer value =  ByteBuffer.wrap("FOO".getBytes());

        Assert.assertEquals(0, MessageUtils.setVariableString(buffer, 10, value, 49));
        Assert.assertEquals(0, buffer.getShort(10));
    }

    @Test
    @SuppressWarnings("static-method")
    public void testSetStringOutOfBounds2() {
        ByteBuffer buffer = ByteBuffer.allocate(50);
        String value = "FOO";

        Assert.assertEquals(0, MessageUtils.setVariableString(buffer, 10, value, 47));
        Assert.assertEquals(0, buffer.getShort(10));
    }

    @Test
    @SuppressWarnings("static-method")
    public void testSetStringOutOfBoundsLongString() {
        ByteBuffer buffer = ByteBuffer.allocate(50);
        ByteBuffer value =  ByteBuffer.wrap("FOOOOOOOOOOOOOOOOOOOOOOOOO".getBytes());

        Assert.assertEquals(0, MessageUtils.setVariableString(buffer, 10, value, 40));
        Assert.assertEquals(0, buffer.getShort(10));
    }

    @Test
    @SuppressWarnings("static-method")
    public void testSetStringOffsetLessThanFieldOffset() {
        ByteBuffer buffer = ByteBuffer.allocate(50);
        String value = "FOO";

        Assert.assertEquals(0, MessageUtils.setVariableString(buffer, 20, value, 10));
        Assert.assertEquals(0, buffer.getShort(10));
    }

	@Test
    @SuppressWarnings("static-method")
    public void testGetString() {
        ByteBuffer buffer = ByteBuffer.allocate(50);
        String value = "FOO";

        MessageUtils.setVariableString(buffer, 10, value, 20);
        Assert.assertEquals(ByteBuffer.wrap("FOO".getBytes()), MessageUtils.getVariableString(buffer, 10));
    }

    @Test
    @SuppressWarnings("static-method")
    public void testGetStringNone() {
        ByteBuffer buffer = ByteBuffer.allocate(50);
        buffer.putShort(10, (short)0);

        Assert.assertEquals(ByteBuffer.wrap("".getBytes()), MessageUtils.getVariableString(buffer, 10));
    }

    @Test
    @SuppressWarnings("static-method")
    public void testGetStringOutOfBounds() {
        ByteBuffer buffer = ByteBuffer.allocate(50);
        Assert.assertEquals(ByteBuffer.wrap("".getBytes()), MessageUtils.getVariableString(buffer, 60));
    }

    @Test
    @SuppressWarnings("static-method")
    public void testGetStringInvalid() {
        ByteBuffer buffer = ByteBuffer.allocate(50);
        buffer.putShort(10, (short)5);
        buffer.putShort(5, (short)2);

        Assert.assertEquals(ByteBuffer.wrap("".getBytes()), MessageUtils.getVariableString(buffer, 10));
    }

    @Test
    @SuppressWarnings("static-method")
    public void testGetStringInvalid2() {
        ByteBuffer buffer = ByteBuffer.allocate(50);
        buffer.putShort(10, (short)49);

        Assert.assertEquals(ByteBuffer.wrap("".getBytes()), MessageUtils.getVariableString(buffer, 10));
    }

    @Test
    @SuppressWarnings("static-method")
    public void testGetStringInvalid3() {
        ByteBuffer buffer = ByteBuffer.allocate(50);
        buffer.putShort(10, (short)60);

        Assert.assertEquals(ByteBuffer.wrap("".getBytes()), MessageUtils.getVariableString(buffer, 10));
    }

    @Test
    @SuppressWarnings("static-method")
    public void testGetStringInvalid4() {
        ByteBuffer buffer = ByteBuffer.allocate(50);
        buffer.putShort(10, (short)40);
        buffer.putShort(40, (short)20);

        Assert.assertEquals(ByteBuffer.wrap("".getBytes()), MessageUtils.getVariableString(buffer, 10));
    }
}
