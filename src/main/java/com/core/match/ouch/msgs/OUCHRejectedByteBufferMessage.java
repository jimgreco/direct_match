package com.core.match.ouch.msgs;

import java.nio.ByteBuffer;

public class OUCHRejectedByteBufferMessage implements 
	OUCHRejectedEvent, 
	OUCHRejectedCommand {
    private static final int DEFAULT_LENGTH = 10;
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
    public void copy(OUCHRejectedEvent cmd) {
        setMsgType(cmd.getMsgType());
        setClOrdID(cmd.getClOrdID());
        setReason(cmd.getReason());
    }

    @Override
    public OUCHRejectedEvent toEvent() {
        return this;
    }

    @Override
    public OUCHRejectedCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "Rejected";
    }
 
    public OUCHRejectedCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('J');
        stringsBlockLength = 0;
        return this;
    }

    public OUCHRejectedEvent wrapEvent(ByteBuffer buf) {
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Rejected");
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
        builder.append(",Reason=");
        if (hasReason()) {
            builder.append(getReason());
        }
        else {
            builder.append("<NULL>");
        }
        return builder.toString();        
    }

    private static class Offsets {
        static int MsgType = 0;
        static int ClOrdID = 1;
        static int Reason = 9;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int ClOrdID = 8;
        static int Reason = 1;
    }
} 
