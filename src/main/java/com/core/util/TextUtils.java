package com.core.util;

import java.nio.ByteBuffer;
import java.time.LocalTime;

/**
 * User: jgreco
 */
public class TextUtils {
    private static final byte[] TRUE = "true".getBytes();
    private static final byte[] FALSE = "false".getBytes();
    private static final TimeCacher timeCache = TimeCacher.fromUTC();

    public static int stringSize(long num) {
        int size = 1;
        if (num < 0) {
            num = -num;
            size++;
        }

        while(num >= 10) {
            size++;
            num /= 10;
        }
        return size;
    }

    public static ByteBuffer writeNumber(ByteBuffer buffer, long num) {
        if (num < 0) {
            buffer.put((byte)'-');
            num = -num;
        }

        int size = stringSize(num);
        for (int i=size-1; i>=0; i--) {
            byte digit = (byte)(num % 10);
            num /= 10;
            buffer.put(buffer.position() + i, (byte) (digit + '0'));
        }

        buffer.position(buffer.position() + size);
        return buffer;
    }

    public static int writeNumber(byte[] buffer, int offset, long num) {
        int initialOffset = offset;

        if (num < 0) {
            buffer[offset++] = (byte)'-';
            num = -num;
        }

        int size = stringSize(num);
        for (int i=size-1; i>=0; i--) {
            byte digit = (byte)(num % 10);
            num /= 10;
            buffer[offset + i] = (byte) (digit + '0');
        }
        offset += size;

        return offset - initialOffset;
    }

    public static ByteBuffer writeStringLeftPadded(ByteBuffer buffer, String str, int length, char padCharacter) {
        int size = str.length();
        int zeros = Math.max(0, length - size);

        for (int i=0; i<zeros; i++) {
            buffer.put((byte) padCharacter);
        }

        BinaryUtils.copy(buffer, str);
        return buffer;
    }

    public static ByteBuffer writeNumberLeftPadded(ByteBuffer buffer, long num, int length, char padCharacter) {
        int size = stringSize(num);
        int zeros = Math.max(0, length - size);

        // pad zeros
        for (int i=0; i<zeros; i++) {
            buffer.put((byte)padCharacter);
        }

        writeNumber(buffer, num);
        return buffer;
    }

    public static int writeNumberLeftPadded(byte[] buffer, int offset, long num, int length, char padCharacter) {
        int initialOffset = offset;
        int size = stringSize(num);
        int zeros = Math.max(0, length - size);

        // pad zeros
        for (int i=0; i<zeros; i++) {
            buffer[offset++] = (byte)padCharacter;
        }

        offset += writeNumber(buffer, offset, num);
        return offset - initialOffset;
    }

    public static void writeNumberLeftPadded(StringBuilder builder, long num, int length, char padCharacter) {
        int size = stringSize(num);
        int zeros = Math.max(0, length - size);

        // pad zeros
        for (int i=0; i<zeros; i++) {
            builder.append(padCharacter);
        }

        builder.append(num);
    }

    public static void writeBool(ByteBuffer buf, boolean buy) {
        buf.put(buy ? TRUE : FALSE);
    }

    public static void writeShort(ByteBuffer buf, short val) {
        buf.putShort(val);
    }

    public static int writeBool(byte[] buf, int offset, boolean buy) {
        byte[] arr = buy ? TRUE : FALSE;
        System.arraycopy(arr, 0, buf, offset, arr.length);
        return arr.length;
    }

    public static void writeDateUTC(ByteBuffer buffer, long date) {
        int dt = timeCache.getDateYYYYMMDD(date);
        writeNumber(buffer, dt);
    }

    public static void writeDateUTC(byte[] buffer, int offset, long date) {
        int dt = timeCache.getDateYYYYMMDD(date);
        writeNumber(buffer, offset, dt);
    }

