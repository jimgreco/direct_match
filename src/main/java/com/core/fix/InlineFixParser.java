package com.core.fix;

import com.core.fix.msgs.FixConstants;
import com.core.fix.msgs.FixTags;
import com.core.fix.tags.FixTag;
import com.core.fix.tags.InlineFixTag;
import com.core.fix.tags.InlineRepeatingGroupFixTag;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.primitive.ShortObjectHashMap;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * User: jgreco
 */
public class InlineFixParser implements FixParser {
    private final ShortObjectHashMap<FixTag> registeredTagsById = new ShortObjectHashMap<>();
    private final List<FixTag> registeredTags = new FastList<>();

    public InlineFixParser() {
        createReadWriteFIXTag(FixTags.CheckSum);
    }

    // everyone shares the same tags
    @Override
    public FixTag createReadWriteFIXTag(int id) {
        FixTag fixTag = registeredTagsById.get((short)id);

        if (fixTag == null) {
            fixTag = new InlineFixTag(id);
            registeredTagsById.put((short)id, fixTag);
            registeredTags.add(fixTag);
        }

        return fixTag;
    }

    @Override
    public FixTag createReadWriteFIXGroupTag(int id) {
        FixTag fixTag = registeredTagsById.get((short)id);

        if (fixTag == null) {
            fixTag = new InlineRepeatingGroupFixTag(id);
            registeredTagsById.put((short)id, fixTag);
            registeredTags.add(fixTag);
        }

        return fixTag;
    }

    @Override
    public FixTag createWriteOnlyFIXTag(int id) {
        return new InlineFixTag(id);
    }

    // assumes a valid FIX message with a trailer of 10=XXX(SOH)
    @Override
    public int parse(ByteBuffer buffer) throws InvalidFixMessageException {
        // not the most efficient way of doing things
        // but it's fine for our single app purposes
        for (int i=0; i<registeredTags.size(); i++) {
            registeredTags.get(i).reset(buffer);
        }

        int startPosition = buffer.position();

        while(buffer.hasRemaining()) {
            short tag = parseTag(buffer);
            if (tag == -1) {
                break;
            }

            FixTag fixTag = registeredTagsById.get(tag);

            int startPositionOfValue = buffer.position();

            if (!advanceToNextTag(buffer)) {
                break;
            }

            int endPositionOfValue = buffer.position() - 1;

            if (endPositionOfValue < startPositionOfValue) {
                throw new InvalidFixMessageException("Invalid FIX message");
            }

            if (fixTag != null) {
                fixTag.setValuePosition(startPositionOfValue, endPositionOfValue);

                if (fixTag.getID() == FixTags.CheckSum) {
                    int endPosition = buffer.position();
                    buffer.position(startPosition);
                    return endPosition - startPosition;
                }
            }
        }

        buffer.position(startPosition);
        return 0;
    }

    public static short parseTag(ByteBuffer buffer) throws InvalidFixMessageException {
        short tag = 0;

        while(buffer.hasRemaining()) {
            byte b = buffer.get();

            if (b == '=') {
                return tag;
            }

            if (b < '0' || b > '9') {
                throw new InvalidFixMessageException("Invalid FIX tag");
            }

            tag *= 10;
            tag += (b - '0');
        }

        return -1;
    }

    public static boolean advanceToNextTag(ByteBuffer buffer)  {
        while(buffer.hasRemaining()) {
            byte b = buffer.get();
            if (b == FixConstants.SOH) {
                return true;
            }
        }

        return false;
    }
}
