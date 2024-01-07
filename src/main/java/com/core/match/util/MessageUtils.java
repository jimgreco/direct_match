package com.core.match.util;

import com.core.match.msgs.MatchConstants;

import java.nio.ByteBuffer;
import java.time.ZoneId;

/**
 * Created by jgreco on 7/6/15.
 */
public class MessageUtils {
    //
    // Price
    //

    public static String to32ndPrice(ByteBuffer buffer, int offset) {
        long price = com.core.util.MessageUtils.getLong(buffer, offset);
        return MatchPriceUtils.to32ndPrice(price);
    }

    public static double getDoublePrice(ByteBuffer buffer, int offset) {
        long price = com.core.util.MessageUtils.getLong(buffer, offset);
        return com.core.util.PriceUtils.toDouble(price, MatchConstants.IMPLIED_DECIMALS);
    }

    public static void setDoublePrice(ByteBuffer buffer, int offset, double value) {
        long price = com.core.util.PriceUtils.toLong(value, MatchConstants.IMPLIED_DECIMALS);
        buffer.putLong(buffer.position() + offset, price);
    }

    //
    // Qty
    //

    public static double toQtyRoundLot(ByteBuffer buffer, int offset) {
        int qty = com.core.util.MessageUtils.getInt(buffer, offset);
        return com.core.util.PriceUtils.toQtyRoundLot(qty, MatchConstants.QTY_MULTIPLIER);
    }

    public static double toExternalQtyRoundLot(ByteBuffer buffer, int offset) {
        int qty = com.core.util.MessageUtils.getInt(buffer, offset);
        return com.core.util.PriceUtils.toQtyRoundLot(qty, 1);
    }

    public static ZoneId zoneID() {
        return ZoneId.of(MatchConstants.TIME_ZONE);
    }
}
