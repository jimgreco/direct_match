package com.core.match.drops.gui.msgs;

import com.core.match.drops.DropVersionable;
import com.core.match.drops.gui.GUIUtils;

import java.nio.ByteBuffer; 

/* THIS FILE IS AUTOGENERATED */
public class GUITrade implements DropVersionable {
    private static final byte[] TYPE_BYTES = "\"type\":".getBytes();  
    private static final byte[] SEC_BYTES = ",\"sec\":".getBytes();
    private static final byte[] SIDE_BYTES = ",\"side\":".getBytes();
    private static final byte[] VOL_BYTES = ",\"vol\":".getBytes();
    private static final byte[] PX_BYTES = ",\"px\":".getBytes();
    private static final byte[] QTY_BYTES = ",\"qty\":".getBytes();
    private static final byte[] MATCHID_BYTES = ",\"matchId\":".getBytes();
    private static final byte[] SES_BYTES = ",\"ses\":".getBytes();
    private static final byte[] CONTRIB_BYTES = ",\"contrib\":".getBytes();
    private static final byte[] ID_BYTES = ",\"id\":".getBytes();
    private static final byte[] VER_BYTES = ",\"ver\":".getBytes();
    private static final byte[] TIME_BYTES = ",\"time\":".getBytes();

    private final String sec; 
	private boolean side;
	private int vol;
	private long px;
	private int qty;
	private int matchId;
    private final int id; 
	private int ver;
	private long time;

    public GUITrade(
		int id
        , String sec
		) {
        this.sec = sec;
        this.id = id;
    }

	public void write(ByteBuffer buffer, String ses, String contrib) {
		GUIUtils.startObject(buffer);
		GUIUtils.writeString(buffer, TYPE_BYTES, getType());		
		GUIUtils.writeString(buffer, SEC_BYTES, getSec());		
		GUIUtils.writeBidOffer(buffer, SIDE_BYTES, getSide());		
		GUIUtils.writeQty(buffer, VOL_BYTES, getVol());		
		GUIUtils.writeLong(buffer, PX_BYTES, getPx());		
		GUIUtils.writeQty(buffer, QTY_BYTES, getQty());		
		GUIUtils.writeInt(buffer, MATCHID_BYTES, getMatchId());		
		GUIUtils.writeString(buffer, SES_BYTES, ses);
		GUIUtils.writeString(buffer, CONTRIB_BYTES, contrib);
		GUIUtils.writeInt(buffer, ID_BYTES, getId());		
		GUIUtils.writeInt(buffer, VER_BYTES, getVer());		
		GUIUtils.writeTime(buffer, TIME_BYTES, getTime());		
		GUIUtils.endObject(buffer);
    }
  
    public String getSec() {
        return sec;
    }
  
    public boolean getSide() {
        return side;
    }
  
    public int getVol() {
        return vol;
    }
  
    public long getPx() {
        return px;
    }
  
    public int getQty() {
        return qty;
    }
  
    public int getMatchId() {
        return matchId;
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

	public void setSide(boolean val) {
		this.side = val;
	}

	public void setVol(int val) {
		this.vol = val;
	}

	public void setPx(long val) {
		this.px = val;
	}

	public void setQty(int val) {
		this.qty = val;
	}

	public void setMatchId(int val) {
		this.matchId = val;
	}

	public void setVer(int val) {
		this.ver = val;
	}

	public void setTime(long val) {
		this.time = val;
	}
	
	public String getType() {
		return "trade";
	}
} 
