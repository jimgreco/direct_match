package com.core.app.heartbeats;

import com.core.util.ByteStringBuffer;

/**
 * Created by jgreco on 5/29/15.
 */
public class HeartbeatNumberFieldImpl extends HeartbeatFieldBase implements HeartbeatNumberField {
    private long value;

    public HeartbeatNumberFieldImpl(byte fieldID, String category, HeartBeatFieldIDEnum fieldName) {
        super(fieldID, category, fieldName);
    }

    @Override
    public char getType() {
        return 'N';
    }

    @Override
    public String getValue() {
        return String.valueOf(value);
    }

    @Override
    protected void writeAsBinary(ByteStringBuffer buffer) {
        buffer.add(value);
    }

    @Override
    public void set(long val) {
        value = val;
    }

    @Override
    public long get() {
        return value;
    }

    @Override
    public long inc() {
        return ++value;
    }
}
