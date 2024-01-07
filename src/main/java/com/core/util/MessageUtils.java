package com.core.util;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by jgreco on 5/17/15.
 */
public class MessageUtils {
    public static final byte NULL_BYTE = (byte)0xFF;
    public static final short NULL_SHORT = (short)0xFFFF;
    public static final int NULL_INT = 0xFFFFFFFF;
    public static final long NULL_LONG = 0xFFFFFFFFFFFFFFFFL;
    private static final byte EMPTY_BYTE = -1;
    private static final byte SPACE_BYTE = 32;


    private static ByteBuffer EMPTY = ByteBuffer.allocate(0);

    //
    // Timestamp
    //

    public static LocalDateTime getDateTime(ByteBuffer buffer, int offset) {
        long timestamp = buffer.getLong(buffer.position() + offset);
        return getDateTime(timestamp);
    }

    public static LocalDateTime getDateTime(long timestamp) {
        // TODO: This is a hack that should be cleaned up
        return TimeUtils.toLocalDateTime(timestamp, com.core.match.util.MessageUtils.zoneID());
    }

    //
    // Date
    //

    public static void setDate(ByteBuffer buffer, int offset, LocalDate time) {
        int timestamp = TimeUtils.toDateInt(time);
        buffer.putInt(offset, timestamp);
    }

    public static LocalDate getDate(ByteBuffer buffer, int offset) {
        int date = buffer.getInt(buffer.position() + offset);
        return getDate(date);
    }

    public static LocalDate getDate(int date) {
        return TimeUtils.toLocalDate(date);
    }

    //
    // Primitives
    //

    public static boolean getBool(ByteBuffer buffer, int offset) {
        return buffer.get(buffer.position() + offset) == 1;
    }

    public static void setBool(ByteBuffer buffer, int offset, boolean value) {
        buffer.put(buffer.position() + offset, (byte) (value ? 1 : 0));
    }

    public static byte getByte(ByteBuffer buffer, int offset) {
        return buffer.get(buffer.position() + offset);
    }

    public static void setByte(ByteBuffer buffer, int offset, byte value) {
        buffer.put(buffer.position() + offset, value);
    }

    public static char getChar(ByteBuffer buffer, int offset) {
        return (char)getByte(buffer, offset);
    }

    public static void setChar(ByteBuffer buffer, int offset, char value) {
        setByte(buffer, offset, (byte) value);
    }

    public static short getShort(ByteBuffer buffer, int offset) {
        return buffer.getShort(buffer.position() + offset);
    }

    public static void setShort(ByteBuffer buffer, int offset, short value) {
        buffer.putShort(buffer.position() + offset, value);
    }

    public static int getInt(ByteBuffer buffer, int offset) {
        return buffer.getInt(buffer.position() + offset);
    }

    public static void setInt(ByteBuffer buffer, int offset, int value) {
        buffer.putInt(buffer.position() + offset, value);
    }

    public static long getLong(ByteBuffer buffer, int offset) {
        return buffer.getLong(buffer.position() + offset);
    }

    public static void setLong(ByteBuffer buffer, int offset, long value) {
        buffer.putLong(buffer.position() + offset, value);
    }

    //
    // Variable string
    //

    public static int setVariableString(ByteBuffer buffer, int fieldOffset, String value, int stringOffset) {
        if (buffer.remaining() < fieldOffset + Short.BYTES) {
            return 0;
        }

        int length = value.length();
        if (stringOffset < fieldOffset + Short.BYTES || buffer.remaining() < stringOffset + Short.BYTES + length) {
            buffer.putShort(buffer.position() + fieldOffset, (short) 0);
            return 0;
        }

        buffer.putShort(buffer.position() + fieldOffset, (short) stringOffset);
        buffer.putShort(buffer.position() + stringOffset, (short) length);
        BinaryUtils.copy(buffer, stringOffset + Short.BYTES, value);
        return Short.BYTES + length;
    }

