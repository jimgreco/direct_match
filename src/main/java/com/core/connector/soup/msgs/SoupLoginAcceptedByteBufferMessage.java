package com.core.connector.soup.msgs;

import java.nio.ByteBuffer;

public class SoupLoginAcceptedByteBufferMessage implements 
	SoupLoginAcceptedEvent, 
	SoupLoginAcceptedCommand {
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
    public void copy(SoupLoginAcceptedEvent cmd) {
        setMsgLength(cmd.getMsgLength());
        setMsgType(cmd.getMsgType());
        setSession(cmd.getSession());
        setSequenceNumber(cmd.getSequenceNumber());
    }

    @Override
    public SoupLoginAcceptedEvent toEvent() {
        return this;
    }

    @Override
    public SoupLoginAcceptedCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "LoginAccepted";
    }
 
    public SoupLoginAcceptedCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('A');
        stringsBlockLength = 0;
        return this;
    }

    public SoupLoginAcceptedEvent wrapEvent(ByteBuffer buf) {
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
     public java.nio.ByteBuffer getSession() {
	     return com.core.util.MessageUtils.getLeftPaddedFixedString(buffer, Offsets.Session, Lengths.Session);
     }

     @Override
     public int getSessionLength() {
	     return com.core.util.MessageUtils.getFixedStringLength(buffer, Offsets.Session, Lengths.Session);
     }

     @Override
     public String getSessionAsString() {
	     return com.core.util.MessageUtils.toLeftPaddedFixedString(buffer, Offsets.Session, Lengths.Session);
     }

    @Override
    public void setSession(java.nio.ByteBuffer value) {
    	com.core.util.MessageUtils.setLeftPaddedFixedString(buffer, Offsets.Session, value, Lengths.Session);  						
    }

    @Override
    public void setSession(String value) {
    	com.core.util.MessageUtils.setLeftPaddedFixedString(buffer, Offsets.Session, value, Lengths.Session);  						
    }

    @Override
    public boolean hasSession() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Session, Lengths.Session);  
    }

     @Override
     public java.nio.ByteBuffer getSequenceNumber() {
	     return com.core.util.MessageUtils.getLeftPaddedFixedString(buffer, Offsets.SequenceNumber, Lengths.SequenceNumber);
     }

     @Override
     public int getSequenceNumberLength() {
	     return com.core.util.MessageUtils.getFixedStringLength(buffer, Offsets.SequenceNumber, Lengths.SequenceNumber);
     }

     @Override
     public String getSequenceNumberAsString() {
	     return com.core.util.MessageUtils.toLeftPaddedFixedString(buffer, Offsets.SequenceNumber, Lengths.SequenceNumber);
     }

    @Override
    public void setSequenceNumber(java.nio.ByteBuffer value) {
    	com.core.util.MessageUtils.setLeftPaddedFixedString(buffer, Offsets.SequenceNumber, value, Lengths.SequenceNumber);  						
    }

    @Override
    public void setSequenceNumber(String value) {
    	com.core.util.MessageUtils.setLeftPaddedFixedString(buffer, Offsets.SequenceNumber, value, Lengths.SequenceNumber);  						
    }

    @Override
    public boolean hasSequenceNumber() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.SequenceNumber, Lengths.SequenceNumber);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=LoginAccepted");
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
        builder.append(",Session=");
        if (hasSession()) {
            builder.append(getSessionAsString());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",SequenceNumber=");
        if (hasSequenceNumber()) {
            builder.append(getSequenceNumberAsString());
        }
        else {
            builder.append("<NULL>");
        }
        return builder.toString();        
    }

    private static class Offsets {
        static int MsgLength = 0;
        static int MsgType = 2;
        static int Session = 3;
        static int SequenceNumber = 13;
    }
	
    private static class Lengths {
        static int MsgLength = 2;
        static int MsgType = 1;
        static int Session = 10;
        static int SequenceNumber = 20;
    }
} 
