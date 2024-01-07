package com.core.match.msgs;

import java.nio.ByteBuffer;

public class MatchSecurityByteBufferMessage implements 
	MatchSecurityEvent, 
	MatchSecurityCommand {
    private static final int DEFAULT_LENGTH = 82;
    private static final byte[] EMPTY = new byte[DEFAULT_LENGTH];
     
    static {
        java.util.Arrays.fill(EMPTY, com.core.util.MessageUtils.NULL_BYTE);
    }
   
    private ByteBuffer buffer;
    private int stringsBlockLength;

    @Override
    public int getLength() {
       return DEFAULT_LENGTH + stringsBlockLength;
    }
	
    @Override
    public void setLength(int length) {
        stringsBlockLength = (length - DEFAULT_LENGTH);
    }

    @Override
    public ByteBuffer getRawBuffer() {
       return buffer;
    }

    @Override 
    public void copy(MatchSecurityEvent cmd) {
        setMsgType(cmd.getMsgType());
        setContributorID(cmd.getContributorID());
        setContributorSeq(cmd.getContributorSeq());
        setTimestamp(cmd.getTimestamp());
        setSecurityID(cmd.getSecurityID());
        setName(cmd.getName());
        setCUSIP(cmd.getCUSIP());
        setMaturityDate(cmd.getMaturityDate());
        setCoupon(cmd.getCoupon());
        setType(cmd.getType());
        setIssueDate(cmd.getIssueDate());
        setCouponFrequency(cmd.getCouponFrequency());
        setTickSize(cmd.getTickSize());
        setLotSize(cmd.getLotSize());
        setBloombergID(cmd.getBloombergID());
        setNumLegs(cmd.getNumLegs());
        setLeg1ID(cmd.getLeg1ID());
        setLeg2ID(cmd.getLeg2ID());
        setLeg3ID(cmd.getLeg3ID());
        setLeg1Size(cmd.getLeg1Size());
        setLeg2Size(cmd.getLeg2Size());
        setLeg3Size(cmd.getLeg3Size());
        setUnderlyingID(cmd.getUnderlyingID());
        setReferencePrice(cmd.getReferencePrice());
    }

    @Override
    public MatchSecurityEvent toEvent() {
        return this;
    }

    @Override
    public MatchSecurityCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "Security";
    }
 
    public MatchSecurityCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('S');
        stringsBlockLength = 0;
        return this;
    }

    public MatchSecurityEvent wrapEvent(ByteBuffer buf) {
        buffer = buf;
		setLength(buffer.remaining());
        return this;
    }

     @Override
     public char getMsgType() {
         return com.core.util.MessageUtils.getChar(buffer, Offsets.MsgType);  	 
     }

    @Override
    public void setMsgType(char value) {
    	com.core.util.MessageUtils.setChar(buffer, Offsets.MsgType, value);  									
    }

    @Override
    public boolean hasMsgType() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.MsgType, Lengths.MsgType);  
    }

     @Override
     public short getContributorID() {
         return com.core.util.MessageUtils.getShort(buffer, Offsets.ContributorID);  	 
     }

    @Override
    public void setContributorID(short value) {
    	com.core.util.MessageUtils.setShort(buffer, Offsets.ContributorID, value);  									
    }

    @Override
    public boolean hasContributorID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.ContributorID, Lengths.ContributorID);  
    }

     @Override
     public int getContributorSeq() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.ContributorSeq);  	 
     }

    @Override
    public void setContributorSeq(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.ContributorSeq, value);  									
    }

    @Override
    public boolean hasContributorSeq() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.ContributorSeq, Lengths.ContributorSeq);  
    }

     @Override
     public long getTimestamp() {
         return com.core.util.MessageUtils.getLong(buffer, Offsets.Timestamp);  	 
     }

     @Override
     public java.time.LocalDateTime getTimestampAsTime() {
         return com.core.util.MessageUtils.getDateTime(buffer, Offsets.Timestamp);  	 
     }

    @Override
    public void setTimestamp(long value) {
    	com.core.util.MessageUtils.setLong(buffer, Offsets.Timestamp, value);  									
    }

    @Override
    public boolean hasTimestamp() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Timestamp, Lengths.Timestamp);  
    }

     @Override
     public short getSecurityID() {
         return com.core.util.MessageUtils.getShort(buffer, Offsets.SecurityID);  	 
     }

    @Override
    public void setSecurityID(short value) {
    	com.core.util.MessageUtils.setShort(buffer, Offsets.SecurityID, value);  									
    }

    @Override
    public boolean hasSecurityID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.SecurityID, Lengths.SecurityID);  
    }

     @Override
     public java.nio.ByteBuffer getName() {
		 return com.core.util.MessageUtils.getVariableString(buffer, Offsets.Name);
     }

     @Override
     public int getNameLength() {
		 return com.core.util.MessageUtils.getStringLength(buffer, Offsets.Name);
     }

     @Override
     public String getNameAsString() {
		 return com.core.util.MessageUtils.toVariableString(buffer, Offsets.Name);
     }

    @Override
    public void setName(java.nio.ByteBuffer value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.Name, value, getLength());  			
    }

    @Override
    public void setName(String value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.Name, value, getLength());  			
    }

    @Override
    public boolean hasName() {
		return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Name, Lengths.Name) && getNameLength() > 0;  
    }

     @Override
     public java.nio.ByteBuffer getCUSIP() {
		 return com.core.util.MessageUtils.getVariableString(buffer, Offsets.CUSIP);
     }

     @Override
     public int getCUSIPLength() {
		 return com.core.util.MessageUtils.getStringLength(buffer, Offsets.CUSIP);
     }

     @Override
     public String getCUSIPAsString() {
		 return com.core.util.MessageUtils.toVariableString(buffer, Offsets.CUSIP);
     }

    @Override
    public void setCUSIP(java.nio.ByteBuffer value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.CUSIP, value, getLength());  			
    }

    @Override
    public void setCUSIP(String value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.CUSIP, value, getLength());  			
    }

    @Override
    public boolean hasCUSIP() {
		return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.CUSIP, Lengths.CUSIP) && getCUSIPLength() > 0;  
    }

     @Override
     public int getMaturityDate() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.MaturityDate);  	 
     }

     @Override
     public java.time.LocalDate getMaturityDateAsDate() {
         return com.core.util.MessageUtils.getDate(buffer, Offsets.MaturityDate);  	 
     }

    @Override
    public void setMaturityDate(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.MaturityDate, value);  									
    }

    @Override
    public void setMaturityDateAsDate(java.time.LocalDate value) {
    	com.core.util.MessageUtils.setDate(buffer, Offsets.MaturityDate, value);  									
    }

    @Override
    public boolean hasMaturityDate() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.MaturityDate, Lengths.MaturityDate);  
    }

     @Override
     public long getCoupon() {
         return com.core.util.MessageUtils.getLong(buffer, Offsets.Coupon);  	 
     }

     @Override
     public String getCouponAs32nd() {
         return com.core.match.util.MessageUtils.to32ndPrice(buffer, Offsets.Coupon);  	 
     }

     @Override
     public double getCouponAsDouble() {
         return com.core.match.util.MessageUtils.getDoublePrice(buffer, Offsets.Coupon);  	 
     }

    @Override
    public void setCoupon(long value) {
    	com.core.util.MessageUtils.setLong(buffer, Offsets.Coupon, value);  									
    }

    @Override
    public void setCoupon(double value) {
    	com.core.match.util.MessageUtils.setDoublePrice(buffer, Offsets.Coupon, value);  									
    }

    @Override
    public boolean hasCoupon() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Coupon, Lengths.Coupon);  
    }

     @Override
     public char getType() {
         return com.core.util.MessageUtils.getChar(buffer, Offsets.Type);  	 
     }

    @Override
    public void setType(char value) {
    	com.core.util.MessageUtils.setChar(buffer, Offsets.Type, value);  									
    }

    @Override
    public boolean hasType() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Type, Lengths.Type);  
    }

     @Override
     public int getIssueDate() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.IssueDate);  	 
     }

     @Override
     public java.time.LocalDate getIssueDateAsDate() {
         return com.core.util.MessageUtils.getDate(buffer, Offsets.IssueDate);  	 
     }

    @Override
    public void setIssueDate(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.IssueDate, value);  									
    }

    @Override
    public void setIssueDateAsDate(java.time.LocalDate value) {
    	com.core.util.MessageUtils.setDate(buffer, Offsets.IssueDate, value);  									
    }

    @Override
    public boolean hasIssueDate() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.IssueDate, Lengths.IssueDate);  
    }

     @Override
     public byte getCouponFrequency() {
         return com.core.util.MessageUtils.getByte(buffer, Offsets.CouponFrequency);  	 
     }

    @Override
    public void setCouponFrequency(byte value) {
    	com.core.util.MessageUtils.setByte(buffer, Offsets.CouponFrequency, value);  									
    }

    @Override
    public boolean hasCouponFrequency() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.CouponFrequency, Lengths.CouponFrequency);  
    }

     @Override
     public long getTickSize() {
         return com.core.util.MessageUtils.getLong(buffer, Offsets.TickSize);  	 
     }

     @Override
     public String getTickSizeAs32nd() {
         return com.core.match.util.MessageUtils.to32ndPrice(buffer, Offsets.TickSize);  	 
     }

     @Override
     public double getTickSizeAsDouble() {
         return com.core.match.util.MessageUtils.getDoublePrice(buffer, Offsets.TickSize);  	 
     }

    @Override
    public void setTickSize(long value) {
    	com.core.util.MessageUtils.setLong(buffer, Offsets.TickSize, value);  									
    }

    @Override
    public void setTickSize(double value) {
    	com.core.match.util.MessageUtils.setDoublePrice(buffer, Offsets.TickSize, value);  									
    }

    @Override
    public boolean hasTickSize() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.TickSize, Lengths.TickSize);  
    }

     @Override
     public int getLotSize() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.LotSize);  	 
     }

     @Override
     public double getLotSizeAsQty() {
         return com.core.match.util.MessageUtils.toQtyRoundLot(buffer, Offsets.LotSize);  	 
     }

    @Override
    public void setLotSize(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.LotSize, value);  									
    }

    @Override
    public boolean hasLotSize() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.LotSize, Lengths.LotSize);  
    }

     @Override
     public java.nio.ByteBuffer getBloombergID() {
		 return com.core.util.MessageUtils.getVariableString(buffer, Offsets.BloombergID);
     }

     @Override
     public int getBloombergIDLength() {
		 return com.core.util.MessageUtils.getStringLength(buffer, Offsets.BloombergID);
     }

     @Override
     public String getBloombergIDAsString() {
		 return com.core.util.MessageUtils.toVariableString(buffer, Offsets.BloombergID);
     }

    @Override
    public void setBloombergID(java.nio.ByteBuffer value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.BloombergID, value, getLength());  			
    }

    @Override
    public void setBloombergID(String value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.BloombergID, value, getLength());  			
    }

    @Override
    public boolean hasBloombergID() {
		return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.BloombergID, Lengths.BloombergID) && getBloombergIDLength() > 0;  
    }

     @Override
     public byte getNumLegs() {
         return com.core.util.MessageUtils.getByte(buffer, Offsets.NumLegs);  	 
     }

    @Override
    public void setNumLegs(byte value) {
    	com.core.util.MessageUtils.setByte(buffer, Offsets.NumLegs, value);  									
    }

    @Override
    public boolean hasNumLegs() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.NumLegs, Lengths.NumLegs);  
    }

     @Override
     public short getLeg1ID() {
         return com.core.util.MessageUtils.getShort(buffer, Offsets.Leg1ID);  	 
     }

    @Override
    public void setLeg1ID(short value) {
    	com.core.util.MessageUtils.setShort(buffer, Offsets.Leg1ID, value);  									
    }

    @Override
    public boolean hasLeg1ID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Leg1ID, Lengths.Leg1ID);  
    }

     @Override
     public short getLeg2ID() {
         return com.core.util.MessageUtils.getShort(buffer, Offsets.Leg2ID);  	 
     }

    @Override
    public void setLeg2ID(short value) {
    	com.core.util.MessageUtils.setShort(buffer, Offsets.Leg2ID, value);  									
    }

    @Override
    public boolean hasLeg2ID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Leg2ID, Lengths.Leg2ID);  
    }

     @Override
     public short getLeg3ID() {
         return com.core.util.MessageUtils.getShort(buffer, Offsets.Leg3ID);  	 
     }

    @Override
    public void setLeg3ID(short value) {
    	com.core.util.MessageUtils.setShort(buffer, Offsets.Leg3ID, value);  									
    }

    @Override
    public boolean hasLeg3ID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Leg3ID, Lengths.Leg3ID);  
    }

     @Override
     public int getLeg1Size() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.Leg1Size);  	 
     }

    @Override
    public void setLeg1Size(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.Leg1Size, value);  									
    }

    @Override
    public boolean hasLeg1Size() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Leg1Size, Lengths.Leg1Size);  
    }

     @Override
     public int getLeg2Size() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.Leg2Size);  	 
     }

    @Override
    public void setLeg2Size(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.Leg2Size, value);  									
    }

    @Override
    public boolean hasLeg2Size() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Leg2Size, Lengths.Leg2Size);  
    }

     @Override
     public int getLeg3Size() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.Leg3Size);  	 
     }

    @Override
    public void setLeg3Size(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.Leg3Size, value);  									
    }

    @Override
    public boolean hasLeg3Size() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Leg3Size, Lengths.Leg3Size);  
    }

     @Override
     public short getUnderlyingID() {
         return com.core.util.MessageUtils.getShort(buffer, Offsets.UnderlyingID);  	 
     }

    @Override
    public void setUnderlyingID(short value) {
    	com.core.util.MessageUtils.setShort(buffer, Offsets.UnderlyingID, value);  									
    }

    @Override
    public boolean hasUnderlyingID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.UnderlyingID, Lengths.UnderlyingID);  
    }

     @Override
     public long getReferencePrice() {
         return com.core.util.MessageUtils.getLong(buffer, Offsets.ReferencePrice);  	 
     }

     @Override
     public String getReferencePriceAs32nd() {
         return com.core.match.util.MessageUtils.to32ndPrice(buffer, Offsets.ReferencePrice);  	 
     }

     @Override
     public double getReferencePriceAsDouble() {
         return com.core.match.util.MessageUtils.getDoublePrice(buffer, Offsets.ReferencePrice);  	 
     }

    @Override
    public void setReferencePrice(long value) {
    	com.core.util.MessageUtils.setLong(buffer, Offsets.ReferencePrice, value);  									
    }

    @Override
    public void setReferencePrice(double value) {
    	com.core.match.util.MessageUtils.setDoublePrice(buffer, Offsets.ReferencePrice, value);  									
    }

    @Override
    public boolean hasReferencePrice() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.ReferencePrice, Lengths.ReferencePrice);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Security");
        builder.append(",MsgType=");
        if (hasMsgType()) {
            builder.append(getMsgType());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",ContributorID=");
        if (hasContributorID()) {
            builder.append(getContributorID());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",ContributorSeq=");
        if (hasContributorSeq()) {
            builder.append(getContributorSeq());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Timestamp=");
        if (hasTimestamp()) {
            builder.append(getTimestampAsTime());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",SecurityID=");
        if (hasSecurityID()) {
            builder.append(getSecurityID());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Name=");
        if (hasName()) {
            builder.append(getNameAsString());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",CUSIP=");
        if (hasCUSIP()) {
            builder.append(getCUSIPAsString());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",MaturityDate=");
        if (hasMaturityDate()) {
            builder.append(getMaturityDate());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Coupon=");
        if (hasCoupon()) {
            builder.append(getCouponAs32nd());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Type=");
        if (hasType()) {
            builder.append(MatchConstants.SecurityType.toString(getType()));
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",IssueDate=");
        if (hasIssueDate()) {
            builder.append(getIssueDate());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",CouponFrequency=");
        if (hasCouponFrequency()) {
            builder.append(getCouponFrequency());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",TickSize=");
        if (hasTickSize()) {
            builder.append(getTickSizeAs32nd());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",LotSize=");
        if (hasLotSize()) {
            builder.append(getLotSizeAsQty());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",BloombergID=");
        if (hasBloombergID()) {
            builder.append(getBloombergIDAsString());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",NumLegs=");
        if (hasNumLegs()) {
            builder.append(getNumLegs());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Leg1ID=");
        if (hasLeg1ID()) {
            builder.append(getLeg1ID());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Leg2ID=");
        if (hasLeg2ID()) {
            builder.append(getLeg2ID());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Leg3ID=");
        if (hasLeg3ID()) {
            builder.append(getLeg3ID());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Leg1Size=");
        if (hasLeg1Size()) {
            builder.append(getLeg1Size());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Leg2Size=");
        if (hasLeg2Size()) {
            builder.append(getLeg2Size());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Leg3Size=");
        if (hasLeg3Size()) {
            builder.append(getLeg3Size());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",UnderlyingID=");
        if (hasUnderlyingID()) {
            builder.append(getUnderlyingID());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",ReferencePrice=");
        if (hasReferencePrice()) {
            builder.append(getReferencePriceAs32nd());
        }
        else {
            builder.append("<NULL>");
        }
        return builder.toString();        
    }

    private static class Offsets {
        static int MsgType = 0;
        static int ContributorID = 1;
        static int ContributorSeq = 3;
        static int Timestamp = 7;
        static int SecurityID = 15;
        static int Name = 17;
        static int CUSIP = 19;
        static int MaturityDate = 21;
        static int Coupon = 25;
        static int Type = 33;
        static int IssueDate = 34;
        static int CouponFrequency = 38;
        static int TickSize = 39;
        static int LotSize = 47;
        static int BloombergID = 51;
        static int NumLegs = 53;
        static int Leg1ID = 54;
        static int Leg2ID = 56;
        static int Leg3ID = 58;
        static int Leg1Size = 60;
        static int Leg2Size = 64;
        static int Leg3Size = 68;
        static int UnderlyingID = 72;
        static int ReferencePrice = 74;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int ContributorID = 2;
        static int ContributorSeq = 4;
        static int Timestamp = 8;
        static int SecurityID = 2;
        static int Name = 2;
        static int CUSIP = 2;
        static int MaturityDate = 4;
        static int Coupon = 8;
        static int Type = 1;
        static int IssueDate = 4;
        static int CouponFrequency = 1;
        static int TickSize = 8;
        static int LotSize = 4;
        static int BloombergID = 2;
        static int NumLegs = 1;
        static int Leg1ID = 2;
        static int Leg2ID = 2;
        static int Leg3ID = 2;
        static int Leg1Size = 4;
        static int Leg2Size = 4;
        static int Leg3Size = 4;
        static int UnderlyingID = 2;
        static int ReferencePrice = 8;
    }
} 