    public static void writeTimeUTC(ByteBuffer buffer, long time) {
        long nanos = timeCache.getNanosSinceMidnight(time);
        long timeSinceMidnight = nanos / TimeUtils.NANOS_PER_MILLI;
        long millis = timeSinceMidnight % 1000;
        timeSinceMidnight /= 1000;
        long seconds = timeSinceMidnight % 60;
        timeSinceMidnight /= 60;
        long minutes = timeSinceMidnight % 60;
        timeSinceMidnight /= 60;
        long hours = timeSinceMidnight % 24;

        writeNumberLeftPadded(buffer, hours, 2, '0');
        buffer.put((byte)':');
        writeNumberLeftPadded(buffer, minutes, 2, '0');
        buffer.put((byte)':');
        writeNumberLeftPadded(buffer, seconds, 2, '0');
        buffer.put((byte)'.');
        writeNumberLeftPadded(buffer, millis, 3, '0');
    }

    public static void writeDateTimeUTC(ByteBuffer buffer, long time) {
        writeDateUTC(buffer, time);
        buffer.put((byte)'-');
        writeTimeUTC(buffer, time);
    }

    public static int writeTimeUTC(byte[] buffer, int offset, long time) {
        long nanos = timeCache.getNanosSinceMidnight(time);
        long timeSinceMidnight = nanos / TimeUtils.NANOS_PER_MILLI;
        long millis = timeSinceMidnight % 1000;
        timeSinceMidnight /= 1000;
        long seconds = timeSinceMidnight % 60;
        timeSinceMidnight /= 60;
        long minutes = timeSinceMidnight % 60;
        timeSinceMidnight /= 60;
        long hours = timeSinceMidnight % 24;

        int initialOffset = offset;
        offset += writeNumberLeftPadded(buffer, offset, hours, 2, '0');
        buffer[offset++] = ':';
        offset += writeNumberLeftPadded(buffer, offset, minutes, 2, '0');
        buffer[offset++] = ':';
        offset += writeNumberLeftPadded(buffer, offset, seconds, 2, '0');
        buffer[offset++] = '.';
        offset += writeNumberLeftPadded(buffer, offset, millis, 3, '0');

        return offset - initialOffset;
    }

    public static int parseNumberLeftPadded(ByteBuffer buffer) {
        for (int i=buffer.position(); i<buffer.limit(); i++) {
            byte c = buffer.get(i);
            if (c == '-' || (c >= '0' && c <= '9' )) {
                int position = buffer.position();
                buffer.position(i);
                int i1 = parseNumber(buffer);
                buffer.position(position);
                return i1;
            }
        }
        return 0;
    }

    public static int parseNumber(ByteBuffer buffer) {
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

        int num = 0;
        while(buffer.hasRemaining()) {
            byte b = buffer.get();

            if (b == '.') {
                return num;
            }
            else if (b < '0' || b > '9') {
                buffer.position(position);
                return 0;
            }

            num *= 10;
            num += (b - '0');
        }

        buffer.position(position);
        return multiplier * num;
    }

    public static LocalTime parseHHMM(String HHMM) {
        String[] split = HHMM.split(":");
        return LocalTime.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    public static long parseHHMMSSsss(ByteBuffer value, long timestamp) {
        // format = 20:54:54.118
        if (value.remaining() < 12) {
            return 0;
        }

        long midnight = timeCache.getMidnightNanos(timestamp);

        int hours = 10 * charToInt(value.get()) + charToInt(value.get());
        value.get();
        int minutes = 10 * charToInt(value.get()) + charToInt(value.get());
        value.get();
        int seconds = 10 * charToInt(value.get()) + charToInt(value.get());
        value.get();
        int millis = 100 * charToInt(value.get()) + 10 * charToInt(value.get()) + charToInt(value.get());

        return midnight + (60 * 60 * 1000 * hours + 60 * 1000 * minutes + 1000 * seconds + millis) * TimeUtils.NANOS_PER_MILLI;
    }

    public static long midnightNanos(long timestamp) {
        return timeCache.getMidnightNanos(timestamp);
    }

    private static int charToInt(byte c) {
        if (c < '0' || c > '9') {
            return 0;
        }
        return c - '0';
    }
}
