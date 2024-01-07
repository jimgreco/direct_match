package com.core.fix;

import com.core.fix.msgs.FixTags;
import com.core.fix.tags.FixTag;
import com.core.util.BinaryUtils;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.util.List;

/**
 * Created by jgreco on 1/4/15.
 */
public class FixMessage {
    private final List<FixTag> tagsInOrder = new FastList<>();
    private final IntObjectHashMap<List<FixTag>> tagsByID = new IntObjectHashMap<>();
    private String msgType;
    private int seqNum;

    public void addTag(FixTag tag) {
        if (tag.getID() == FixTags.MsgType) {
            msgType = BinaryUtils.toString(tag.getValue());
        }
        if (tag.getID() == FixTags.MsgSeqNum) {
            seqNum = tag.getValueAsInt();
        }

        if (tagsByID.containsKey(tag.getID())) {
            tagsByID.get(tag.getID()).add(tag);
        }
        else {
            List<FixTag> fixTags = new FastList<>();
            fixTags.add(tag);
            tagsByID.put(tag.getID(), fixTags);
        }

        tagsInOrder.add(tag);
    }

    public int getValueAsInt(int tagID) {
        return getTag(tagID).getValueAsInt();
    }

    public char getValueAsChar(int tagID) {
        return getTag(tagID).getValueAsChar();
    }

    public String getValueAsString(int tagID) {
        return BinaryUtils.toString(getTag(tagID).getValue());
    }

    public double getValueAsPrice(int tagID) {
        return Double.parseDouble(getValueAsString(tagID));
    }


    public int getValueAsInt(int tagID, int position) {
        return getTag(tagID, position).getValueAsInt();
    }

    public char getValueAsChar(int tagID, int position) {
        return getTag(tagID, position).getValueAsChar();
    }

    public String getValueAsString(int tagID, int position) {
        return BinaryUtils.toString(getTag(tagID, position).getValue());
    }

    public double getValueAsPrice(int tagID, int position) {
        return Double.parseDouble(getValueAsString(tagID, position));
    }




    public FixTag getTag(int id) {
        return getTag(id, 0);
    }

    public FixTag getTag(int id, int position) {
        return tagsByID.get(id).get(position);
    }


    public char getMsgType() {
        return msgType.charAt(0);
    }

    public String getMsgTypeAsString() {
        return msgType;
    }

    public int getSeqNum() {
        return seqNum;
    }
}
