package com.core.match.drops.gui;

import com.core.match.drops.gui.msgs.GUITrade;
import com.core.match.msgs.MatchConstants;
import com.core.match.services.security.Bond;
import com.core.util.BinaryUtils;
import com.core.util.TimeUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 2/18/16.
 */
public class GUITradeTest {
    private GUITrade row;

    @Before
    public void setup() {
        row = new GUITrade(1, "5Y");

        Assert.assertEquals("5Y", row.getSec());
        Assert.assertEquals(0, row.getTime());
        Assert.assertEquals(0, row.getVol());
        Assert.assertEquals(0, row.getQty());
        Assert.assertEquals(0, row.getPx());
    }

    @Test
    public void testOneTrade() {
        row.setSide(true);
        row.setQty(10);
        row.setVol(10);
        row.setPx(100);

        Assert.assertEquals(100, row.getPx());
        Assert.assertEquals(10, row.getQty());
        Assert.assertEquals(10, row.getVol());
        Assert.assertTrue(row.getSide());
    }

    @Test
    public void testMultipleTradeUpdatesVolume() {
        row.setSide(true);
        row.setQty(10);
        row.setVol(10);
        row.setPx(100);

        row.setSide(true);
        row.setQty(5);
        row.setVol(15);
        row.setPx(100);

        row.setSide(false);
        row.setQty(8);
        row.setVol(23);
        row.setPx(101);

        Assert.assertEquals(101, row.getPx());
        Assert.assertEquals(8, row.getQty());
        Assert.assertEquals(23 , row.getVol());
        Assert.assertFalse(row.getSide());
    }

    @Test
    public void testJSON() {
        row.setTime(6 * TimeUtils.NANOS_PER_SECOND);
        row.setSide(false);
        row.setQty(3);
        row.setVol(3);
        row.setPx(100);

        row.setTime(7 * TimeUtils.NANOS_PER_SECOND);
        row.setSide(true);
        row.setQty(8);
        row.setVol(11);
        row.setPx(101);

        ByteBuffer buf = ByteBuffer.allocate(1000);
        buf.clear();
        row.write(buf, "20160222AA", "FOO01");
        buf.flip();
        String s = BinaryUtils.toString(buf);

        // qty= 8*MatchConstants.QTY_MULTIPLIER= 8 000000  and vol=11* 1000000in test mode
        Assert.assertEquals("{\"type\":\"trade\",\"sec\":\"5Y\",\"side\":\"bid\",\"vol\":11,\"px\":101,\"qty\":8,\"matchId\":0,\"ses\":\"20160222AA\",\"contrib\":\"FOO01\",\"id\":1,\"ver\":0,\"time\":7000}\n", s);
    }

    @Test
    public void write_extremelyLargeVolumeAndQty_willNotCauseOverflow() {


        row.setTime(7 * TimeUtils.NANOS_PER_SECOND);
        row.setSide(true);
        row.setQty(Integer.MAX_VALUE/MatchConstants.QTY_MULTIPLIER +10);
        row.setVol(Integer.MAX_VALUE/MatchConstants.QTY_MULTIPLIER +1);
        row.setPx(101);

        ByteBuffer buf = ByteBuffer.allocate(1000);
        buf.clear();
        row.write(buf, "20160222AA", "FOO01");
        buf.flip();
        String s = BinaryUtils.toString(buf);

        // qty= 8*MatchConstants.QTY_MULTIPLIER= 8 000000  and vol=11* 1000000in test mode
        Assert.assertEquals("{\"type\":\"trade\",\"sec\":\"5Y\",\"side\":\"bid\",\"vol\":2148,\"px\":101,\"qty\":2157,\"matchId\":0,\"ses\":\"20160222AA\",\"contrib\":\"FOO01\",\"id\":1,\"ver\":0,\"time\":7000}\n", s);
    }
}
