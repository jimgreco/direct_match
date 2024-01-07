package com.core.util;

import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * User: jgreco
 */
public class PriceUtils {
    public static final long[] PRICE_MULTIPLIERS = new long[] {
            1,
            10,
            100,
            1000,
            10000,
            100000,
            1000000,
            10000000,
            100000000,
            1000000000,
            10000000000L,
            100000000000L,
            1000000000000L,
            10000000000000L,
            100000000000000L,
            1000000000000000L,
            10000000000000000L,
            100000000000000000L,
    };

    public static int getStrSize(long num) {
        int size = 0;
        while (num > 0) {
            size++;
            num /= 10;
        }
        return Math.max(1, size);
    }

    public static void writePrice(ByteBuffer buffer, long price, int impliedDecimals) {
        long whole = price / PRICE_MULTIPLIERS[impliedDecimals];
        long decimal = price - (whole * PRICE_MULTIPLIERS[impliedDecimals]);

        // write whole number
        TextUtils.writeNumber(buffer, whole);

        // decimal place
        buffer.put((byte)'.');
        int startDec = buffer.position();

        // pad decimal with zeros
        int decimalSize = getStrSize(decimal);
        int zeroPadding = impliedDecimals - decimalSize;
        while (zeroPadding-- > 0) {
            buffer.put((byte) '0');
        }

        // get rid of extra zeros
        while (decimal % 10 == 0 && decimal != 0) {
            decimal /= 10;
        }

        // write out actual decimal
        TextUtils.writeNumber(buffer, decimal);

        // add trailing zeros for a price
        if (buffer.position() - startDec < 2) {
            buffer.put((byte) '0');
        }
    }

    public static String writePrice(long price, int impliedDecimals) {
        long whole = price / PRICE_MULTIPLIERS[impliedDecimals];
        long decimal = price - (whole * PRICE_MULTIPLIERS[impliedDecimals]);

        StringBuilder builder = new StringBuilder();
        builder.append(whole);

        // decimal place
        builder.append('.');

        // pad decimal with zeros
        int decimalSize = getStrSize(decimal);
        int zeroPadding = impliedDecimals - decimalSize;
        while (zeroPadding-- > 0) {
            builder.append('0');
        }

        builder.append(decimal);
        return builder.toString();
    }

    public static long parsePrice(ByteBuffer buffer, int impliedDecimals) {
        if (!buffer.hasRemaining()) {
            return 0;
        }

        int position = buffer.position();

        int multiplier = 1;
        byte first = buffer.get(buffer.position());
        if (first == '-') {
            multiplier = -1;
            buffer.get();
        }

        int wholeNumber = 0;
        int decimalNumber = 0;
        int numDecimals = 0;
        boolean doingDecimal = false;

        while(buffer.hasRemaining()) {
            if (numDecimals >= impliedDecimals) {
                break;
            }

            byte b = buffer.get();

            if (b == '.') {
                // can't have two decimals
                if (doingDecimal) {
                    return 0;
                }

                doingDecimal = true;
                continue;
            }

            if (doingDecimal) {
                decimalNumber *= 10;
            }
            else {
                wholeNumber *= 10;
            }

            if (b < '0' || b > '9') {
                // illegal character
                buffer.reset();
                return 0;
            }

            int numVal = b - '0';

            if (doingDecimal) {
                numDecimals++;
                decimalNumber += numVal;
            }
            else {
                wholeNumber += numVal;
            }
        }

        buffer.position(position);

        long wholeMult = PRICE_MULTIPLIERS[impliedDecimals];
        long decimalMult = PRICE_MULTIPLIERS[impliedDecimals - numDecimals];
        return multiplier * wholeNumber * wholeMult + (decimalMult * decimalNumber);
    }

    public static double toDouble(long price, int impliedDecimals) {
        return 1.0 * price / PRICE_MULTIPLIERS[impliedDecimals];
    }

    public static long toLong(double v, int impliedDecimals) {
        if(v < 0){
            return (long) ((v * PRICE_MULTIPLIERS[impliedDecimals])- 0.5);

        }
        return (long) ((v * PRICE_MULTIPLIERS[impliedDecimals]) + 0.5);
    }



    private static final String[] QUARTER_FRACTION_DISPLAY = new String[] {
            "0", "2", "+", "6"
    };

    @SuppressWarnings("unused")
    private static final String[] EIGHTH_FRACTION_DISPLAY = new String[] {
            "0", "1", "2", "3", "+", "5", "6", "7"
    };

