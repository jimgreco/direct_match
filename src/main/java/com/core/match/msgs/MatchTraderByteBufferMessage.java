package com.core.match.msgs;

import java.nio.ByteBuffer;

public class MatchTraderByteBufferMessage implements 
	MatchTraderEvent, 
	MatchTraderCommand {
    private static final int DEFAULT_LENGTH = 45;
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
    public void copy(MatchTraderEvent cmd) {
        setMsgType(cmd.getMsgType());
        setContributorID(cmd.getContributorID());
        setContributorSeq(cmd.getContributorSeq());
        setTimestamp(cmd.getTimestamp());
        setTraderID(cmd.getTraderID());
        setAccountID(cmd.getAccountID());
        setName(cmd.getName());
        setFatFinger2YLimit(cmd.getFatFinger2YLimit());
        setFatFinger3YLimit(cmd.getFatFinger3YLimit());
        setFatFinger5YLimit(cmd.getFatFinger5YLimit());
        setFatFinger7YLimit(cmd.getFatFinger7YLimit());
        setFatFinger10YLimit(cmd.getFatFinger10YLimit());
        setFatFinger30YLimit(cmd.getFatFinger30YLimit());
    }

    @Override
    public MatchTraderEvent toEvent() {
        return this;
    }

    @Override
    public MatchTraderCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "Trader";
    }
 
    public MatchTraderCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('T');
        stringsBlockLength = 0;
        return this;
    }

    public MatchTraderEvent wrapEvent(ByteBuffer buf) {
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
     public short getTraderID() {
         return com.core.util.MessageUtils.getShort(buffer, Offsets.TraderID);  	 
     }

    @Override
    public void setTraderID(short value) {
    	com.core.util.MessageUtils.setShort(buffer, Offsets.TraderID, value);  									
    }

    @Override
    public boolean hasTraderID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.TraderID, Lengths.TraderID);  
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
     public int getFatFinger2YLimit() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.FatFinger2YLimit);  	 
     }

    @Override
    public void setFatFinger2YLimit(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.FatFinger2YLimit, value);  									
    }

    @Override
    public boolean hasFatFinger2YLimit() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.FatFinger2YLimit, Lengths.FatFinger2YLimit);  
    }

     @Override
     public int getFatFinger3YLimit() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.FatFinger3YLimit);  	 
     }

    @Override
    public void setFatFinger3YLimit(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.FatFinger3YLimit, value);  									
    }

    @Override
    public boolean hasFatFinger3YLimit() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.FatFinger3YLimit, Lengths.FatFinger3YLimit);  
    }

     @Override
     public int getFatFinger5YLimit() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.FatFinger5YLimit);  	 
     }

    @Override
    public void setFatFinger5YLimit(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.FatFinger5YLimit, value);  									
    }

    @Override
    public boolean hasFatFinger5YLimit() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.FatFinger5YLimit, Lengths.FatFinger5YLimit);  
    }

     @Override
     public int getFatFinger7YLimit() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.FatFinger7YLimit);  	 
     }

    @Override
    public void setFatFinger7YLimit(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.FatFinger7YLimit, value);  									
    }

    @Override
    public boolean hasFatFinger7YLimit() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.FatFinger7YLimit, Lengths.FatFinger7YLimit);  
    }

     @Override
     public int getFatFinger10YLimit() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.FatFinger10YLimit);  	 
     }

    @Override
    public void setFatFinger10YLimit(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.FatFinger10YLimit, value);  									
    }

    @Override
    public boolean hasFatFinger10YLimit() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.FatFinger10YLimit, Lengths.FatFinger10YLimit);  
    }

     @Override
     public int getFatFinger30YLimit() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.FatFinger30YLimit);  	 
     }

    @Override
    public void setFatFinger30YLimit(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.FatFinger30YLimit, value);  									
    }

    @Override
    public boolean hasFatFinger30YLimit() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.FatFinger30YLimit, Lengths.FatFinger30YLimit);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Trader");
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
        builder.append(",TraderID=");
        if (hasTraderID()) {
            builder.append(getTraderID());
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
        builder.append(",FatFinger2YLimit=");
        if (hasFatFinger2YLimit()) {
            builder.append(getFatFinger2YLimit());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",FatFinger3YLimit=");
        if (hasFatFinger3YLimit()) {
            builder.append(getFatFinger3YLimit());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",FatFinger5YLimit=");
        if (hasFatFinger5YLimit()) {
            builder.append(getFatFinger5YLimit());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",FatFinger7YLimit=");
        if (hasFatFinger7YLimit()) {
            builder.append(getFatFinger7YLimit());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",FatFinger10YLimit=");
        if (hasFatFinger10YLimit()) {
            builder.append(getFatFinger10YLimit());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",FatFinger30YLimit=");
        if (hasFatFinger30YLimit()) {
            builder.append(getFatFinger30YLimit());
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
        static int TraderID = 15;
        static int AccountID = 17;
        static int Name = 19;
        static int FatFinger2YLimit = 21;
        static int FatFinger3YLimit = 25;
        static int FatFinger5YLimit = 29;
        static int FatFinger7YLimit = 33;
        static int FatFinger10YLimit = 37;
        static int FatFinger30YLimit = 41;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int ContributorID = 2;
        static int ContributorSeq = 4;
        static int Timestamp = 8;
        static int TraderID = 2;
        static int AccountID = 2;
        static int Name = 2;
        static int FatFinger2YLimit = 4;
        static int FatFinger3YLimit = 4;
        static int FatFinger5YLimit = 4;
        static int FatFinger7YLimit = 4;
        static int FatFinger10YLimit = 4;
        static int FatFinger30YLimit = 4;
    }
} 
