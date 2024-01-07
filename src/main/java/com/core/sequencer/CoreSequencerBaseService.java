package com.core.sequencer;

import com.core.match.msgs.MatchConstants;
import com.core.util.log.Log;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.list.mutable.primitive.BooleanArrayList;
import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;

/**
 * Created by jgreco on 7/26/15.
 */
public abstract class CoreSequencerBaseService {
    protected final Log log;

    private final BooleanArrayList disabled = new BooleanArrayList();
    private final ObjectIntHashMap<String> nameIdMap = new ObjectIntHashMap<>();
    private final FastList<String> names = new FastList<>();
    private final String name;

    public CoreSequencerBaseService(Log log, String name) {
        this.log = log;
        this.name = name;
    }

    public int size() {
        return names.size();
    }

    public boolean isValid(int id) {
        return id >= MatchConstants.STATICS_START_INDEX && id < size() + MatchConstants.STATICS_START_INDEX;
    }

    protected int add(String name) {
        names.add(name);
        nameIdMap.put(name, (short)size());
        disabled.add(false);
        return (short)size();
    }

    public short getID(String name) {
        return (short)nameIdMap.get(name);
    }

    public int getIDAsInt(String name) {
        return nameIdMap.get(name);
    }

    public String getName(int id) {
        return names.get(getIndex(id));
    }

    public boolean isDisabled(int id)
    {
        if (!isValid(id)) {
            log.error(log.log()
                    .add("Tried to check disabled static of non-existent statics. Name=").add(name)
                    .add(", ID=").add(id));
            return true;
        }
        return disabled.get(getIndex(id));
    }

    public void setDisabled(int id)
    {
        if (!isValid(id)) {
            log.error(log.log()
                    .add("Tried to disable non-existent statics. Name=").add(name)
                    .add(", ID=").add(id));
            return;
        }
        disabled.set(getIndex(id), true);
    }

    public void setEnabled(int id )
    {
        if (!isValid(id)) {
            log.error(log.log()
                    .add("Tried to enable non-existent statics. Name=").add(name)
                    .add(", ID=").add(id));
            return;
        }
        disabled.set(getIndex(id), false);
    }

    protected static int getIndex(int id) {
        return id - MatchConstants.STATICS_START_INDEX;
    }
}
