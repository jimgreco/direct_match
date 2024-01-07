package com.core.match.ouch.msgs;

import java.nio.ByteBuffer;

public class OUCHReplaceByteBufferMessage implements 
	OUCHReplaceEvent, 
	OUCHReplaceCommand {
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
    public void copy(OUCHReplaceEvent cmd) {
        setMsgType(cmd.getMsgType());
        setClOrdID(cmd.getClOrdID());
        setNewClOrdID(cmd.getNewClOrdID());
        setNewQty(cmd.getNewQty());
        setNewPrice(cmd.getNewPrice());
        setNewMaxDisplayedQty(cmd.getNewMaxDisplayedQty());
    }

    @Override
    public OUCHReplaceEvent toEvent() {
        return this;
    }

    @Override
    public OUCHReplaceCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "Replace";
    }
 
    public OUCHReplaceCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('U');
        stringsBlockLength = 0;
        return this;
    }

    public OUCHReplaceEvent wrapEvent(ByteBuffer buf) {
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
     public long getNewClOrdID() {
         return com.core.util.MessageUtils.getLong(buffer, Offsets.NewClOrdID);  	 
     }

    @Override
    public void setNewClOrdID(long value) {
    	com.core.util.MessageUtils.setLong(buffer, Offsets.NewClOrdID, value);  									
    }

    @Override
    public boolean hasNewClOrdID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.NewClOrdID, Lengths.NewClOrdID);  
    }

     @Override
     public int getNewQty() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.NewQty);  	 
     }

     @Override
     public double getNewQtyAsQty() {
         return com.core.match.util.MessageUtils.toExternalQtyRoundLot(buffer, Offsets.NewQty);  	 
     }

    @Override
    public void setNewQty(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.NewQty, value);  									
    }

    @Override
    public boolean hasNewQty() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.NewQty, Lengths.NewQty);  
    }

     @Override
     public long getNewPrice() {
         return com.core.util.MessageUtils.getLong(buffer, Offsets.NewPrice);  	 
     }

     @Override
     public String getNewPriceAs32nd() {
         return com.core.match.util.MessageUtils.to32ndPrice(buffer, Offsets.NewPrice);  	 
     }

     @Override
     public double getNewPriceAsDouble() {
         return com.core.match.util.MessageUtils.getDoublePrice(buffer, Offsets.NewPrice);  	 
     }

    @Override
    public void setNewPrice(long value) {
    	com.core.util.MessageUtils.setLong(buffer, Offsets.NewPrice, value);  									
    }

    @Override
    public void setNewPrice(double value) {
    	com.core.match.util.MessageUtils.setDoublePrice(buffer, Offsets.NewPrice, value);  									
    }

    @Override
    public boolean hasNewPrice() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.NewPrice, Lengths.NewPrice);  
    }

     @Override
     public int getNewMaxDisplayedQty() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.NewMaxDisplayedQty);  	 
     }

     @Override
     public double getNewMaxDisplayedQtyAsQty() {
         return com.core.match.util.MessageUtils.toExternalQtyRoundLot(buffer, Offsets.NewMaxDisplayedQty);  	 
     }

    @Override
    public void setNewMaxDisplayedQty(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.NewMaxDisplayedQty, value);  									
    }

    @Override
    public boolean hasNewMaxDisplayedQty() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.NewMaxDisplayedQty, Lengths.NewMaxDisplayedQty);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Replace");
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
        builder.append(",NewClOrdID=");
        if (hasNewClOrdID()) {
            builder.append(getNewClOrdID());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",NewQty=");
        if (hasNewQty()) {
            builder.append(getNewQtyAsQty());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",NewPrice=");
        if (hasNewPrice()) {
            builder.append(getNewPriceAs32nd());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",NewMaxDisplayedQty=");
        if (hasNewMaxDisplayedQty()) {
            builder.append(getNewMaxDisplayedQtyAsQty());
        }
        else {
            builder.append("<NULL>");
        }
        return builder.toString();        
    }

    private static class Offsets {
        static int MsgType = 0;
        static int ClOrdID = 1;
        static int NewClOrdID = 9;
        static int NewQty = 17;
        static int NewPrice = 21;
        static int NewMaxDisplayedQty = 29;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int ClOrdID = 8;
        static int NewClOrdID = 8;
        static int NewQty = 4;
        static int NewPrice = 8;
        static int NewMaxDisplayedQty = 4;
    }
} 
