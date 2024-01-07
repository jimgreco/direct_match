package com.core.match.drops;

/**
 * Created by jgreco on 2/18/16.
 */
public class LinearCounter {
    private int version = 0;

    public int incVersion() {
        return ++version;
    }

    public int getVersion() {
        return version;
    }
}
