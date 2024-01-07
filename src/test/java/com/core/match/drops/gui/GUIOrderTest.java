package com.core.match.drops.gui;

import com.core.match.drops.gui.msgs.GUIOrder;
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
public class GUIOrderTest {
    @Test
    public void testOrder() {
        GUIOrder order = new GUIOrder(1, "10Y", true, 2);
        Assert.assertEquals(0, order.getQty());
        Assert.assertEquals(0, order.getPx());
        Assert.assertEquals(1, order.getId());
        Assert.assertEquals(2, order.getPos());
        Assert.assertEquals("10Y", order.getSec());

        order.setTime(1000);
        order.setOrderID(10);
        order.setQty(4);
        order.setPx(100);

        Assert.assertEquals(4, order.getQty());
        Assert.assertEquals(100, order.getPx());
        Assert.assertEquals(1, order.getId());
        Assert.assertEquals(2, order.getPos());
        Assert.assertEquals(10, order.getOrderID());
        Assert.assertEquals("10Y", order.getSec());
    }

    @Test
    public void testJSON() {
        GUIOrder order = new GUIOrder(3, "10Y", true, 3);

        order.setTime(1000 *  TimeUtils.NANOS_PER_SECOND);
        order.setOrderID(10);
        order.setQty(4);
        order.setPx(99);
        order.setVer(3);

        ByteBuffer buf = ByteBuffer.allocate(1000);
        buf.clear();
        order.write(buf, "20160223AA", "FOO01");
        buf.flip();
        String s = BinaryUtils.toString(buf);

        Assert.assertEquals("{\"type\":\"order\",\"sec\":\"10Y\",\"side\":\"bid\",\"pos\":3,\"px\":99,\"qty\":4,\"orderID\":10,\"ses\":\"20160223AA\",\"contrib\":\"FOO01\",\"id\":3,\"ver\":3,\"time\":1000000}\n", s);
    }
}
