package com.core.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Created by jgreco on 5/17/15.
 */
public class TradeDateUtilsTest {
    private TradeDateUtils tradeDateUtils;

    @Before
    public void before() {
        tradeDateUtils = new TradeDateUtils(ZoneId.of("America/New_York"), "18:15");
    }

    @Test
    public void testTradeDateFromNanos() {
        Assert.assertEquals(LocalDate.of(2015, 7, 19), tradeDateUtils.getTradeDate(1437336133 * TimeUtils.NANOS_PER_SECOND));
    }

    @Test
    public void testTradeDateFromNanosNextDay() {
        Assert.assertEquals(LocalDate.of(2015, 7, 20), tradeDateUtils.getTradeDate(1437344533 * TimeUtils.NANOS_PER_SECOND));
    }
}
