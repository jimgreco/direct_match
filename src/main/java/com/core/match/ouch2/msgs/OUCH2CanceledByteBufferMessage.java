package com.core.match.ouch2.msgs;

import java.nio.ByteBuffer;

public class OUCH2CanceledByteBufferMessage implements 
	OUCH2CanceledEvent, 
	OUCH2CanceledCommand {
    private static final int DEFAULT_LENGTH = 22;
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
    public void copy(OUCH2CanceledEvent cmd) {
        setMsgType(cmd.getMsgType());
        setTimestamp(cmd.getTimestamp());
        setClOrdID(cmd.getClOrdID());
        setReason(cmd.getReason());
        setCanceledQty(cmd.getCanceledQty());
    }

    @Override
    public OUCH2CanceledEvent toEvent() {
        return this;
    }

    @Override
    public OUCH2CanceledCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "Canceled";
    }
 
    public OUCH2CanceledCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('C');
        stringsBlockLength = 0;
        return this;
    }

    public OUCH2CanceledEvent wrapEvent(ByteBuffer buf) {
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
     public char getReason() {
         return com.core.util.MessageUtils.getChar(buffer, Offsets.Reason);  	 
     }

    @Override
    public void setReason(char value) {
    	com.core.util.MessageUtils.setChar(buffer, Offsets.Reason, value);  									
    }

    @Override
    public boolean hasReason() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Reason, Lengths.Reason);  
    }

     @Override
     public int getCanceledQty() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.CanceledQty);  	 
     }

     @Override
     public double getCanceledQtyAsQty() {
         return com.core.match.util.MessageUtils.toExternalQtyRoundLot(buffer, Offsets.CanceledQty);  	 
     }

    @Override
    public void setCanceledQty(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.CanceledQty, value);  									
    }

    @Override
    public boolean hasCanceledQty() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.CanceledQty, Lengths.CanceledQty);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Canceled");
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
        builder.append(",Reason=");
        if (hasReason()) {
            builder.append(getReason());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",CanceledQty=");
        if (hasCanceledQty()) {
            builder.append(getCanceledQtyAsQty());
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
        static int Reason = 17;
        static int CanceledQty = 18;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int Timestamp = 8;
        static int ClOrdID = 8;
        static int Reason = 1;
        static int CanceledQty = 4;
    }
} 
