package com.core.match.drops.gui;

import com.core.match.drops.gui.msgs.GUIStatus;
import com.core.match.msgs.MatchConstants;
import com.core.util.BinaryUtils;
import com.core.util.TimeUtils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 2/19/16.
 */
public class GUIStatusTest {
    @Test
    public void testInitialIsUnknown() {
        GUIStatus e = new GUIStatus((short)10);
        Assert.assertEquals(0, e.getEvent());
    }

    @Test
    public void testOpen() {
        GUIStatus e = new GUIStatus((short)10);
        e.setEvent(MatchConstants.SystemEvent.Open);
        Assert.assertEquals('O', e.getEvent());
    }

    @Test
    public void testClose() {
        GUIStatus e = new GUIStatus((short)10);
        e.setEvent(MatchConstants.SystemEvent.Close);
        Assert.assertEquals('C', e.getEvent());
    }

    @Test
    public void testJSON() {
        GUIStatus e = new GUIStatus((short)10);
        e.setEvent(MatchConstants.SystemEvent.Open);
        e.setTime(TimeUtils.NANOS_PER_SECOND);
        e.setVer(3);

        ByteBuffer buf = ByteBuffer.allocate(1000);
        buf.clear();
        e.write(buf, "20160223AA", "FOO01");
        buf.flip();
        String s = BinaryUtils.toString(buf);


        Assert.assertEquals("{\"type\":\"status\",\"event\":\"Open\",\"ses\":\"20160223AA\",\"contrib\":\"FOO01\",\"id\":10,\"ver\":3,\"time\":1000}\n", s);
    }
}
