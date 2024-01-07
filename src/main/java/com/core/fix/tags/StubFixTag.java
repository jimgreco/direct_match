package com.core.fix.tags;

import com.core.util.PriceUtils;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class StubFixTag implements FixTag {
    private final int id;
    private String value;

    public StubFixTag(int id) {
        this.id = id;
    }

    public void setValue(char value) {
        this.value = new String(new char[] { value });
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setValue(int value) {
        this.value = Integer.toString(value);
    }

    public void setValue(double value) {
        this.value = Double.toString(value);
    }

    @Override
    public byte[] getTagString() {
        return (id + "=").getBytes();
    }

    @Override
    public short getID() {
        return (short)id;
    }

    @Override
    public void reset(ByteBuffer buffer) {
        // none
    }

    @Override
    public void setValuePosition(int startPosition, int endPosition) {
        // none
    }

    @Override
    public boolean isPresent() {
        return value != null && value.length() > 0;
    }

    @Override
    public ByteBuffer getValue() {
        if (value == null) {
            return null;
        }

        return ByteBuffer.wrap(value.getBytes());
    }

    @Override
    public char getValueAsChar() {
        if (value == null) {
            return 0;
        }

        return value.charAt(0);
    }

    @Override
    public int getValueAsInt() {
        if (value == null) {
            return 0;
        }

        return Integer.parseInt(value);
    }

    @Override
    public long getValueAsPrice(int impliedDecimals) {
        if (value == null) {
            return 0;
        }

        return PriceUtils.toLong(Double.parseDouble(value), impliedDecimals);
    }

    @Override
    public int getGroup() {
        return 0;
    }

    @Override
    public int getNumGroups() {
        return 0;
    }
}