package com.core.match.ouch2.msgs;

import java.nio.ByteBuffer;

public class OUCH2OrderByteBufferMessage implements 
	OUCH2OrderEvent, 
	OUCH2OrderCommand {
    private static final int DEFAULT_LENGTH = 59;
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
    public void copy(OUCH2OrderEvent cmd) {
        setMsgType(cmd.getMsgType());
        setTimestamp(cmd.getTimestamp());
        setClOrdID(cmd.getClOrdID());
        setSide(cmd.getSide());
        setQty(cmd.getQty());
        setSecurity(cmd.getSecurity());
        setPrice(cmd.getPrice());
        setTimeInForce(cmd.getTimeInForce());
        setReserved(cmd.getReserved());
        setTrader(cmd.getTrader());
    }

    @Override
    public OUCH2OrderEvent toEvent() {
        return this;
    }

    @Override
    public OUCH2OrderCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "Order";
    }
 
    public OUCH2OrderCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('O');
        stringsBlockLength = 0;
        return this;
    }

    public OUCH2OrderEvent wrapEvent(ByteBuffer buf) {
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
     public long getClOrdID() {
         return com.core.util.MessageUtils.getLong(buffer, Offsets.ClOrdID);  	 
     }

    @Override
    public void setClOrdID(long value) {
    	com.core.util.MessageUtils.setLong(buffer, Offsets.ClOrdID, value);  									
    }

    @Override
    public boolean hasClOrdID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.ClOrdID, Lengths.ClOrdID);  
    }

     @Override
     public char getSide() {
         return com.core.util.MessageUtils.getChar(buffer, Offsets.Side);  	 
     }

    @Override
    public void setSide(char value) {
    	com.core.util.MessageUtils.setChar(buffer, Offsets.Side, value);  									
    }

    @Override
    public boolean hasSide() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Side, Lengths.Side);  
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
     public java.nio.ByteBuffer getSecurity() {
	     return com.core.util.MessageUtils.getRightPaddedFixedString(buffer, Offsets.Security, Lengths.Security);
     }

     @Override
     public int getSecurityLength() {
	     return com.core.util.MessageUtils.getFixedStringLength(buffer, Offsets.Security, Lengths.Security);
     }

     @Override
     public String getSecurityAsString() {
	     return com.core.util.MessageUtils.toRightPaddedFixedString(buffer, Offsets.Security, Lengths.Security);
     }

    @Override
    public void setSecurity(java.nio.ByteBuffer value) {
    	com.core.util.MessageUtils.setRightPaddedFixedString(buffer, Offsets.Security, value, Lengths.Security);  						
    }

    @Override
    public void setSecurity(String value) {
    	com.core.util.MessageUtils.setRightPaddedFixedString(buffer, Offsets.Security, value, Lengths.Security);  						
    }

    @Override
    public boolean hasSecurity() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Security, Lengths.Security);  
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
     public char getTimeInForce() {
         return com.core.util.MessageUtils.getChar(buffer, Offsets.TimeInForce);  	 
     }

    @Override
    public void setTimeInForce(char value) {
    	com.core.util.MessageUtils.setChar(buffer, Offsets.TimeInForce, value);  									
    }

    @Override
    public boolean hasTimeInForce() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.TimeInForce, Lengths.TimeInForce);  
    }

     @Override
     public int getReserved() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.Reserved);  	 
     }

    @Override
    public void setReserved(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.Reserved, value);  									
    }

    @Override
    public boolean hasReserved() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Reserved, Lengths.Reserved);  
    }

     @Override
     public java.nio.ByteBuffer getTrader() {
	     return com.core.util.MessageUtils.getRightPaddedFixedString(buffer, Offsets.Trader, Lengths.Trader);
     }

     @Override
     public int getTraderLength() {
	     return com.core.util.MessageUtils.getFixedStringLength(buffer, Offsets.Trader, Lengths.Trader);
     }

     @Override
     public String getTraderAsString() {
	     return com.core.util.MessageUtils.toRightPaddedFixedString(buffer, Offsets.Trader, Lengths.Trader);
     }

    @Override
    public void setTrader(java.nio.ByteBuffer value) {
    	com.core.util.MessageUtils.setRightPaddedFixedString(buffer, Offsets.Trader, value, Lengths.Trader);  						
    }

    @Override
    public void setTrader(String value) {
    	com.core.util.MessageUtils.setRightPaddedFixedString(buffer, Offsets.Trader, value, Lengths.Trader);  						
    }

    @Override
    public boolean hasTrader() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Trader, Lengths.Trader);  
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
        builder.append(",Timestamp=");
        if (hasTimestamp()) {
            builder.append(getTimestampAsTime());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",ClOrdID=");
        if (hasClOrdID()) {
            builder.append(getClOrdID());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Side=");
        if (hasSide()) {
            builder.append(getSide());
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
        builder.append(",Security=");
        if (hasSecurity()) {
            builder.append(getSecurityAsString());
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
        builder.append(",TimeInForce=");
        if (hasTimeInForce()) {
            builder.append(getTimeInForce());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Reserved=");
        if (hasReserved()) {
            builder.append(getReserved());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Trader=");
        if (hasTrader()) {
            builder.append(getTraderAsString());
        }
        else {
            builder.append("<NULL>");
        }
        return builder.toString();        
    }

    private static class Offsets {
        static int MsgType = 0;
        static int Timestamp = 1;
        static int ClOrdID = 9;
        static int Side = 17;
        static int Qty = 18;
        static int Security = 22;
        static int Price = 34;
        static int TimeInForce = 42;
        static int Reserved = 43;
        static int Trader = 47;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int Timestamp = 8;
        static int ClOrdID = 8;
        static int Side = 1;
        static int Qty = 4;
        static int Security = 12;
        static int Price = 8;
        static int TimeInForce = 1;
        static int Reserved = 4;
        static int Trader = 12;
    }
} 
