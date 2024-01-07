package com.core.match.services.security;

/**
 * Created by jgreco on 11/21/14.
 */
public enum SecurityCategory {
    BOND("Bond"), MULTI_LEG("MultiLegInstrument");

    private final String name;

    SecurityCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
