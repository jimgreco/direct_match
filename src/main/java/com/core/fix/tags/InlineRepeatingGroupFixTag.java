package com.core.fix.tags;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class InlineRepeatingGroupFixTag extends InlineFixTag {
    private static final int MAX_GROUPS = 20;

    private final int[] startPositions = new int[MAX_GROUPS];
    private final int[] endPositions = new int[MAX_GROUPS];
    private int readGroup;
    private int writeGroup;

    public InlineRepeatingGroupFixTag(int id) {
        super(id);
    }

    @Override
    public void reset(ByteBuffer newBuffer) {
        super.reset(newBuffer);
        this.readGroup = 0;
        this.writeGroup = 0;
    }

    @Override
    public void setValuePosition(int startPosition, int endPosition) {
        super.setValuePosition(startPosition, endPosition);
        this.startPositions[writeGroup] = startPosition;
        this.endPositions[writeGroup] = endPosition;
        writeGroup++;
    }

    @Override
    public ByteBuffer getValue() {
        if (!isPresent()) {
            return null;
        }

        int position = buffer.position();
        ByteBuffer slice = buffer.slice();
        slice.position(startPositions[readGroup] - position);
        slice.limit(endPositions[readGroup] - position);
        readGroup++;
        return slice;
    }

    @Override
    public int getGroup() {
        return readGroup;
    }

    @Override
    public int getNumGroups() {
        return writeGroup;
    }
}
