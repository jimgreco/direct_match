package com.core.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Created by jgreco on 2/8/15.
 */
public class TradeDateUtils {
    private final ZoneId timeZone;
    private final LocalTime sessionRollover;

    public TradeDateUtils(ZoneId timeZone, String tradeDateRolloverHHMM) {
        this.timeZone = timeZone;
        this.sessionRollover = TextUtils.parseHHMM(tradeDateRolloverHHMM);
    }

    public LocalDate getTradeDate(long nanos) {
        LocalDateTime localDateTime = TimeUtils.toLocalDateTime(nanos, timeZone);
        return getTradeDate(localDateTime);
    }

    LocalDate getTradeDate(LocalDateTime time) {
        if (time.toLocalTime().compareTo(sessionRollover) >= 0) {
            time = time.plusDays(1);
        }
        return time.toLocalDate();
    }
}
