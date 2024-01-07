package com.core.connector.soup.msgs;

import java.nio.ByteBuffer;

public class SoupLoginRejectedByteBufferMessage implements 
	SoupLoginRejectedEvent, 
	SoupLoginRejectedCommand {
    private static final int DEFAULT_LENGTH = 4;
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
    public void copy(SoupLoginRejectedEvent cmd) {
        setMsgLength(cmd.getMsgLength());
        setMsgType(cmd.getMsgType());
        setRejectReasonCode(cmd.getRejectReasonCode());
    }

    @Override
    public SoupLoginRejectedEvent toEvent() {
        return this;
    }

    @Override
    public SoupLoginRejectedCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "LoginRejected";
    }
 
    public SoupLoginRejectedCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('J');
        stringsBlockLength = 0;
        return this;
    }

    public SoupLoginRejectedEvent wrapEvent(ByteBuffer buf) {
        buffer = buf;
		setLength(buffer.remaining());
        return this;
    }

     @Override
     public short getMsgLength() {
         return com.core.util.MessageUtils.getShort(buffer, Offsets.MsgLength);  	 
     }

    @Override
    public void setMsgLength(short value) {
    	com.core.util.MessageUtils.setShort(buffer, Offsets.MsgLength, value);  									
    }

    @Override
    public boolean hasMsgLength() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.MsgLength, Lengths.MsgLength);  
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
     public char getRejectReasonCode() {
         return com.core.util.MessageUtils.getChar(buffer, Offsets.RejectReasonCode);  	 
     }

    @Override
    public void setRejectReasonCode(char value) {
    	com.core.util.MessageUtils.setChar(buffer, Offsets.RejectReasonCode, value);  									
    }

    @Override
    public boolean hasRejectReasonCode() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.RejectReasonCode, Lengths.RejectReasonCode);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=LoginRejected");
        builder.append(",MsgLength=");
        if (hasMsgLength()) {
            builder.append(getMsgLength());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",MsgType=");
        if (hasMsgType()) {
            builder.append(getMsgType());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",RejectReasonCode=");
        if (hasRejectReasonCode()) {
            builder.append(getRejectReasonCode());
        }
        else {
            builder.append("<NULL>");
        }
        return builder.toString();        
    }

    private static class Offsets {
        static int MsgLength = 0;
        static int MsgType = 2;
        static int RejectReasonCode = 3;
    }
	
    private static class Lengths {
        static int MsgLength = 2;
        static int MsgType = 1;
        static int RejectReasonCode = 1;
    }
} 
