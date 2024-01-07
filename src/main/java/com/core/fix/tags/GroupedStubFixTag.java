package com.core.fix.tags;

import com.core.util.PriceUtils;
import com.gs.collections.impl.list.mutable.FastList;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * User: jgreco
 */
public class GroupedStubFixTag implements FixTag {
    private final int id;
    private List<String> values = new FastList<>();
    int group = 0;

    public GroupedStubFixTag(int id) {
        this.id = id;
    }

    public void setValue(char value) {
        setValue(new String(new char[]{value}));
    }

    public void setValue(String value) {
        if (value != null && value.length() > 0) {
            this.values.add(value);
        }
    }

    public void setValue(int value) {
        setValue(Integer.toString(value));
    }

    public void setValue(double value) {
        setValue(Double.toString(value));
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
        group = 0;
        // none
    }

    @Override
    public void setValuePosition(int startPosition, int endPosition) {
        // none
    }

    @Override
    public boolean isPresent() {
        return values.size() > 0 && group < values.size();
    }

    @Override
    public ByteBuffer getValue() {
        if (!isPresent()) {
            return null;
        }

        return ByteBuffer.wrap(values.get(group++).getBytes());
    }

    @Override
    public char getValueAsChar() {
        if (!isPresent()) {
            return 0;
        }

        return values.get(group++).charAt(0);
    }

    @Override
    public int getValueAsInt() {
        if (!isPresent()) {
            return 0;
        }

        return Integer.parseInt(values.get(group++));
    }

    @Override
    public long getValueAsPrice(int impliedDecimals) {
        if (!isPresent()) {
            return 0;
        }

        return PriceUtils.toLong(Double.parseDouble(values.get(group++)), impliedDecimals);
    }

    @Override
    public int getGroup() {
        return group;
    }

    @Override
    public int getNumGroups() {
        return values.size();
    }
}