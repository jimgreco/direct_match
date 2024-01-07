package com.core.fix.tags;

/**
 * Created by jgreco on 1/6/15.
 */
public interface FixTagCreator {
    FixTag createReadWriteFIXTag(int id);
    FixTag createWriteOnlyFIXTag(int id);
    FixTag createReadWriteFIXGroupTag(int id);
}
