package com.core.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jgreco on 7/18/15.
 */
@SuppressWarnings("static-method")

public class TimeCacherTest {
    @Test
    public void testUTCDate() {
        TimeCacher timeCache = TimeCacher.fromUTC();

        // 2015-07-17 23:30 EST (next day UTC)
        Assert.assertEquals(20150718, timeCache.getDateYYYYMMDD(1437190200 * TimeUtils.NANOS_PER_SECOND));

        // 2015-07-18 0:30 EST
        Assert.assertEquals(20150718, timeCache.getDateYYYYMMDD(1437193800 * TimeUtils.NANOS_PER_SECOND));
    }

	@Test
    public void testUTCNanosSinceMidnight() {
        TimeCacher timeCache = TimeCacher.fromUTC();

        Assert.assertEquals(30 * 60 * TimeUtils.NANOS_PER_SECOND,
                timeCache.getNanosSinceMidnight(1437179400 * TimeUtils.NANOS_PER_SECOND));
    }
}
