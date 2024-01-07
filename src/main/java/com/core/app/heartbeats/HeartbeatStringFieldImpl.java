package com.core.app.heartbeats;

import com.core.util.BinaryUtils;
import com.core.util.ByteStringBuffer;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 5/29/15.
 */
public class HeartbeatStringFieldImpl extends HeartbeatFieldBase implements HeartbeatStringField {
    private String value = "";

    public HeartbeatStringFieldImpl(byte fieldID, String category, HeartBeatFieldIDEnum fieldName) {
        super(fieldID, category, fieldName);
    }

    public static void writeString(ByteBuffer buffer, String str) {
        buffer.putShort((short) str.length());
        BinaryUtils.copy(buffer, str);
    }

    public static void writeString(ByteBuffer buffer, byte[] str) {
        buffer.putShort((short) str.length);
        buffer.put(str);
    }

    @Override
    public char getType() {
        return 'S';
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    protected void writeAsBinary(ByteStringBuffer buffer) {
        buffer.add(value);
    }

    @Override
    public void set(String val) {
        value = val;

        if (value == null) {
            value = "";
        }
    }

    @Override
    public String get() {
        return value;
    }
}