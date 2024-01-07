package com.core.util;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class BinaryUtils {
    public static final byte[] ASCII = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static String readString(ByteBuffer buffer, int length) {
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes);
    }


    public static ByteBuffer createCopy(ByteBuffer buffer) {
        if (buffer == null) {
            return ByteBuffer.allocate(0);
        }

        buffer.mark();
        ByteBuffer allocate = ByteBuffer.allocate(buffer.remaining());
        allocate.put(buffer);
        buffer.reset();
        allocate.rewind();
        return allocate;
    }

    public static ByteBuffer copy(ByteBuffer dest, String str) {
        for (int i=0; i<str.length(); i++) {
            dest.put((byte) str.charAt(i));
        }
        return dest;
    }

    public static ByteBuffer copy(ByteBuffer dest, int offset, String str) {
        for (int i=0; i<str.length(); i++) {
            dest.put(dest.position() + offset + i, (byte) str.charAt(i));
        }
        return dest;
    }

    public static ByteBuffer copy(ByteBuffer dest, int offset, ByteBuffer src) {
        int destPosition = dest.position();
        int srcPosition = src.position();

        dest.position(dest.position() + offset);
        dest.put(src);

        dest.position(destPosition);
        src.position(srcPosition);
        return dest;
    }

    public static ByteBuffer copy(ByteBuffer dest, ByteBuffer src) {
        int srcPosition = src.position();
        dest.put(src);
        src.position(srcPosition);
        return dest;
    }

    public static boolean compare(ByteBuffer buffer, String string) {
        int remaining = buffer.remaining();
        if (string.length() != remaining) {
            return false;
        }

        for (int i=0; i<remaining; i++) {
            byte b = buffer.get(buffer.position() + i);
            if (b != (byte)string.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    public static boolean compare(ByteBuffer buffer1, ByteBuffer buffer2) {
        if (buffer1.remaining() != buffer2.remaining()) {
            return false;
        }

        for (int i=0; i<buffer1.remaining(); i++) {
            byte b1 = buffer1.get(buffer1.position() + i);
            byte b2 = buffer2.get(buffer2.position() + i);

            if (b1 != b2) {
                return false;
            }
        }

        return true;
    }

    public static boolean compareMin(ByteBuffer buffer, String string) {
        for (int i=0; i<Math.min(buffer.remaining(), string.length()); i++) {
            byte b = buffer.get(buffer.position() + i);
            if (b != (byte)string.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    public static String toString(ByteBuffer buffer) {
        if (buffer == null) {
            return "";
        }

        int remaining = buffer.remaining();
        byte[] bytes = new byte[remaining];

        int position = buffer.position();
        buffer.get(bytes);
        buffer.position(position);

        return new String(bytes);
    }

    public static byte[] toBytes(ByteBuffer value) {
        byte[] bytes = new byte[value.remaining()];

        value.mark();
        value.get(bytes);
        value.reset();

        return bytes;
    }

    public static String toHexString(ByteBuffer buf, int offset, int length) {
        StringBuilder builder = new StringBuilder();
        for(int i=buf.position() + offset; i<buf.position() + offset + length; i++) {
            byte b = buf.get(i);
            if (b >= ' ' && b < '~') {
                builder.append(b);
            }
            else {
                builder.append('.');
            }
            builder.append('[');
            builder.append(ASCII[(b >> 4) & 0xF]);
            builder.append(ASCII[b & 0xF]);
            builder.append(']');
            builder.append(' ');
        }
        return builder.toString();
    }
    
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    
    public static String toHex(ByteBuffer buffer) {
    	byte[] bufferBytes = toBytes(buffer);
    	return bytesToHex(bufferBytes);
    }
}
