package com.core.match.itch.client;

import com.core.services.StaticsList;

/**
 * Created by jgreco on 7/6/15.
 */
public class ITCHClientSecurity implements StaticsList.StaticsObject {
    private final short id;
    private final String name;

    public ITCHClientSecurity(short id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
	public short getID() {
        return id;
    }

    @Override
	public String getName() {
        return name;
    }
}
