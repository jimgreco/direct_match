package com.core.match.drops.gui.msgs;

import com.core.match.drops.DropVersionable;
import com.core.match.drops.gui.GUIUtils;

import java.nio.ByteBuffer; 

/* THIS FILE IS AUTOGENERATED */
public class GUISecurity implements DropVersionable {
    private static final byte[] TYPE_BYTES = "\"type\":".getBytes();  
    private static final byte[] SEC_BYTES = ",\"sec\":".getBytes();
    private static final byte[] SECTYPE_BYTES = ",\"secType\":".getBytes();
    private static final byte[] LOTSIZE_BYTES = ",\"lotSize\":".getBytes();
    private static final byte[] TICKSIZE_BYTES = ",\"tickSize\":".getBytes();
    private static final byte[] CUSIP_BYTES = ",\"cusip\":".getBytes();
    private static final byte[] MATDATE_BYTES = ",\"matDate\":".getBytes();
    private static final byte[] COUPON_BYTES = ",\"coupon\":".getBytes();
    private static final byte[] SES_BYTES = ",\"ses\":".getBytes();
    private static final byte[] CONTRIB_BYTES = ",\"contrib\":".getBytes();
    private static final byte[] ID_BYTES = ",\"id\":".getBytes();
    private static final byte[] VER_BYTES = ",\"ver\":".getBytes();
    private static final byte[] TIME_BYTES = ",\"time\":".getBytes();

    private final String sec; 
	private String secType;
	private int lotSize;
	private long tickSize;
	private String cusip;
	private int matDate;
	private long coupon;
    private final int id; 
	private int ver;
	private long time;

    public GUISecurity(
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
		GUIUtils.writeString(buffer, SECTYPE_BYTES, getSecType());		
		GUIUtils.writeInt(buffer, LOTSIZE_BYTES, getLotSize());		
		GUIUtils.writeLong(buffer, TICKSIZE_BYTES, getTickSize());		
		GUIUtils.writeString(buffer, CUSIP_BYTES, getCusip());		
		GUIUtils.writeInt(buffer, MATDATE_BYTES, getMatDate());		
		GUIUtils.writeLong(buffer, COUPON_BYTES, getCoupon());		
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
  
    public String getSecType() {
        return secType;
    }
  
    public int getLotSize() {
        return lotSize;
    }
  
    public long getTickSize() {
        return tickSize;
    }
  
    public String getCusip() {
        return cusip;
    }
  
    public int getMatDate() {
        return matDate;
    }
  
    public long getCoupon() {
        return coupon;
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

	public void setSecType(String val) {
		this.secType = val;
	}

	public void setLotSize(int val) {
		this.lotSize = val;
	}

	public void setTickSize(long val) {
		this.tickSize = val;
	}

	public void setCusip(String val) {
		this.cusip = val;
	}

	public void setMatDate(int val) {
		this.matDate = val;
	}

	public void setCoupon(long val) {
		this.coupon = val;
	}

	public void setVer(int val) {
		this.ver = val;
	}

	public void setTime(long val) {
		this.time = val;
	}
	
	public String getType() {
		return "security";
	}
} 