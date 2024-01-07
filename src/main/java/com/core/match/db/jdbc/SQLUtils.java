package com.core.match.db.jdbc;

import com.core.util.BinaryUtils;

import java.nio.ByteBuffer;
import java.sql.Date;
import java.time.LocalDate;

/**
 * Created by hli on 10/1/15.
 */
public class SQLUtils {
    public static java.sql.Date localdateToDate(LocalDate ld) {
        if(ld==null) return null;
        return Date.valueOf(ld.toString());

    }

    public static String getClOrdIdAsString(ByteBuffer clOrdId) {
        String clOrdIdAsString = BinaryUtils.toString(clOrdId);
        return (clOrdId != null && clOrdId.remaining() >= 8 && !isValidClOrdIdString(clOrdIdAsString)) ? String.valueOf(clOrdId.getLong()) : clOrdIdAsString;
    }

    public static boolean isValidClOrdIdString(String clOrdIdAsString) {
        return clOrdIdAsString.chars().allMatch(Character::isLetterOrDigit);
    }
}
