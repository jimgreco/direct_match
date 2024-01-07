package com.core.fix.tags;

import com.core.util.BinaryUtils;
import com.core.util.PriceUtils;
import com.core.util.TextUtils;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class InlineFixTag implements FixTag {
    private final byte[] tagString;
    private final short id;

    private boolean present;
    protected ByteBuffer buffer;

    private int startPosition;
    private int endPosition;

    public InlineFixTag(int id) {
        this.id = (short)id;
        this.tagString = (id + "=").getBytes();
    }

    @Override
    public byte[] getTagString() {
        return tagString;
    }

    @Override
    public short getID() {
        return id;
    }

    @Override
    public void reset(ByteBuffer newBuffer) {
        this.present = false;
        this.buffer = newBuffer;
    }

    @Override
    public void setValuePosition(int startPosition, int endPosition) {
        this.present = endPosition > startPosition;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    @Override
    public boolean isPresent() {
        return present;
    }

    @Override
    public ByteBuffer getValue() {
        if (!isPresent()) {
            return null;
        }

        int position = buffer.position();
        ByteBuffer slice = buffer.slice();
        slice.position(startPosition - position);
        slice.limit(endPosition - position);
        return slice;
    }

    public String getValueAsString() {
        if (!isPresent()) {
            return null;
        }

        return BinaryUtils.toString(getValue());
    }

    @Override
    public char getValueAsChar() {
        if (!isPresent()) {
            return 0;
        }

        ByteBuffer value = getValue();
        return (char)value.get(value.position());
    }

    @Override
    public int getValueAsInt() {
        if (!isPresent()) {
            return 0;
        }

        return TextUtils.parseNumber(getValue());
    }

    @Override
    public long getValueAsPrice(int impliedDecimals) {
        if (!isPresent()) {
            return 0;
        }

        return PriceUtils.parsePrice(getValue(), impliedDecimals);
    }

    @Override
    public int getGroup() {
        return isPresent() ? 1 : 0;
    }

    @Override
    public int getNumGroups() {
        return isPresent() ? 1 : 0;
    }
}
