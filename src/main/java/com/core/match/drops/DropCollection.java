package com.core.match.drops;

import com.gs.collections.impl.list.mutable.FastList;

import java.util.Iterator;
import java.util.List;

/**
 * Created by jgreco on 2/20/16.
 */
public abstract class DropCollection {
    private final DropIterator iterator;
    private final List<DropVersionable> versions;
    private final LinearCounter versionCounter;
    protected final LinearCounter itemCounter;

    public DropCollection(LinearCounter versionCounter, LinearCounter itemCounter) {
        this.versionCounter = versionCounter;
        this.itemCounter = itemCounter;
        this.versions = new FastList<>();
        this.iterator = new DropIterator(versions);
    }

    protected void addVersion(DropVersionable versionable) {
        versionable.setVer(versionCounter.incVersion());
        versions.add(versionable);
    }

    protected void updateVersion(DropVersionable versionable) {
        versionable.setVer(versionCounter.incVersion());
    }

    public DropVersionable getItem(int index) {
        return versions.get(index);
    }

    public DropIterator getIterator(int lastVersion) {
        iterator.reset(lastVersion);
        return iterator;
    }

    public class DropIterator implements Iterator<DropVersionable> {
        private final List<DropVersionable> versions;
        private int version;
        private int maxVersion;
        private int index;

        public DropIterator(List<DropVersionable> versions) {
            this.versions = versions;
        }

        public void reset(int version) {
            this.index = 0;
            this.maxVersion = 0;
            this.version = version;
        }

        @Override
        public boolean hasNext() {
            return index < versions.size();
        }

        @Override
        public DropVersionable next() {
            while (index < versions.size()) {
                DropVersionable securityNext = versions.get(index++);
                maxVersion = Math.max(maxVersion, securityNext.getVer());
                if (version < securityNext.getVer()) {
                    return securityNext;
                }
            }

            return null;
        }

        public int getMaxVersion() {
            return maxVersion;
        }
    }
}
