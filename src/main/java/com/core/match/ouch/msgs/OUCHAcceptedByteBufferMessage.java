package com.core.match.ouch.msgs;

import java.nio.ByteBuffer;

public class OUCHAcceptedByteBufferMessage implements 
	OUCHAcceptedEvent, 
	OUCHAcceptedCommand {
    private static final int DEFAULT_LENGTH = 51;
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
    public void copy(OUCHAcceptedEvent cmd) {
        setMsgType(cmd.getMsgType());
        setClOrdID(cmd.getClOrdID());
        setSide(cmd.getSide());
        setQty(cmd.getQty());
        setSecurity(cmd.getSecurity());
        setPrice(cmd.getPrice());
        setTimeInForce(cmd.getTimeInForce());
        setMaxDisplayedQty(cmd.getMaxDisplayedQty());
        setTrader(cmd.getTrader());
    }

    @Override
    public OUCHAcceptedEvent toEvent() {
        return this;
    }

    @Override
    public OUCHAcceptedCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "Accepted";
    }
 
    public OUCHAcceptedCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('A');
        stringsBlockLength = 0;
        return this;
    }

    public OUCHAcceptedEvent wrapEvent(ByteBuffer buf) {
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
     public int getMaxDisplayedQty() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.MaxDisplayedQty);  	 
     }

     @Override
     public double getMaxDisplayedQtyAsQty() {
         return com.core.match.util.MessageUtils.toExternalQtyRoundLot(buffer, Offsets.MaxDisplayedQty);  	 
     }

    @Override
    public void setMaxDisplayedQty(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.MaxDisplayedQty, value);  									
    }

    @Override
    public boolean hasMaxDisplayedQty() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.MaxDisplayedQty, Lengths.MaxDisplayedQty);  
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
        builder.append("Msg=Accepted");
        builder.append(",MsgType=");
        if (hasMsgType()) {
            builder.append(getMsgType());
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
        builder.append(",MaxDisplayedQty=");
        if (hasMaxDisplayedQty()) {
            builder.append(getMaxDisplayedQtyAsQty());
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
        static int ClOrdID = 1;
        static int Side = 9;
        static int Qty = 10;
        static int Security = 14;
        static int Price = 26;
        static int TimeInForce = 34;
        static int MaxDisplayedQty = 35;
        static int Trader = 39;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int ClOrdID = 8;
        static int Side = 1;
        static int Qty = 4;
        static int Security = 12;
        static int Price = 8;
        static int TimeInForce = 1;
        static int MaxDisplayedQty = 4;
        static int Trader = 12;
    }
} 
