package com.core.match.itch.msgs;

import java.nio.ByteBuffer;

public class ITCHOrderExecutedByteBufferMessage implements 
	ITCHOrderExecutedEvent, 
	ITCHOrderExecutedCommand {
    private static final int DEFAULT_LENGTH = 31;
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
    public void copy(ITCHOrderExecutedEvent cmd) {
        setMsgType(cmd.getMsgType());
        setSecurityID(cmd.getSecurityID());
        setTimestamp(cmd.getTimestamp());
        setOrderID(cmd.getOrderID());
        setQty(cmd.getQty());
        setPrice(cmd.getPrice());
        setMatchID(cmd.getMatchID());
    }

    @Override
    public ITCHOrderExecutedEvent toEvent() {
        return this;
    }

    @Override
    public ITCHOrderExecutedCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "OrderExecuted";
    }
 
    public ITCHOrderExecutedCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('E');
        stringsBlockLength = 0;
        return this;
    }

    public ITCHOrderExecutedEvent wrapEvent(ByteBuffer buf) {
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
     public int getQty() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.Qty);  	 
     }

     @Override
     public double getQtyAsQty() {
         return com.core.match.util.MessageUtils.toExternalQtyRoundLot(buffer, Offsets.Qty);  	 
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
     public int getMatchID() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.MatchID);  	 
     }

    @Override
    public void setMatchID(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.MatchID, value);  									
    }

    @Override
    public boolean hasMatchID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.MatchID, Lengths.MatchID);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=OrderExecuted");
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
        builder.append(",OrderID=");
        if (hasOrderID()) {
            builder.append(getOrderID());
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
        builder.append(",MatchID=");
        if (hasMatchID()) {
            builder.append(getMatchID());
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
        static int OrderID = 11;
        static int Qty = 15;
        static int Price = 19;
        static int MatchID = 27;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int SecurityID = 2;
        static int Timestamp = 8;
        static int OrderID = 4;
        static int Qty = 4;
        static int Price = 8;
        static int MatchID = 4;
    }
} 
