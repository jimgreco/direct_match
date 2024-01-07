package com.core.sequencer;

import com.core.util.log.Log;
import com.gs.collections.impl.list.mutable.primitive.IntArrayList;

/**
 * Created by jgreco on 10/17/16.
 */
public abstract class CoreSequencerContributorService extends CoreSequencerBaseService  {
    private final IntArrayList seqNum = new IntArrayList();

    public CoreSequencerContributorService(Log log) {
        super(log, "Contributors");
    }

    public int incSeqNum(int id) {
        int index = getIndex(id);
        int snum = seqNum.get(index) + 1;
        seqNum.set(index, snum);
        return snum;
    }

    public int getSeqNum(int id) {
        return seqNum.get(getIndex(id));
    }

    public void setSeqNum(int id, int snum) {
        seqNum.set(getIndex(id), snum);
    }

    public short getOrAddContributor(String name) {
        short id = getID(name);
        if (id == 0) {
            id = addContributor(name);
        }
        return id;
    }

    public short addContributor(String name) {
        seqNum.add(0);
        return (short)super.add(name);
    }
}
