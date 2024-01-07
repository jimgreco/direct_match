package com.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Created by jgreco on 7/16/15.
 */
@SuppressWarnings("static-method")
public class TextUtilsByteArrayTest {
	@Test
    public void testWriteNumberLeftPadded() {
        byte[] bytes = new byte[3];
        TextUtils.writeNumberLeftPadded(bytes, 0, 215, 3, '0');
        Assert.assertEquals("215", new String(bytes));
    }

    @Test
    public void testWriteNumberLeftPaddedWithZeros() {
        byte[] bytes = new byte[3];
        TextUtils.writeNumberLeftPadded(bytes, 0, 21, 3, '0');
        Assert.assertEquals("021", new String(bytes));
    }

    @Test
    public void testWriteNumberLeftPaddedWithNotFilled() {
        byte[] bytes = new byte[5];
        bytes[0] = ' ';
        bytes[4] = ' ';
        TextUtils.writeNumberLeftPadded(bytes, 1, 21, 3, '0');
        Assert.assertEquals(" 021 ", new String(bytes));
    }

    @Test
    public void testWriteBoolTrue() {
        byte[] bytes = new byte[10];
        TextUtils.writeBool(bytes, 0, true);

        Assert.assertEquals("true", new String(bytes, 0, "true".length()));
    }

    @Test
    public void testWriteBoolFalse() {
        byte[] bytes = new byte[10];
        TextUtils.writeBool(bytes, 0, false);

        Assert.assertEquals("false", new String(bytes, 0, "false".length()));
    }

    @Test
    public void testWriteDate() {
        byte[] date = new byte[10];
        TextUtils.writeDateUTC(date, 1, getNanos(LocalDate.of(2015, 5, 15)));
        Assert.assertEquals("20150515", new String(date, 1, 8));
    }

    public static long getNanos(LocalDate date) {
        return date.atStartOfDay().atZone(ZoneId.of("UTC")).toEpochSecond() * TimeUtils.NANOS_PER_SECOND;
    }

    @Test
    public void testWriteTime() {
        byte[] bytes = new byte[50];
        TextUtils.writeTimeUTC(bytes, 10, 1437227242121L * TimeUtils.NANOS_PER_MILLI);
        String expected = "13:47:22.121";
        Assert.assertEquals(expected, new String(bytes, 10, expected.length()));
    }
}
