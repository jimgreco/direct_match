package com.core.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;


/**
 * User: jgreco
 */
public class TimeUtils {
	public static final int MILLIS_PER_SECOND = 1000;
    public static final long NANOS_PER_MICRO = 1000;
    public static final long NANOS_PER_MILLI = 1000 * NANOS_PER_MICRO;
    public static final long NANOS_PER_SECOND = 1000 * NANOS_PER_MILLI;

    public static final int MONTHS_PER_YEAR = 12;
	private static final int MIDDLE_DATE_OF_MONTH = 15;

    public static LocalDate subtractMonthsMaintainRelativeDate( LocalDate localDate, long months )
    {
    	LocalDate toReturn = localDate.minusMonths(months);
    	if( toReturn.getDayOfMonth() == TimeUtils.MIDDLE_DATE_OF_MONTH) {
            return toReturn;
        }
    	return toReturn.plusDays(toReturn.getMonth().length(Year.isLeap(toReturn.getYear())) - toReturn.getDayOfMonth());
    }

    public static int toDateInt(LocalDate localDate)
    {
    	//10k * year + 100 * month + day
    	return 100 * ( localDate.getYear() * 100 + localDate.getMonthValue() ) + localDate.getDayOfMonth(); 
    }
    
    public static LocalDate toLocalDate(int yyyymmdd)
    {
    	int year = yyyymmdd / 10000;
		int day = yyyymmdd % 100;
		int month = yyyymmdd / 100 % 100;
		return LocalDate.of(year, month, day);
    }

    // TODO: this seems weird
    public static boolean isValidDate(int yyyymmdd) {
        try {
            toLocalDate(yyyymmdd);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static LocalDateTime toLocalDateTime(long nanos, ZoneId zoneID) {
        Instant instant = Instant.ofEpochSecond(nanos / NANOS_PER_SECOND, nanos % NANOS_PER_SECOND);
        return LocalDateTime.ofInstant(instant, zoneID);
    }

    public static long toNanos(LocalDate date, LocalTime time, ZoneId zoneID) {
        ZonedDateTime zonedDateTime = time.atDate(date).atZone(zoneID);
        return zonedDateTime.toEpochSecond() * TimeUtils.NANOS_PER_SECOND;
    }

    public static long toNanos(LocalDate date, ZoneId zoneID) {
        return date.atStartOfDay().atZone(zoneID).toInstant().getEpochSecond() * TimeUtils.NANOS_PER_SECOND;
    }
}