    @SuppressWarnings("unused")
    private static final String[] SIXTEENTH_FRACTION_DISPLAY = new String[] {
            "0", "0.5", "1", "1.5", "2", "2.5", "3", "3.5", "+", "4.5", "5", "5.5", "6", "6.5", "7", "7.5"
    };

    private static final String[] THIRTY_SECOND_DISPLAY = new String[] {
            "-00", "-01", "-02", "-03", "-04", "-05", "-06", "-07", "-08", "-09",
            "-10", "-11", "-12", "-13", "-14", "-15", "-16", "-17", "-18", "-19",
            "-20", "-21", "-22", "-23", "-24", "-25", "-26", "-27", "-28", "-29",
            "-30", "-31",
    };

    public static long getPlus(int impliedDecimals) {
        return PRICE_MULTIPLIERS[impliedDecimals] / 32 / 2;
    }

    public static long getQuarter(int impliedDecimals) {
        return PRICE_MULTIPLIERS[impliedDecimals] / 32 / 4;
    }

    public static long getEighth(int impliedDecimals) {
        return PRICE_MULTIPLIERS[impliedDecimals] / 32 / 8;
    }

    public static long getSixteenth(int impliedDecimals) {
        return PRICE_MULTIPLIERS[impliedDecimals] / 32 / 16;
    }

    public static void to32ndPrice(StringBuilder builder, long price, int impliedDecimals) {
        long onePercent = PRICE_MULTIPLIERS[impliedDecimals];
        long oneThirtySecond = onePercent / 32;
        long quarterThirtySecond = oneThirtySecond / 4;

        if (price % quarterThirtySecond != 0) {
            builder.append(toDouble(price, impliedDecimals));
            return;
        }

        long handle = price / onePercent;
        long dec = price - handle * onePercent;
        int thirtySecond = (int) (dec / oneThirtySecond);
        dec -= oneThirtySecond * thirtySecond;
        int fraction = (int)(dec / quarterThirtySecond);

        builder.append(handle);
        builder.append(THIRTY_SECOND_DISPLAY[thirtySecond]);
        builder.append(QUARTER_FRACTION_DISPLAY[fraction]);
    }

    public static String to32ndPrice(long price, int impliedDecimals) {
        long onePercent = PRICE_MULTIPLIERS[impliedDecimals];
        long oneThirtySecond = onePercent / 32;
        long handle = price / onePercent;
        long dec = price - handle * onePercent;
        int thirtySecond = (int) (dec / oneThirtySecond);
        dec -= oneThirtySecond * thirtySecond;
        //To handle prices for spreads
        if(thirtySecond >= 0) {
            String result = handle + THIRTY_SECOND_DISPLAY[thirtySecond];
            if (impliedDecimals == 7) {
                long quarterThirtySecond = oneThirtySecond / 4;

                if (price % quarterThirtySecond != 0) {
                    return Double.toString(com.core.util.PriceUtils.toDouble(price, impliedDecimals));
                }

                int fraction = (int) (dec / quarterThirtySecond);
                return result + QUARTER_FRACTION_DISPLAY[fraction];
            } else if (impliedDecimals == 8) {
                long eighthThirtySecond = oneThirtySecond / 8;

                if (price % eighthThirtySecond != 0) {
                    return Double.toString(com.core.util.PriceUtils.toDouble(price, impliedDecimals));
                }

                int fraction = (int) (dec / eighthThirtySecond);
                return result + EIGHTH_FRACTION_DISPLAY[fraction];
            } else if (impliedDecimals == 9) {
                long sixteenthThirtySecond = oneThirtySecond / 16;

                if (price % sixteenthThirtySecond != 0) {
                    return Double.toString(com.core.util.PriceUtils.toDouble(price, impliedDecimals));
                }

                int fraction = (int) (dec / sixteenthThirtySecond);
                return result + SIXTEENTH_FRACTION_DISPLAY[fraction];
            }
        }
        return Double.toString(com.core.util.PriceUtils.toDouble(price, impliedDecimals));
    }

    public static long getPriceMultiplier(int impliedDecimals) {
        if (impliedDecimals < 0 || impliedDecimals >= PRICE_MULTIPLIERS.length) {
            return 0;
        }

        return PRICE_MULTIPLIERS[impliedDecimals];
    }

    public static double toQtyRoundLot(int qty, int qtyMultiplier) {
        return 1.0 * qty * qtyMultiplier / PRICE_MULTIPLIERS[6];
    }

    public static String toCommas(int value) {
        return NumberFormat.getNumberInstance(Locale.US).format(value);
    }
}
