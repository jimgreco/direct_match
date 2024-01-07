package com.core.match.itch.msgs;

import java.nio.ByteBuffer;

public class ITCHSecurityByteBufferMessage implements 
	ITCHSecurityEvent, 
	ITCHSecurityCommand {
    private static final int DEFAULT_LENGTH = 49;
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
    public void copy(ITCHSecurityEvent cmd) {
        setMsgType(cmd.getMsgType());
        setSecurityID(cmd.getSecurityID());
        setTimestamp(cmd.getTimestamp());
        setName(cmd.getName());
        setSecurityType(cmd.getSecurityType());
        setCoupon(cmd.getCoupon());
        setMaturityDate(cmd.getMaturityDate());
        setSecurityReference(cmd.getSecurityReference());
        setSecurityReferenceSource(cmd.getSecurityReferenceSource());
    }

    @Override
    public ITCHSecurityEvent toEvent() {
        return this;
    }

    @Override
    public ITCHSecurityCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "Security";
    }
 
    public ITCHSecurityCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('R');
        stringsBlockLength = 0;
        return this;
    }

    public ITCHSecurityEvent wrapEvent(ByteBuffer buf) {
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
     public java.nio.ByteBuffer getName() {
	     return com.core.util.MessageUtils.getRightPaddedFixedString(buffer, Offsets.Name, Lengths.Name);
     }

     @Override
     public int getNameLength() {
	     return com.core.util.MessageUtils.getFixedStringLength(buffer, Offsets.Name, Lengths.Name);
     }

     @Override
     public String getNameAsString() {
	     return com.core.util.MessageUtils.toRightPaddedFixedString(buffer, Offsets.Name, Lengths.Name);
     }

    @Override
    public void setName(java.nio.ByteBuffer value) {
    	com.core.util.MessageUtils.setRightPaddedFixedString(buffer, Offsets.Name, value, Lengths.Name);  						
    }

    @Override
    public void setName(String value) {
    	com.core.util.MessageUtils.setRightPaddedFixedString(buffer, Offsets.Name, value, Lengths.Name);  						
    }

    @Override
    public boolean hasName() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Name, Lengths.Name);  
    }

     @Override
     public char getSecurityType() {
         return com.core.util.MessageUtils.getChar(buffer, Offsets.SecurityType);  	 
     }

    @Override
    public void setSecurityType(char value) {
    	com.core.util.MessageUtils.setChar(buffer, Offsets.SecurityType, value);  									
    }

    @Override
    public boolean hasSecurityType() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.SecurityType, Lengths.SecurityType);  
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
     public java.nio.ByteBuffer getSecurityReference() {
	     return com.core.util.MessageUtils.getRightPaddedFixedString(buffer, Offsets.SecurityReference, Lengths.SecurityReference);
     }

     @Override
     public int getSecurityReferenceLength() {
	     return com.core.util.MessageUtils.getFixedStringLength(buffer, Offsets.SecurityReference, Lengths.SecurityReference);
     }

     @Override
     public String getSecurityReferenceAsString() {
	     return com.core.util.MessageUtils.toRightPaddedFixedString(buffer, Offsets.SecurityReference, Lengths.SecurityReference);
     }

    @Override
    public void setSecurityReference(java.nio.ByteBuffer value) {
    	com.core.util.MessageUtils.setRightPaddedFixedString(buffer, Offsets.SecurityReference, value, Lengths.SecurityReference);  						
    }

    @Override
    public void setSecurityReference(String value) {
    	com.core.util.MessageUtils.setRightPaddedFixedString(buffer, Offsets.SecurityReference, value, Lengths.SecurityReference);  						
    }

    @Override
    public boolean hasSecurityReference() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.SecurityReference, Lengths.SecurityReference);  
    }

     @Override
     public char getSecurityReferenceSource() {
         return com.core.util.MessageUtils.getChar(buffer, Offsets.SecurityReferenceSource);  	 
     }

    @Override
    public void setSecurityReferenceSource(char value) {
    	com.core.util.MessageUtils.setChar(buffer, Offsets.SecurityReferenceSource, value);  									
    }

    @Override
    public boolean hasSecurityReferenceSource() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.SecurityReferenceSource, Lengths.SecurityReferenceSource);  
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
        builder.append(",SecurityID=");
        if (hasSecurityID()) {
            builder.append(getSecurityID());
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
        builder.append(",Name=");
        if (hasName()) {
            builder.append(getNameAsString());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",SecurityType=");
        if (hasSecurityType()) {
            builder.append(getSecurityType());
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
        builder.append(",MaturityDate=");
        if (hasMaturityDate()) {
            builder.append(getMaturityDate());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",SecurityReference=");
        if (hasSecurityReference()) {
            builder.append(getSecurityReferenceAsString());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",SecurityReferenceSource=");
        if (hasSecurityReferenceSource()) {
            builder.append(getSecurityReferenceSource());
        }
        else {
            builder.append("<NULL>");
        }
        return builder.toString();        
    }

    private static class Offsets {
        static int MsgType = 0;
        static int SecurityID = 1;
        static int Timestamp = 3;
        static int Name = 11;
        static int SecurityType = 23;
        static int Coupon = 24;
        static int MaturityDate = 32;
        static int SecurityReference = 36;
        static int SecurityReferenceSource = 48;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int SecurityID = 2;
        static int Timestamp = 8;
        static int Name = 12;
        static int SecurityType = 1;
        static int Coupon = 8;
        static int MaturityDate = 4;
        static int SecurityReference = 12;
        static int SecurityReferenceSource = 1;
    }
} 
