package com.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.time.LocalTime;

/**
 * Created by jgreco on 7/16/15.
 */
@SuppressWarnings("static-method")
public class TextUtilsByteBufferTest {
    @Test
    public void testWriteNumberLeftPadded() {
        ByteBuffer byteBuffer = TextUtils.writeNumberLeftPadded(ByteBuffer.allocate(3), 215, 3, '0');
        byteBuffer.flip();
        Assert.assertEquals("215", BinaryUtils.toString(byteBuffer));
    }

	@Test
    public void testWriteLeftNumberPaddedWithZeros() {
        ByteBuffer byteBuffer = TextUtils.writeNumberLeftPadded(ByteBuffer.allocate(3), 21, 3, '0');
        byteBuffer.flip();
        Assert.assertEquals("021", BinaryUtils.toString(byteBuffer));
    }

    @Test
    public void testWriteNumberPaddedWithBufferNotFilled() {
        ByteBuffer byteBuffer = TextUtils.writeNumberLeftPadded(ByteBuffer.allocate(5), 21, 3, '0');
        byteBuffer.flip();
        Assert.assertEquals("021", BinaryUtils.toString(byteBuffer));
    }

    @Test
    public void testNumberStringSize() {
        Assert.assertEquals(1, TextUtils.stringSize(0));
        Assert.assertEquals(1, TextUtils.stringSize(1));
        Assert.assertEquals(2, TextUtils.stringSize(12));
        Assert.assertEquals(5, TextUtils.stringSize(12345));
    }

    @Test
    public void testNegativeNumberStringSize() {
        Assert.assertEquals(2, TextUtils.stringSize(-1));
        Assert.assertEquals(3, TextUtils.stringSize(-10));
        Assert.assertEquals(6, TextUtils.stringSize(-12345));
    }

    @Test
    public void testWriteNumber() {
        Assert.assertEquals(2, TextUtils.stringSize(-1));
        Assert.assertEquals(3, TextUtils.stringSize(-10));
        Assert.assertEquals(6, TextUtils.stringSize(-12345));
    }

    @Test
    public void testWriteBoolTrue() {
        ByteBuffer allocate = ByteBuffer.allocate(10);
        TextUtils.writeBool(allocate, true);
        allocate.flip();

        Assert.assertEquals("true", BinaryUtils.toString(allocate));
    }

    @Test
    public void testWriteBoolFalse() {
        ByteBuffer allocate = ByteBuffer.allocate(10);
        TextUtils.writeBool(allocate, false);
        allocate.flip();

        Assert.assertEquals("false", BinaryUtils.toString(allocate));
    }

    @Test
    public void testWriteDate() {
        ByteBuffer allocate = ByteBuffer.allocate(50);
        TextUtils.writeDateUTC(allocate, 1437227242121L * TimeUtils.NANOS_PER_MILLI);
        allocate.flip();
        Assert.assertEquals("20150718", BinaryUtils.toString(allocate));
    }

    @Test
    public void testWriteTime() {
        ByteBuffer allocate = ByteBuffer.allocate(50);
        TextUtils.writeTimeUTC(allocate, 1437227242121L * TimeUtils.NANOS_PER_MILLI);
        allocate.flip();
        Assert.assertEquals("13:47:22.121", BinaryUtils.toString(allocate));
    }

    @Test
    public void testWriteDateTime() {
        ByteBuffer allocate = ByteBuffer.allocate(50);
        TextUtils.writeDateTimeUTC(allocate, 1437227242121L * TimeUtils.NANOS_PER_MILLI);
        allocate.flip();
        Assert.assertEquals("20150718-13:47:22.121", BinaryUtils.toString(allocate));
    }

    @Test
    public void testParseNumber() {
        ByteBuffer allocate = ByteBuffer.allocate(50);
        BinaryUtils.copy(allocate, "123456").flip();
        Assert.assertEquals(123456, TextUtils.parseNumber(allocate));
    }

    @Test
    public void testParseNegativeNumber() {
        ByteBuffer allocate = ByteBuffer.allocate(50);
        BinaryUtils.copy(allocate, "-123456").flip();
        Assert.assertEquals(-123456, TextUtils.parseNumber(allocate));
    }

    @Test
    public void testParseNumberLeftPadded() {
        ByteBuffer allocate = ByteBuffer.allocate(50);
        BinaryUtils.copy(allocate, "     123456").flip();
        Assert.assertEquals(123456, TextUtils.parseNumberLeftPadded(allocate));
    }

    @Test
    public void testParseNegativeNumberLeftPadded() {
        ByteBuffer allocate = ByteBuffer.allocate(50);
        BinaryUtils.copy(allocate, "     -123456").flip();
        Assert.assertEquals(-123456, TextUtils.parseNumberLeftPadded(allocate));
    }

    @Test
    public void testParseInvalidNumberAsZero() {
        ByteBuffer allocate = ByteBuffer.allocate(50);
        BinaryUtils.copy(allocate, "FOO").flip();
        Assert.assertEquals(0, TextUtils.parseNumber(allocate));
    }

    @Test
    public void testParseNumberWithDecimal() {
        ByteBuffer allocate = ByteBuffer.allocate(50);
        BinaryUtils.copy(allocate, "100.0").flip();
        Assert.assertEquals(100, TextUtils.parseNumber(allocate));
    }

    @Test
    public void testParseInvalidPaddedNumberAsZero() {
        ByteBuffer allocate = ByteBuffer.allocate(50);
        BinaryUtils.copy(allocate, "    FOO").flip();
        Assert.assertEquals(0, TextUtils.parseNumber(allocate));
    }

    @Test
    public void testParseHHMM() {
        Assert.assertEquals(LocalTime.of(9, 30), TextUtils.parseHHMM("09:30"));
        Assert.assertEquals(LocalTime.of(13, 30), TextUtils.parseHHMM("13:30"));
    }
}
