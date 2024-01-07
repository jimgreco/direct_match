package com.core.match.drops;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jgreco on 2/20/16.
 */
public class LinearCounterTest {
    @Test
    public void testVersion() {
        LinearCounter counter = new LinearCounter();
        Assert.assertEquals(0, counter.getVersion());

        Assert.assertEquals(1, counter.incVersion());
        Assert.assertEquals(1, counter.getVersion());

        Assert.assertEquals(2, counter.incVersion());
        Assert.assertEquals(2, counter.getVersion());
    }
}
