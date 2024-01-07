package com.core.match.drops.gui.msgs;

import com.core.match.drops.DropVersionable;
import com.core.match.drops.gui.GUIUtils;

import java.nio.ByteBuffer; 

/* THIS FILE IS AUTOGENERATED */
public class GUIStatus implements DropVersionable {
    private static final byte[] TYPE_BYTES = "\"type\":".getBytes();  
    private static final byte[] EVENT_BYTES = ",\"event\":".getBytes();
    private static final byte[] SES_BYTES = ",\"ses\":".getBytes();
    private static final byte[] CONTRIB_BYTES = ",\"contrib\":".getBytes();
    private static final byte[] ID_BYTES = ",\"id\":".getBytes();
    private static final byte[] VER_BYTES = ",\"ver\":".getBytes();
    private static final byte[] TIME_BYTES = ",\"time\":".getBytes();

	private char event;
    private final int id; 
	private int ver;
	private long time;

    public GUIStatus(
		int id
		) {
        this.id = id;
    }

	public void write(ByteBuffer buffer, String ses, String contrib) {
		GUIUtils.startObject(buffer);
		GUIUtils.writeString(buffer, TYPE_BYTES, getType());		
		GUIUtils.writeEvent(buffer, EVENT_BYTES, getEvent());		
		GUIUtils.writeString(buffer, SES_BYTES, ses);
		GUIUtils.writeString(buffer, CONTRIB_BYTES, contrib);
		GUIUtils.writeInt(buffer, ID_BYTES, getId());		
		GUIUtils.writeInt(buffer, VER_BYTES, getVer());		
		GUIUtils.writeTime(buffer, TIME_BYTES, getTime());		
		GUIUtils.endObject(buffer);
    }
  
    public char getEvent() {
        return event;
    }
  
    public int getId() {
        return id;
    }
  
    public int getVer() {
        return ver;
    }
  
    public long getTime() {
        return time;
    }

	public void setEvent(char val) {
		this.event = val;
	}

	public void setVer(int val) {
		this.ver = val;
	}

	public void setTime(long val) {
		this.time = val;
	}
	
	public String getType() {
		return "status";
	}
} 
