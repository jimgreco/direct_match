package com.core.connector.soup.msgs;

import java.nio.ByteBuffer;

public class SoupLoginRequestByteBufferMessage implements 
	SoupLoginRequestEvent, 
	SoupLoginRequestCommand {
    private static final int DEFAULT_LENGTH = 49;
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
    public void copy(SoupLoginRequestEvent cmd) {
        setMsgLength(cmd.getMsgLength());
        setMsgType(cmd.getMsgType());
        setUsername(cmd.getUsername());
        setPassword(cmd.getPassword());
        setRequestedSession(cmd.getRequestedSession());
        setRequestedSequenceNumber(cmd.getRequestedSequenceNumber());
    }

    @Override
    public SoupLoginRequestEvent toEvent() {
        return this;
    }

    @Override
    public SoupLoginRequestCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "LoginRequest";
    }
 
    public SoupLoginRequestCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('L');
        stringsBlockLength = 0;
        return this;
    }

    public SoupLoginRequestEvent wrapEvent(ByteBuffer buf) {
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
     public java.nio.ByteBuffer getUsername() {
	     return com.core.util.MessageUtils.getRightPaddedFixedString(buffer, Offsets.Username, Lengths.Username);
     }

     @Override
     public int getUsernameLength() {
	     return com.core.util.MessageUtils.getFixedStringLength(buffer, Offsets.Username, Lengths.Username);
     }

     @Override
     public String getUsernameAsString() {
	     return com.core.util.MessageUtils.toRightPaddedFixedString(buffer, Offsets.Username, Lengths.Username);
     }

    @Override
    public void setUsername(java.nio.ByteBuffer value) {
    	com.core.util.MessageUtils.setRightPaddedFixedString(buffer, Offsets.Username, value, Lengths.Username);  						
    }

    @Override
    public void setUsername(String value) {
    	com.core.util.MessageUtils.setRightPaddedFixedString(buffer, Offsets.Username, value, Lengths.Username);  						
    }

    @Override
    public boolean hasUsername() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Username, Lengths.Username);  
    }

     @Override
     public java.nio.ByteBuffer getPassword() {
	     return com.core.util.MessageUtils.getRightPaddedFixedString(buffer, Offsets.Password, Lengths.Password);
     }

     @Override
     public int getPasswordLength() {
	     return com.core.util.MessageUtils.getFixedStringLength(buffer, Offsets.Password, Lengths.Password);
     }

     @Override
     public String getPasswordAsString() {
	     return com.core.util.MessageUtils.toRightPaddedFixedString(buffer, Offsets.Password, Lengths.Password);
     }

    @Override
    public void setPassword(java.nio.ByteBuffer value) {
    	com.core.util.MessageUtils.setRightPaddedFixedString(buffer, Offsets.Password, value, Lengths.Password);  						
    }

    @Override
    public void setPassword(String value) {
    	com.core.util.MessageUtils.setRightPaddedFixedString(buffer, Offsets.Password, value, Lengths.Password);  						
    }

    @Override
    public boolean hasPassword() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Password, Lengths.Password);  
    }

     @Override
     public java.nio.ByteBuffer getRequestedSession() {
	     return com.core.util.MessageUtils.getLeftPaddedFixedString(buffer, Offsets.RequestedSession, Lengths.RequestedSession);
     }

     @Override
     public int getRequestedSessionLength() {
	     return com.core.util.MessageUtils.getFixedStringLength(buffer, Offsets.RequestedSession, Lengths.RequestedSession);
     }

     @Override
     public String getRequestedSessionAsString() {
	     return com.core.util.MessageUtils.toLeftPaddedFixedString(buffer, Offsets.RequestedSession, Lengths.RequestedSession);
     }

    @Override
    public void setRequestedSession(java.nio.ByteBuffer value) {
    	com.core.util.MessageUtils.setLeftPaddedFixedString(buffer, Offsets.RequestedSession, value, Lengths.RequestedSession);  						
    }

    @Override
    public void setRequestedSession(String value) {
    	com.core.util.MessageUtils.setLeftPaddedFixedString(buffer, Offsets.RequestedSession, value, Lengths.RequestedSession);  						
    }

    @Override
    public boolean hasRequestedSession() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.RequestedSession, Lengths.RequestedSession);  
    }

     @Override
     public java.nio.ByteBuffer getRequestedSequenceNumber() {
	     return com.core.util.MessageUtils.getLeftPaddedFixedString(buffer, Offsets.RequestedSequenceNumber, Lengths.RequestedSequenceNumber);
     }

     @Override
     public int getRequestedSequenceNumberLength() {
	     return com.core.util.MessageUtils.getFixedStringLength(buffer, Offsets.RequestedSequenceNumber, Lengths.RequestedSequenceNumber);
     }

     @Override
     public String getRequestedSequenceNumberAsString() {
	     return com.core.util.MessageUtils.toLeftPaddedFixedString(buffer, Offsets.RequestedSequenceNumber, Lengths.RequestedSequenceNumber);
     }

    @Override
    public void setRequestedSequenceNumber(java.nio.ByteBuffer value) {
    	com.core.util.MessageUtils.setLeftPaddedFixedString(buffer, Offsets.RequestedSequenceNumber, value, Lengths.RequestedSequenceNumber);  						
    }

    @Override
    public void setRequestedSequenceNumber(String value) {
    	com.core.util.MessageUtils.setLeftPaddedFixedString(buffer, Offsets.RequestedSequenceNumber, value, Lengths.RequestedSequenceNumber);  						
    }

    @Override
    public boolean hasRequestedSequenceNumber() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.RequestedSequenceNumber, Lengths.RequestedSequenceNumber);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=LoginRequest");
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
        builder.append(",Username=");
        if (hasUsername()) {
            builder.append(getUsernameAsString());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Password=");
        if (hasPassword()) {
            builder.append(getPasswordAsString());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",RequestedSession=");
        if (hasRequestedSession()) {
            builder.append(getRequestedSessionAsString());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",RequestedSequenceNumber=");
        if (hasRequestedSequenceNumber()) {
            builder.append(getRequestedSequenceNumberAsString());
        }
        else {
            builder.append("<NULL>");
        }
        return builder.toString();        
    }

    private static class Offsets {
        static int MsgLength = 0;
        static int MsgType = 2;
        static int Username = 3;
        static int Password = 9;
        static int RequestedSession = 19;
        static int RequestedSequenceNumber = 29;
    }
	
    private static class Lengths {
        static int MsgLength = 2;
        static int MsgType = 1;
        static int Username = 6;
        static int Password = 10;
        static int RequestedSession = 10;
        static int RequestedSequenceNumber = 20;
    }
} 
