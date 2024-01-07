package com.core.match.ouch.msgs;

import java.nio.ByteBuffer;

public class OUCHReplacedByteBufferMessage implements 
	OUCHReplacedEvent, 
	OUCHReplacedCommand {
    private static final int DEFAULT_LENGTH = 33;
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
    public void copy(OUCHReplacedEvent cmd) {
        setMsgType(cmd.getMsgType());
        setClOrdID(cmd.getClOrdID());
        setOldClOrdId(cmd.getOldClOrdId());
        setQty(cmd.getQty());
        setPrice(cmd.getPrice());
        setMaxDisplayedQty(cmd.getMaxDisplayedQty());
    }

    @Override
    public OUCHReplacedEvent toEvent() {
        return this;
    }

    @Override
    public OUCHReplacedCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "Replaced";
    }
 
    public OUCHReplacedCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('M');
        stringsBlockLength = 0;
        return this;
    }

    public OUCHReplacedEvent wrapEvent(ByteBuffer buf) {
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
        builder.append(",MaxDisplayedQty=");
        if (hasMaxDisplayedQty()) {
            builder.append(getMaxDisplayedQtyAsQty());
        }
        else {
            builder.append("<NULL>");
        }
        return builder.toString();        
    }

    private static class Offsets {
        static int MsgType = 0;
        static int ClOrdID = 1;
        static int OldClOrdId = 9;
        static int Qty = 17;
        static int Price = 21;
        static int MaxDisplayedQty = 29;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int ClOrdID = 8;
        static int OldClOrdId = 8;
        static int Qty = 4;
        static int Price = 8;
        static int MaxDisplayedQty = 4;
    }
} 