    public static boolean doesStringExist(ByteBuffer byteBuffer, int offSet, int length){
        for(int i=offSet; i<length+offSet;i++){
            if(byteBuffer.get(i)!=SPACE_BYTE && byteBuffer.get(i)!= EMPTY_BYTE && byteBuffer.get(i)!= NULL_BYTE){
                return true ;
            }
        }
        return false;
    }

    public static int setVariableString(ByteBuffer buffer, int fieldOffset, ByteBuffer value, int stringOffset) {
        if (buffer.remaining() < fieldOffset + Short.BYTES) {
            return 0;
        }

        if (value == null) {
            buffer.putShort(buffer.position() + fieldOffset, (short) 0);
            return 0;
        }

        int length = value.remaining();
        if (stringOffset < fieldOffset + Short.BYTES || buffer.remaining() < stringOffset + Short.BYTES + length) {
            buffer.putShort(buffer.position() + fieldOffset, (short) 0);
            return 0;
        }

        buffer.putShort(buffer.position() + fieldOffset, (short) stringOffset);
        buffer.putShort(buffer.position() + stringOffset, (short) length);

        int oldPosition = buffer.position();
        buffer.position(buffer.position() + stringOffset + Short.BYTES);
        buffer.put(value);
        buffer.position(oldPosition);

        return Short.BYTES + length;
    }

    public static ByteBuffer getVariableString(ByteBuffer buffer, int offset) {
        if (buffer.remaining() < offset) {
            return EMPTY;
        }

        short stringOffset = buffer.getShort(buffer.position() + offset);
        if (stringOffset == NULL_SHORT || stringOffset <= offset || buffer.remaining() < stringOffset + Short.BYTES) {
            return EMPTY;
        }

        short length = buffer.getShort(buffer.position() + stringOffset);
        if (buffer.remaining() < stringOffset + Short.BYTES + length) {
            return EMPTY;
        }

        ByteBuffer slice = buffer.slice();
        slice.position(stringOffset + Short.BYTES);
        slice.limit(stringOffset + Short.BYTES + length);
        return slice;
    }

    public static String toVariableString(ByteBuffer buffer, int offset) {
        return BinaryUtils.toString(getVariableString(buffer, offset));
    }

    public static int getStringLength(ByteBuffer buffer, int offset) {
        if (buffer.remaining() < offset) {
            return 0;
        }

        short stringOffset = buffer.getShort(buffer.position() + offset);
        if (stringOffset == NULL_SHORT || stringOffset <= offset || buffer.remaining() < stringOffset + Short.BYTES) {
            return 0;
        }

        short length = buffer.getShort(buffer.position() + stringOffset);
        if (buffer.remaining() < stringOffset + Short.BYTES + length) {
            return 0;
        }

        return length;
    }

    //
    // hasField
    //

    public static boolean doesFieldExist(ByteBuffer buffer, int offset, int length) {
        switch(length) {
            case 0:
                return true;
            case Byte.BYTES:
                return getByte(buffer, offset) != NULL_BYTE;
            case Short.BYTES:
                return getShort(buffer, offset) != NULL_SHORT;
            case Integer.BYTES:
                return getInt(buffer, offset) != NULL_INT;
            case Long.BYTES:
                return getLong(buffer, offset) != NULL_LONG;
            default:
                return doesStringExist(buffer, offset, length);

        }
    }

    //
    // Right Padded Fixed string
    //

    public static void setRightPaddedFixedString(ByteBuffer dest, int fieldOffset, String value, int length) {
        if (dest.remaining() < fieldOffset + length) {
            return;
        }

        if (value.length() > length) {
            return;
        }

        BinaryUtils.copy(dest, fieldOffset, value);

        for (int i=value.length(); i<length; i++) {
            dest.put(dest.position() + fieldOffset + i, (byte) ' ');
        }
    }

    public static void setRightPaddedFixedString(ByteBuffer dest, int fieldOffset, ByteBuffer value, int length) {
        if (dest.remaining() < fieldOffset + length) {
            return;
        }

        if (value.remaining() > length) {
            return;
        }

        int dstPosition = dest.position();
        dest.position(dstPosition + fieldOffset);

        int srcPosition = value.position();
        dest.put(value);
        value.position(srcPosition);

        dest.position(dstPosition);

        for (int i= value.remaining(); i<length; i++) {
            dest.put(dest.position() + fieldOffset + i, (byte) ' ');
        }
    }

