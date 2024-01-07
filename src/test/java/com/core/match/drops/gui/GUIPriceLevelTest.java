package com.core.match.drops.gui;

import com.core.match.drops.gui.msgs.GUIPrice;
import com.core.match.msgs.MatchConstants;
import com.core.match.services.security.Bond;
import com.core.util.BinaryUtils;
import com.core.util.TimeUtils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 2/19/16.
 */
public class GUIPriceLevelTest {
    @Test
    public void testPriceLevel() {
        GUIPrice priceLevel = new GUIPrice(1, "10Y", true, 0);
        Assert.assertEquals(0, priceLevel.getQty());
        Assert.assertEquals(0, priceLevel.getPx());
        Assert.assertEquals(1, priceLevel.getId());
        Assert.assertEquals("10Y", priceLevel.getSec());

        updateQuote(priceLevel, 1000, 10, 100, 10, 1);
        Assert.assertEquals(10, priceLevel.getQty());
        Assert.assertEquals(100, priceLevel.getPx());
        Assert.assertEquals(1, priceLevel.getId());
        Assert.assertEquals(10, priceLevel.getOrd());
        Assert.assertEquals(1, priceLevel.getIord());
        Assert.assertEquals("10Y", priceLevel.getSec());

        updateQuote(priceLevel, 1001, 10, 101, 1, 0);
        updateQuote(priceLevel, 1002, 20, 102, 1, 0);
        updateQuote(priceLevel, 1003, 30, 103, 1, 0);

        Assert.assertEquals(30, priceLevel.getQty());
        Assert.assertEquals(103, priceLevel.getPx());
        Assert.assertEquals(1, priceLevel.getId());
        Assert.assertEquals("10Y", priceLevel.getSec());
    }

    private void updateQuote(GUIPrice level, long timestamp, int qty, int price, int orders, int insideOrders) {
        level.setTime(timestamp);
        level.setQty(qty);
        level.setPx(price);
        level.setOrd(orders);
        level.setIord(insideOrders);
    }

    @Test
    public void testJSON() {
        GUIPrice priceLevel = new GUIPrice(1, "10Y", false, 2);
        updateQuote(priceLevel, 1000 * TimeUtils.NANOS_PER_SECOND, 10, 100, 10, 3);
        priceLevel.setVer(3);

        ByteBuffer buf = ByteBuffer.allocate(1000);
        buf.clear();
        priceLevel.write(buf, "20160222AA", "FOO01");
        buf.flip();
        String s = BinaryUtils.toString(buf);

        Assert.assertEquals("{\"type\":\"price\",\"sec\":\"10Y\",\"side\":\"ask\",\"pos\":2,\"px\":100,\"qty\":10,\"ord\":10,\"iord\":3,\"ses\":\"20160222AA\",\"contrib\":\"FOO01\",\"id\":1,\"ver\":3,\"time\":1000000}\n", s);
    }

    @Test
    public void write_largeQty_willNotCauseINtegerOverflow() {
        GUIPrice priceLevel = new GUIPrice(1, "10Y", false, 2);
        updateQuote(priceLevel, 1000 * TimeUtils.NANOS_PER_SECOND, 10, 100, 10, 3);
        priceLevel.setVer(3);
        priceLevel.setQty(Integer.MAX_VALUE/MatchConstants.QTY_MULTIPLIER +4);
        ByteBuffer buf = ByteBuffer.allocate(1000);
        buf.clear();
        priceLevel.write(buf, "20160222AA", "FOO01");
        buf.flip();
        String s = BinaryUtils.toString(buf);

        Assert.assertEquals("{\"type\":\"price\",\"sec\":\"10Y\",\"side\":\"ask\",\"pos\":2,\"px\":100,\"qty\":2151,\"ord\":10,\"iord\":3,\"ses\":\"20160222AA\",\"contrib\":\"FOO01\",\"id\":1,\"ver\":3,\"time\":1000000}\n", s);
    }
}
