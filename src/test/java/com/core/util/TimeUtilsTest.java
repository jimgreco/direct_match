package com.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Created by jgreco on 7/18/15.
 */
@SuppressWarnings("static-method")
public class TimeUtilsTest {
	@Test
    public void testYYYYMMDDToLocalDate() {
        Assert.assertEquals(LocalDate.of(2015, 12, 15), TimeUtils.toLocalDate(20151215));
    }

    @Test
    public void testLocalDateToYYYYMMDD() {
        Assert.assertEquals(20151215, TimeUtils.toDateInt(LocalDate.of(2015, 12, 15)));
    }

    @Test
    public void testValidDate() {
        Assert.assertTrue(TimeUtils.isValidDate(20150930));
    }

    @Test
    public void testInvalidDate() {
        Assert.assertFalse(TimeUtils.isValidDate(20150931));
    }


    @Test
    public void testRelativeDateMidMonth() {

        Assert.assertEquals(LocalDate.of(2014, 11, 15), TimeUtils.subtractMonthsMaintainRelativeDate(LocalDate.of(2015, 5, 15), 6));
        Assert.assertEquals(LocalDate.of(2014, 5, 15), TimeUtils.subtractMonthsMaintainRelativeDate(LocalDate.of(2014, 11, 15), 6));
    }

    @Test
    public void testRelativeDateEndOfMonth() {
        Assert.assertEquals(LocalDate.of(2015, 5, 31), TimeUtils.subtractMonthsMaintainRelativeDate(LocalDate.of(2015, 11, 30), 6));
    }

    @Test
    public void testRelativeDateEndOfMonthLeapYear() {
        Assert.assertEquals(LocalDate.of(2012, 2, 29), TimeUtils.subtractMonthsMaintainRelativeDate(LocalDate.of(2012, 8, 31), 6));
    }

    @Test
    public void testToLocalDateTime() {
        Assert.assertEquals(LocalDateTime.of(2015, 7, 18, 22, 20, 32), TimeUtils.toLocalDateTime(1437272432 * TimeUtils.NANOS_PER_SECOND, com.core.match.util.MessageUtils.zoneID()));
    }

    @Test
    public void testGetNanosFromDateAndTime() {
        Assert.assertEquals(1437272432 * TimeUtils.NANOS_PER_SECOND, TimeUtils.toNanos(LocalDate.of(2015, 7, 18), LocalTime.of(22, 20, 32), com.core.match.util.MessageUtils.zoneID()));
    }
}
