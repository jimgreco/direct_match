package com.core.connector.soup.msgs;

import java.nio.ByteBuffer;

public class SoupUnsequencedDataByteBufferMessage implements 
	SoupUnsequencedDataEvent, 
	SoupUnsequencedDataCommand {
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
    public void copy(SoupUnsequencedDataEvent cmd) {
        setMsgLength(cmd.getMsgLength());
        setMsgType(cmd.getMsgType());
        setMessage(cmd.getMessage());
    }

    @Override
    public SoupUnsequencedDataEvent toEvent() {
        return this;
    }

    @Override
    public SoupUnsequencedDataCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "UnsequencedData";
    }
 
    public SoupUnsequencedDataCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('U');
        stringsBlockLength = 0;
        return this;
    }

    public SoupUnsequencedDataEvent wrapEvent(ByteBuffer buf) {
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
     public java.nio.ByteBuffer getMessage() {
		 return com.core.util.MessageUtils.getBytes(buffer, Offsets.Message, getLength() - Offsets.Message);
     }

     @Override
     public int getMessageLength() {
		 return com.core.util.MessageUtils.getBytesLength(buffer, Offsets.Message, getLength() - Offsets.Message);
     }

     @Override
     public String getMessageAsString() {
		 return com.core.util.MessageUtils.toString(buffer, Offsets.Message, getLength() - Offsets.Message);
     }

     @Override
     public String getMessageAsHexString() {
		 return com.core.util.MessageUtils.toHexString(buffer, Offsets.Message, getLength() - Offsets.Message);
     }

    @Override
    public void setMessage(java.nio.ByteBuffer value) {
		stringsBlockLength += com.core.util.MessageUtils.setBytes(buffer, Offsets.Message, value);  			
    }

    @Override
    public void setMessage(String value) {
		stringsBlockLength += com.core.util.MessageUtils.setBytes(buffer, Offsets.Message, value);  			
    }

    @Override
    public boolean hasMessage() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Message, Lengths.Message);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=UnsequencedData");
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
        builder.append(",Message=");
        if (hasMessage()) {
            builder.append(getMessageAsHexString());
        }
        else {
            builder.append("<NULL>");
        }
        return builder.toString();        
    }

    private static class Offsets {
        static int MsgLength = 0;
        static int MsgType = 2;
        static int Message = 3;
    }
	
    private static class Lengths {
        static int MsgLength = 2;
        static int MsgType = 1;
        static int Message = 0;
    }
} 