    public static ByteBuffer getRightPaddedFixedString(ByteBuffer buffer, int offset, int length) {
        if (buffer.remaining() < offset + length) {
            return EMPTY;
        }

        ByteBuffer slice = buffer.slice();
        slice.position(offset);
        slice.limit(offset + length);

        for (int i=slice.position(); i<slice.limit(); i++) {
            if (slice.get(i) == ' ') {
                slice.limit(i);
                break;
            }
        }
        return slice;
    }

    public static String toRightPaddedFixedString(ByteBuffer buffer, int offset, int length) {
        ByteBuffer fixedString = getRightPaddedFixedString(buffer, offset, length);
        return BinaryUtils.toString(fixedString);
    }

    //
    // Left Padded Fixed string
    //

    public static void setLeftPaddedFixedString(ByteBuffer dest, int fieldOffset, String value, int length) {
        if (dest.remaining() < fieldOffset + length) {
            return;
        }

        if (value.length() > length) {
            return;
        }

        int padding = length - value.length();
        for (int i=0; i<padding; i++) {
            dest.put(dest.position() + fieldOffset + i, (byte) ' ');
        }
        BinaryUtils.copy(dest, fieldOffset + padding, value);
    }

    public static void setLeftPaddedFixedString(ByteBuffer dest, int fieldOffset, ByteBuffer value, int length) {
        if (dest.remaining() < fieldOffset + length) {
            return;
        }

        if (value.remaining() > length) {
            return;
        }

        int padding = length - value.remaining();
        for (int i=0; i<padding; i++) {
            dest.put(dest.position() + fieldOffset + i, (byte) ' ');
        }
        BinaryUtils.copy(dest, fieldOffset + padding, value);
    }

    public static ByteBuffer getLeftPaddedFixedString(ByteBuffer buffer, int offset, int length) {
        if (buffer.remaining() < offset + length) {
            return EMPTY;
        }

        ByteBuffer slice = buffer.slice();
        slice.position(offset);
        slice.limit(offset + length);

        for (int i=slice.position(); i<slice.limit(); i++) {
            if (slice.get(i) != ' ') {
                slice.position(i);
                break;
            }
        }
        return slice;
    }

    public static String toLeftPaddedFixedString(ByteBuffer buffer, int offset, int length) {
        ByteBuffer fixedString = getLeftPaddedFixedString(buffer, offset, length);
        return BinaryUtils.toString(fixedString);
    }

    //
    // Fixed String
    //

    @SuppressWarnings("unused")
	public static int getFixedStringLength(ByteBuffer buffer, int offset, int length) {
        return length;
    }


    //
    // Variable bytes
    //

    public static int setBytes(ByteBuffer buffer, int offset, ByteBuffer value) {
        int length = value.remaining();
        if (buffer.remaining() < offset + length) {
            return 0;
        }

        BinaryUtils.copy(buffer, offset, value);
        return length;
    }

    @SuppressWarnings("unused")
	public static int getBytesLength(ByteBuffer buffer, int offset, int length) {
        return length;
    }

    public static ByteBuffer getBytes(ByteBuffer buffer, int offset, int length) {
        ByteBuffer slice = buffer.slice();
        slice.position(offset);
        slice.limit(offset + length);
        return slice;
    }

    public static int setBytes(ByteBuffer buffer, int offset, String value) {
        int length = value.length();

        if (buffer.remaining() < offset + length) {
            return 0;
        }

        BinaryUtils.copy(buffer, offset, value);
        return length;
    }

    public static String toString(ByteBuffer buffer, int offset, int length) {
        ByteBuffer bytes = getBytes(buffer, offset, length);
        return BinaryUtils.toString(bytes);
    }

    public static String toHexString(ByteBuffer buffer, int offset, int length) {
        ByteBuffer bytes = getBytes(buffer, offset, length);
        return BinaryUtils.toString(bytes);
    }
}
