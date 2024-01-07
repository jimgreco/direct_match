package com.core.match.msgs;

import java.nio.ByteBuffer;

public class MatchAccountByteBufferMessage implements 
	MatchAccountEvent, 
	MatchAccountCommand {
    private static final int DEFAULT_LENGTH = 34;
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
    public void copy(MatchAccountEvent cmd) {
        setMsgType(cmd.getMsgType());
        setContributorID(cmd.getContributorID());
        setContributorSeq(cmd.getContributorSeq());
        setTimestamp(cmd.getTimestamp());
        setAccountID(cmd.getAccountID());
        setName(cmd.getName());
        setNetDV01Limit(cmd.getNetDV01Limit());
        setCommission(cmd.getCommission());
        setSSGMID(cmd.getSSGMID());
        setNettingClearing(cmd.getNettingClearing());
    }

    @Override
    public MatchAccountEvent toEvent() {
        return this;
    }

    @Override
    public MatchAccountCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "Account";
    }
 
    public MatchAccountCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('A');
        stringsBlockLength = 0;
        return this;
    }

    public MatchAccountEvent wrapEvent(ByteBuffer buf) {
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
     public short getAccountID() {
         return com.core.util.MessageUtils.getShort(buffer, Offsets.AccountID);  	 
     }

    @Override
    public void setAccountID(short value) {
    	com.core.util.MessageUtils.setShort(buffer, Offsets.AccountID, value);  									
    }

    @Override
    public boolean hasAccountID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.AccountID, Lengths.AccountID);  
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
     public int getNetDV01Limit() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.NetDV01Limit);  	 
     }

    @Override
    public void setNetDV01Limit(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.NetDV01Limit, value);  									
    }

    @Override
    public boolean hasNetDV01Limit() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.NetDV01Limit, Lengths.NetDV01Limit);  
    }

     @Override
     public long getCommission() {
         return com.core.util.MessageUtils.getLong(buffer, Offsets.Commission);  	 
     }

     @Override
     public String getCommissionAs32nd() {
         return com.core.match.util.MessageUtils.to32ndPrice(buffer, Offsets.Commission);  	 
     }

     @Override
     public double getCommissionAsDouble() {
         return com.core.match.util.MessageUtils.getDoublePrice(buffer, Offsets.Commission);  	 
     }

    @Override
    public void setCommission(long value) {
    	com.core.util.MessageUtils.setLong(buffer, Offsets.Commission, value);  									
    }

    @Override
    public void setCommission(double value) {
    	com.core.match.util.MessageUtils.setDoublePrice(buffer, Offsets.Commission, value);  									
    }

    @Override
    public boolean hasCommission() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Commission, Lengths.Commission);  
    }

     @Override
     public java.nio.ByteBuffer getSSGMID() {
		 return com.core.util.MessageUtils.getVariableString(buffer, Offsets.SSGMID);
     }

     @Override
     public int getSSGMIDLength() {
		 return com.core.util.MessageUtils.getStringLength(buffer, Offsets.SSGMID);
     }

     @Override
     public String getSSGMIDAsString() {
		 return com.core.util.MessageUtils.toVariableString(buffer, Offsets.SSGMID);
     }

    @Override
    public void setSSGMID(java.nio.ByteBuffer value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.SSGMID, value, getLength());  			
    }

    @Override
    public void setSSGMID(String value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.SSGMID, value, getLength());  			
    }

    @Override
    public boolean hasSSGMID() {
		return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.SSGMID, Lengths.SSGMID) && getSSGMIDLength() > 0;  
    }

     @Override
     public boolean getNettingClearing() {
         return com.core.util.MessageUtils.getBool(buffer, Offsets.NettingClearing);  	 
     }

    @Override
    public void setNettingClearing(boolean value) {
    	com.core.util.MessageUtils.setBool(buffer, Offsets.NettingClearing, value);  									
    }

    @Override
    public boolean hasNettingClearing() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.NettingClearing, Lengths.NettingClearing);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Account");
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
        builder.append(",AccountID=");
        if (hasAccountID()) {
            builder.append(getAccountID());
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
        builder.append(",NetDV01Limit=");
        if (hasNetDV01Limit()) {
            builder.append(getNetDV01Limit());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Commission=");
        if (hasCommission()) {
            builder.append(getCommissionAs32nd());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",SSGMID=");
        if (hasSSGMID()) {
            builder.append(getSSGMIDAsString());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",NettingClearing=");
        if (hasNettingClearing()) {
            builder.append(getNettingClearing());
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
        static int AccountID = 15;
        static int Name = 17;
        static int NetDV01Limit = 19;
        static int Commission = 23;
        static int SSGMID = 31;
        static int NettingClearing = 33;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int ContributorID = 2;
        static int ContributorSeq = 4;
        static int Timestamp = 8;
        static int AccountID = 2;
        static int Name = 2;
        static int NetDV01Limit = 4;
        static int Commission = 8;
        static int SSGMID = 2;
        static int NettingClearing = 1;
    }
} 
