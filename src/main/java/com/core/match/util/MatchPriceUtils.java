package com.core.match.util;

import com.core.match.msgs.MatchConstants;

/**
 * Created by jgreco on 7/6/15.
 */
public class MatchPriceUtils {
    public static String to32ndPrice(long price) {
        return com.core.util.PriceUtils.to32ndPrice(price, MatchConstants.IMPLIED_DECIMALS);
    }

    public static long toLong(double price) {
        return com.core.util.PriceUtils.toLong(price, MatchConstants.IMPLIED_DECIMALS);
    }

    public static double toDouble(long price) {
        return com.core.util.PriceUtils.toDouble(price, MatchConstants.IMPLIED_DECIMALS);
    }

    public static long getPriceMultiplier() {
        return com.core.util.PriceUtils.getPriceMultiplier(MatchConstants.IMPLIED_DECIMALS);
    }

    public static double toQtyRoundLot(int qty) {
        return com.core.util.PriceUtils.toQtyRoundLot(qty, MatchConstants.QTY_MULTIPLIER);
    }

    public static int roundLotToCoreQty(int qty) {
        return qty * (1000000 / MatchConstants.QTY_MULTIPLIER);
    }

    public static long coreToNotionalLongQty(long qty) {
        return qty * MatchConstants.QTY_MULTIPLIER ;
    }

    public static long roundPrice(long price, long unit, boolean up) {
        if (!up) return (price / unit) * unit;
        return (price % unit > 0) ? (price / unit) * unit + unit : price;
    }

    public static long roundHalf(long price, boolean buy) {
        return roundPrice(price, com.core.util.PriceUtils.getPlus(MatchConstants.IMPLIED_DECIMALS), !buy);
    }

    public static long roundQuarter(long price, boolean buy) {
        return roundPrice(price, com.core.util.PriceUtils.getQuarter(MatchConstants.IMPLIED_DECIMALS), !buy);
    }

    public static long roundEighth(long price, boolean buy) {
        return roundPrice(price, com.core.util.PriceUtils.getEighth(MatchConstants.IMPLIED_DECIMALS), !buy);
    }
}
