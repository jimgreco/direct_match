package com.core.connector.soup.msgs;

import java.nio.ByteBuffer;

public class SoupDebugByteBufferMessage implements 
	SoupDebugEvent, 
	SoupDebugCommand {
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
    public void copy(SoupDebugEvent cmd) {
        setMsgLength(cmd.getMsgLength());
        setMsgType(cmd.getMsgType());
        setText(cmd.getText());
    }

    @Override
    public SoupDebugEvent toEvent() {
        return this;
    }

    @Override
    public SoupDebugCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "Debug";
    }
 
    public SoupDebugCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('+');
        stringsBlockLength = 0;
        return this;
    }

    public SoupDebugEvent wrapEvent(ByteBuffer buf) {
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
     public java.nio.ByteBuffer getText() {
		 return com.core.util.MessageUtils.getBytes(buffer, Offsets.Text, getLength() - Offsets.Text);
     }

     @Override
     public int getTextLength() {
		 return com.core.util.MessageUtils.getBytesLength(buffer, Offsets.Text, getLength() - Offsets.Text);
     }

     @Override
     public String getTextAsString() {
		 return com.core.util.MessageUtils.toString(buffer, Offsets.Text, getLength() - Offsets.Text);
     }

     @Override
     public String getTextAsHexString() {
		 return com.core.util.MessageUtils.toHexString(buffer, Offsets.Text, getLength() - Offsets.Text);
     }

    @Override
    public void setText(java.nio.ByteBuffer value) {
		stringsBlockLength += com.core.util.MessageUtils.setBytes(buffer, Offsets.Text, value);  			
    }

    @Override
    public void setText(String value) {
		stringsBlockLength += com.core.util.MessageUtils.setBytes(buffer, Offsets.Text, value);  			
    }

    @Override
    public boolean hasText() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Text, Lengths.Text);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Debug");
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
        builder.append(",Text=");
        if (hasText()) {
            builder.append(getTextAsHexString());
        }
        else {
            builder.append("<NULL>");
        }
        return builder.toString();        
    }

    private static class Offsets {
        static int MsgLength = 0;
        static int MsgType = 2;
        static int Text = 3;
    }
	
    private static class Lengths {
        static int MsgLength = 2;
        static int MsgType = 1;
        static int Text = 0;
    }
} 
