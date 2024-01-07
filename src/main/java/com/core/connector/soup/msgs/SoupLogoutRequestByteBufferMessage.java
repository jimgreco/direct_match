package com.core.connector.soup.msgs;

import java.nio.ByteBuffer;

public class SoupLogoutRequestByteBufferMessage implements 
	SoupLogoutRequestEvent, 
	SoupLogoutRequestCommand {
    private static final int DEFAULT_LENGTH = 3;
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
    public void copy(SoupLogoutRequestEvent cmd) {
        setMsgLength(cmd.getMsgLength());
        setMsgType(cmd.getMsgType());
    }

    @Override
    public SoupLogoutRequestEvent toEvent() {
        return this;
    }

    @Override
    public SoupLogoutRequestCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "LogoutRequest";
    }
 
    public SoupLogoutRequestCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('O');
        stringsBlockLength = 0;
        return this;
    }

    public SoupLogoutRequestEvent wrapEvent(ByteBuffer buf) {
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=LogoutRequest");
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
        return builder.toString();        
    }

    private static class Offsets {
        static int MsgLength = 0;
        static int MsgType = 2;
    }
	
    private static class Lengths {
        static int MsgLength = 2;
        static int MsgType = 1;
    }
} 
