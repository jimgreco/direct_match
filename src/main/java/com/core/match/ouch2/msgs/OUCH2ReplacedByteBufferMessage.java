package com.core.match.ouch2.msgs;

import java.nio.ByteBuffer;

public class OUCH2ReplacedByteBufferMessage implements 
	OUCH2ReplacedEvent, 
	OUCH2ReplacedCommand {
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
    public void copy(OUCH2ReplacedEvent cmd) {
        setMsgType(cmd.getMsgType());
        setTimestamp(cmd.getTimestamp());
        setClOrdID(cmd.getClOrdID());
        setOldClOrdId(cmd.getOldClOrdId());
        setQty(cmd.getQty());
        setPrice(cmd.getPrice());
        setReserved(cmd.getReserved());
        setExternalOrderID(cmd.getExternalOrderID());
    }

    @Override
    public OUCH2ReplacedEvent toEvent() {
        return this;
    }

    @Override
    public OUCH2ReplacedCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "Replaced";
    }
 
    public OUCH2ReplacedCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('M');
        stringsBlockLength = 0;
        return this;
    }

    public OUCH2ReplacedEvent wrapEvent(ByteBuffer buf) {
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
     public long getOldClOrdId() {
         return com.core.util.MessageUtils.getLong(buffer, Offsets.OldClOrdId);  	 
     }

    @Override
    public void setOldClOrdId(long value) {
    	com.core.util.MessageUtils.setLong(buffer, Offsets.OldClOrdId, value);  									
    }

    @Override
    public boolean hasOldClOrdId() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.OldClOrdId, Lengths.OldClOrdId);  
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
     public int getReserved() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.Reserved);  	 
     }

     @Override
     public double getReservedAsQty() {
         return com.core.match.util.MessageUtils.toExternalQtyRoundLot(buffer, Offsets.Reserved);  	 
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Replaced");
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
        builder.append(",OldClOrdId=");
        if (hasOldClOrdId()) {
            builder.append(getOldClOrdId());
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
        builder.append(",Reserved=");
        if (hasReserved()) {
            builder.append(getReservedAsQty());
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
        return builder.toString();        
    }

    private static class Offsets {
        static int MsgType = 0;
        static int Timestamp = 1;
        static int ClOrdID = 9;
        static int OldClOrdId = 17;
        static int Qty = 25;
        static int Price = 29;
        static int Reserved = 37;
        static int ExternalOrderID = 41;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int Timestamp = 8;
        static int ClOrdID = 8;
        static int OldClOrdId = 8;
        static int Qty = 4;
        static int Price = 8;
        static int Reserved = 4;
        static int ExternalOrderID = 4;
    }
} 
