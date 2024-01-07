package com.core.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Created by jgreco on 7/18/15.
 */
public class TimeCacher {
    private final ZoneId zoneID;
    private long startOfDayNanos;
    private long endOfDayNanos;
    private int dateYYYYMMDD;

    public static TimeCacher fromUTC() {
        return new TimeCacher(ZoneId.of("UTC"));
    }

    private TimeCacher(ZoneId zoneID) {
        this.zoneID = zoneID;
    }

    void checkCache(long time) {
        if (time >= startOfDayNanos && time < endOfDayNanos) {
            return;
        }

        LocalDate startDate = Instant.ofEpochSecond(time / TimeUtils.NANOS_PER_SECOND).atZone(zoneID).toLocalDate();
        LocalDate endDate = startDate.plusDays(1);

        dateYYYYMMDD = Integer.parseInt(startDate.format(DateTimeFormatter.BASIC_ISO_DATE));
        startOfDayNanos = TimeUtils.toNanos(startDate, zoneID);
        endOfDayNanos = TimeUtils.toNanos(endDate, zoneID);
    }

    public int getDateYYYYMMDD(long nanos) {
        checkCache(nanos);
        return dateYYYYMMDD;
    }

    public long getNanosSinceMidnight(long nanos) {
        checkCache(nanos);
        return (nanos - startOfDayNanos);
    }

    public long getMidnightNanos(long nanos) {
        checkCache(nanos);
        return startOfDayNanos;
    }
}
