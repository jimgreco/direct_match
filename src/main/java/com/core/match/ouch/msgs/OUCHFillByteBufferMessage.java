package com.core.match.ouch.msgs;

import java.nio.ByteBuffer;

public class OUCHFillByteBufferMessage implements 
	OUCHFillEvent, 
	OUCHFillCommand {
    private static final int DEFAULT_LENGTH = 25;
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
    public void copy(OUCHFillEvent cmd) {
        setMsgType(cmd.getMsgType());
        setClOrdID(cmd.getClOrdID());
        setExecutionQty(cmd.getExecutionQty());
        setExecutionPrice(cmd.getExecutionPrice());
        setMatchID(cmd.getMatchID());
    }

    @Override
    public OUCHFillEvent toEvent() {
        return this;
    }

    @Override
    public OUCHFillCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "Fill";
    }
 
    public OUCHFillCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('E');
        stringsBlockLength = 0;
        return this;
    }

    public OUCHFillEvent wrapEvent(ByteBuffer buf) {
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
     public int getExecutionQty() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.ExecutionQty);  	 
     }

     @Override
     public double getExecutionQtyAsQty() {
         return com.core.match.util.MessageUtils.toExternalQtyRoundLot(buffer, Offsets.ExecutionQty);  	 
     }

    @Override
    public void setExecutionQty(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.ExecutionQty, value);  									
    }

    @Override
    public boolean hasExecutionQty() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.ExecutionQty, Lengths.ExecutionQty);  
    }

     @Override
     public long getExecutionPrice() {
         return com.core.util.MessageUtils.getLong(buffer, Offsets.ExecutionPrice);  	 
     }

     @Override
     public String getExecutionPriceAs32nd() {
         return com.core.match.util.MessageUtils.to32ndPrice(buffer, Offsets.ExecutionPrice);  	 
     }

     @Override
     public double getExecutionPriceAsDouble() {
         return com.core.match.util.MessageUtils.getDoublePrice(buffer, Offsets.ExecutionPrice);  	 
     }

    @Override
    public void setExecutionPrice(long value) {
    	com.core.util.MessageUtils.setLong(buffer, Offsets.ExecutionPrice, value);  									
    }

    @Override
    public void setExecutionPrice(double value) {
    	com.core.match.util.MessageUtils.setDoublePrice(buffer, Offsets.ExecutionPrice, value);  									
    }

    @Override
    public boolean hasExecutionPrice() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.ExecutionPrice, Lengths.ExecutionPrice);  
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
        builder.append("Msg=Fill");
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
        builder.append(",ExecutionQty=");
        if (hasExecutionQty()) {
            builder.append(getExecutionQtyAsQty());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",ExecutionPrice=");
        if (hasExecutionPrice()) {
            builder.append(getExecutionPriceAs32nd());
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
        static int ClOrdID = 1;
        static int ExecutionQty = 9;
        static int ExecutionPrice = 13;
        static int MatchID = 21;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int ClOrdID = 8;
        static int ExecutionQty = 4;
        static int ExecutionPrice = 8;
        static int MatchID = 4;
    }
} 
