package com.core.match.services.contributor;

import com.core.services.StaticsList;

/**
 * User: jgreco
 */
public class Contributor implements StaticsList.StaticsObject {
    private final short id;
    private final String name;
    private int seqNum;
	private boolean cancelOnDisconnect;

    public Contributor(short id, String name) {
        this.id = id;
        this.name = name;
        this.seqNum = 0; 
    }

    @Override
	public short getID() {
        return id;
    }

    @Override
	public String getName() {
        return name;
    }

    public void incSeqNum() {
        seqNum++;
    }

    public int getSeqNum() {
        return seqNum;
    }

	public boolean isCancelOnDisconnect()
	{
		return cancelOnDisconnect;
	}

	public void setCancelOnDisconnect(boolean cancelOnDisconnect)
	{
		this.cancelOnDisconnect = cancelOnDisconnect;
	}
}
