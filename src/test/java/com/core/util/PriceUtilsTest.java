package com.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 1/23/15.
 */
public class PriceUtilsTest {
    @SuppressWarnings("static-method")
    @Test
    public void testParsePrice() {
        ByteBuffer wrap = ByteBuffer.wrap("101.5".getBytes());
        long price = PriceUtils.parsePrice(wrap, 9);
        Assert.assertEquals(101500000000L, price);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testWritePrice() {
        ByteBuffer wrap = ByteBuffer.wrap("101.5".getBytes());
        long price = PriceUtils.parsePrice(wrap, 9);
        Assert.assertEquals(101500000000L, price);

        ByteBuffer buf = ByteBuffer.allocate(100);
        PriceUtils.writePrice(buf, 101500000000L, 9);
        buf.flip();

        Assert.assertEquals("101.50", BinaryUtils.toString(buf));
    }


    @SuppressWarnings("static-method")
    @Test
    public void to32ndPrice_priceIsNegativeAndNotIn32ndsForSpread_() {
        long price = PriceUtils.toLong(-1.2, 9);
        Assert.assertEquals(-1200000000L, price);

        String price32Result= PriceUtils.to32ndPrice(price, 9);

        Assert.assertEquals("-1.2", price32Result);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testWritePrice2() {
        ByteBuffer wrap = ByteBuffer.wrap("101.005".getBytes());
        long price = PriceUtils.parsePrice(wrap, 9);
        Assert.assertEquals(101005000000L, price);

        ByteBuffer buf = ByteBuffer.allocate(100);
        PriceUtils.writePrice(buf, 101005000000L, 9);
        buf.flip();

        Assert.assertEquals("101.005", BinaryUtils.toString(buf));
    }

    @SuppressWarnings("static-method")
    @Test
    public void testWritePrice3() {
        ByteBuffer wrap = ByteBuffer.wrap("101.0000001".getBytes());
        long price = PriceUtils.parsePrice(wrap, 9);
        Assert.assertEquals(101000000100L, price);

        ByteBuffer buf = ByteBuffer.allocate(100);
        PriceUtils.writePrice(buf, 101000000100L, 9);
        buf.flip();

        Assert.assertEquals("101.0000001", BinaryUtils.toString(buf));
    }

    @SuppressWarnings("static-method")
    @Test
    public void testWritePrice4() {
        ByteBuffer wrap = ByteBuffer.wrap("0.0021".getBytes());
        long price = PriceUtils.parsePrice(wrap, 9);
        Assert.assertEquals(2100000, price);

        ByteBuffer buf = ByteBuffer.allocate(100);
        PriceUtils.writePrice(buf, 2100000, 9);
        buf.flip();

        Assert.assertEquals("0.0021", BinaryUtils.toString(buf));
    }

    @SuppressWarnings("static-method")
    @Test
    public void toLong_negativePricing_returnCorrectValue() {

        long price= PriceUtils.toLong(-1.2, 9);

        Assert.assertEquals(-1200000000,price);
    }

    @SuppressWarnings("static-method")
    @Test
    public void toLong_positivePricing_returnCorrectValue() {

        long price= PriceUtils.toLong(1.2, 9);

        Assert.assertEquals(1200000000,price);
    }

    @SuppressWarnings("static-method")
    @Test
    public void toLong_zeroPricing_returnCorrectValue() {

        long price= PriceUtils.toLong(0, 9);

        Assert.assertEquals(0,price);
    }
}
