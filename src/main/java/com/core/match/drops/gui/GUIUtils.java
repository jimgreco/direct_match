package com.core.match.drops.gui;

import com.core.match.msgs.MatchConstants;
import com.core.match.util.MatchPriceUtils;
import com.core.util.BinaryUtils;
import com.core.util.TextUtils;
import com.core.util.TimeUtils;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 3/16/16.
 */
public class GUIUtils {
    public static void writeString(ByteBuffer buffer, byte[] key, String value) {
        buffer.put(key);
        buffer.put((byte) '\"');
        BinaryUtils.copy(buffer, value);
        buffer.put((byte) '\"');
    }

    public static void writeLong(ByteBuffer buffer, byte[] key, long value) {
        buffer.put(key);
        TextUtils.writeNumber(buffer, value);
    }

    public static void writeInt(ByteBuffer buffer, byte[] key, int value) {
        buffer.put(key);
        TextUtils.writeNumber(buffer, value);
    }

    public static void startObject(ByteBuffer buffer) {
        buffer.put((byte)'{');
    }

    public static void endObject(ByteBuffer buffer) {
        buffer.put((byte)'}');
        buffer.put((byte)'\n');
    }

    public static void writeTime(ByteBuffer buffer, byte[] key, long value) {
        buffer.put(key);
        TextUtils.writeNumber(buffer, value / TimeUtils.NANOS_PER_MILLI);
    }

    public static void writeEvent(ByteBuffer buffer, byte[] key, char event) {
        writeString(buffer, key, MatchConstants.SystemEvent.toString(event));
    }

    public static void writeBuySell(ByteBuffer buffer, byte[] key, boolean side) {
        writeString(buffer, key, side ? "buy" : "sell");
    }

    public static void writeBidOffer(ByteBuffer buffer, byte[] key, boolean side) {
        writeString(buffer, key, side ? "bid" : "ask");
    }

    public static void writeQty(ByteBuffer buffer, byte[] key, int qty) {
        GUIUtils.writeLong(buffer, key, qty);
    }
}
