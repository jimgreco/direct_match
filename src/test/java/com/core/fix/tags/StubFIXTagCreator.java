package com.core.fix.tags;

import com.core.fix.msgs.FixTags;
import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;

/**
 * Created by jgreco on 1/6/15.
 */
public class StubFIXTagCreator implements FixTagCreator {
    private final String version;
    private final String client;
    private final String server;
    private int nextSeq = 1;
    private IntObjectHashMap<StubFixTag> idTagMap = new IntObjectHashMap<>();

    public StubFIXTagCreator(int minorVersion, String client, String server) {
        this.version = "FIX.4." + minorVersion;
        this.client = client;
        this.server = server;
    }

    @Override
    public FixTag createReadWriteFIXTag(int id) {
        StubFixTag stubFixTag = idTagMap.get(id);
        if (stubFixTag == null) {
            stubFixTag = new StubFixTag(id);
            idTagMap.put(id, stubFixTag);
        }
        return stubFixTag;
    }

    @Override
    public FixTag createWriteOnlyFIXTag(int id) {
        return new StubFixTag(id);
    }

    @Override
    public FixTag createReadWriteFIXGroupTag(int id) {
        return new StubFixTag(id);
    }

    public void newMsg(char type) {
        clear();
        setTag(FixTags.BeginString, version);
//        setTag(FixTags.BodyLength, 0);
        setTag(FixTags.MsgType, type);
        setTag(FixTags.MsgSeqNum, nextSeq++);
        setTag(FixTags.SenderCompID, client);
        setTag(FixTags.TargetCompID, server);
    }

    public void clear() {
        for (StubFixTag stubFixTag : idTagMap) {
            stubFixTag.setValue(null);
        }
    }

    public void setTag(int tagID, String value) {
        StubFixTag tag = idTagMap.get(tagID);
        tag.setValue(value);
    }

    public void setTag(int tagID, int value) {
        StubFixTag tag = idTagMap.get(tagID);
        tag.setValue(value);
    }

    public void setTag(int tagID, double value) {
        StubFixTag tag = idTagMap.get(tagID);
        tag.setValue(value);
    }

    public void setTag(int tagID, char value) {
        StubFixTag tag = idTagMap.get(tagID);
        tag.setValue(value);
    }
}
