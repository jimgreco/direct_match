package com.core.match.msgs;

import java.nio.ByteBuffer;

public class MatchOrderByteBufferMessage implements 
	MatchOrderEvent, 
	MatchOrderCommand {
    private static final int DEFAULT_LENGTH = 44;
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
    public void copy(MatchOrderEvent cmd) {
        setMsgType(cmd.getMsgType());
        setContributorID(cmd.getContributorID());
        setContributorSeq(cmd.getContributorSeq());
        setTimestamp(cmd.getTimestamp());
        setOrderID(cmd.getOrderID());
        setBuy(cmd.getBuy());
        setSecurityID(cmd.getSecurityID());
        setQty(cmd.getQty());
        setPrice(cmd.getPrice());
        setClOrdID(cmd.getClOrdID());
        setTraderID(cmd.getTraderID());
        setIOC(cmd.getIOC());
        setExternalOrderID(cmd.getExternalOrderID());
        setInBook(cmd.getInBook());
    }

    @Override
    public MatchOrderEvent toEvent() {
        return this;
    }

    @Override
    public MatchOrderCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "Order";
    }
 
    public MatchOrderCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('O');
        stringsBlockLength = 0;
        return this;
    }

    public MatchOrderEvent wrapEvent(ByteBuffer buf) {
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
     public int getOrderID() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.OrderID);  	 
     }

    @Override
    public void setOrderID(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.OrderID, value);  									
    }

    @Override
    public boolean hasOrderID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.OrderID, Lengths.OrderID);  
    }

     @Override
     public boolean getBuy() {
         return com.core.util.MessageUtils.getBool(buffer, Offsets.Buy);  	 
     }

    @Override
    public void setBuy(boolean value) {
    	com.core.util.MessageUtils.setBool(buffer, Offsets.Buy, value);  									
    }

    @Override
    public boolean hasBuy() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Buy, Lengths.Buy);  
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
     public int getQty() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.Qty);  	 
     }

     @Override
     public double getQtyAsQty() {
         return com.core.match.util.MessageUtils.toQtyRoundLot(buffer, Offsets.Qty);  	 
     }

    @Override
    public void setQty(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.Qty, value);  									
    }

    @Override
    public boolean hasQty() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Qty, Lengths.Qty);  
    }

     @Override
     public long getPrice() {
         return com.core.util.MessageUtils.getLong(buffer, Offsets.Price);  	 
     }

     @Override
     public String getPriceAs32nd() {
         return com.core.match.util.MessageUtils.to32ndPrice(buffer, Offsets.Price);  	 
     }

     @Override
     public double getPriceAsDouble() {
         return com.core.match.util.MessageUtils.getDoublePrice(buffer, Offsets.Price);  	 
     }

    @Override
    public void setPrice(long value) {
    	com.core.util.MessageUtils.setLong(buffer, Offsets.Price, value);  									
    }

    @Override
    public void setPrice(double value) {
    	com.core.match.util.MessageUtils.setDoublePrice(buffer, Offsets.Price, value);  									
    }

    @Override
    public boolean hasPrice() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Price, Lengths.Price);  
    }

     @Override
     public java.nio.ByteBuffer getClOrdID() {
		 return com.core.util.MessageUtils.getVariableString(buffer, Offsets.ClOrdID);
     }

     @Override
     public int getClOrdIDLength() {
		 return com.core.util.MessageUtils.getStringLength(buffer, Offsets.ClOrdID);
     }

     @Override
     public String getClOrdIDAsString() {
		 return com.core.util.MessageUtils.toVariableString(buffer, Offsets.ClOrdID);
     }

    @Override
    public void setClOrdID(java.nio.ByteBuffer value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.ClOrdID, value, getLength());  			
    }

    @Override
    public void setClOrdID(String value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.ClOrdID, value, getLength());  			
    }

    @Override
    public boolean hasClOrdID() {
		return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.ClOrdID, Lengths.ClOrdID) && getClOrdIDLength() > 0;  
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
     public boolean getIOC() {
         return com.core.util.MessageUtils.getBool(buffer, Offsets.IOC);  	 
     }

    @Override
    public void setIOC(boolean value) {
    	com.core.util.MessageUtils.setBool(buffer, Offsets.IOC, value);  									
    }

    @Override
    public boolean hasIOC() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.IOC, Lengths.IOC);  
    }

     @Override
     public int getExternalOrderID() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.ExternalOrderID);  	 
     }

    @Override
    public void setExternalOrderID(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.ExternalOrderID, value);  									
    }

    @Override
    public boolean hasExternalOrderID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.ExternalOrderID, Lengths.ExternalOrderID);  
    }

     @Override
     public boolean getInBook() {
         return com.core.util.MessageUtils.getBool(buffer, Offsets.InBook);  	 
     }

    @Override
    public void setInBook(boolean value) {
    	com.core.util.MessageUtils.setBool(buffer, Offsets.InBook, value);  									
    }

    @Override
    public boolean hasInBook() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.InBook, Lengths.InBook);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Order");
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
        builder.append(",OrderID=");
        if (hasOrderID()) {
            builder.append(getOrderID());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Buy=");
        if (hasBuy()) {
            builder.append(getBuy());
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
        builder.append(",Qty=");
        if (hasQty()) {
            builder.append(getQtyAsQty());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Price=");
        if (hasPrice()) {
            builder.append(getPriceAs32nd());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",ClOrdID=");
        if (hasClOrdID()) {
            builder.append(getClOrdIDAsString());
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
        builder.append(",IOC=");
        if (hasIOC()) {
            builder.append(getIOC());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",ExternalOrderID=");
        if (hasExternalOrderID()) {
            builder.append(getExternalOrderID());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",InBook=");
        if (hasInBook()) {
            builder.append(getInBook());
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
        static int OrderID = 15;
        static int Buy = 19;
        static int SecurityID = 20;
        static int Qty = 22;
        static int Price = 26;
        static int ClOrdID = 34;
        static int TraderID = 36;
        static int IOC = 38;
        static int ExternalOrderID = 39;
        static int InBook = 43;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int ContributorID = 2;
        static int ContributorSeq = 4;
        static int Timestamp = 8;
        static int OrderID = 4;
        static int Buy = 1;
        static int SecurityID = 2;
        static int Qty = 4;
        static int Price = 8;
        static int ClOrdID = 2;
        static int TraderID = 2;
        static int IOC = 1;
        static int ExternalOrderID = 4;
        static int InBook = 1;
    }
} 
