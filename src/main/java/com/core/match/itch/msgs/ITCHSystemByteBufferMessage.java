package com.core.match.itch.msgs;

import java.nio.ByteBuffer;

public class ITCHSystemByteBufferMessage implements 
	ITCHSystemEvent, 
	ITCHSystemCommand {
    private static final int DEFAULT_LENGTH = 12;
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
    public void copy(ITCHSystemEvent cmd) {
        setMsgType(cmd.getMsgType());
        setSecurityID(cmd.getSecurityID());
        setTimestamp(cmd.getTimestamp());
        setEventCode(cmd.getEventCode());
    }

    @Override
    public ITCHSystemEvent toEvent() {
        return this;
    }

    @Override
    public ITCHSystemCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "System";
    }
 
    public ITCHSystemCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('S');
        stringsBlockLength = 0;
        return this;
    }

    public ITCHSystemEvent wrapEvent(ByteBuffer buf) {
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
     public short getSecurityID() {
         return com.core.util.MessageUtils.getShort(buffer, Offsets.SecurityID);  	 
     }

    @Override
    public void setSecurityID(short value) {
    	com.core.util.MessageUtils.setShort(buffer, Offsets.SecurityID, value);  									
    }

    @Override
    public boolean hasSecurityID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.SecurityID, Lengths.SecurityID);  
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
     public char getEventCode() {
         return com.core.util.MessageUtils.getChar(buffer, Offsets.EventCode);  	 
     }

    @Override
    public void setEventCode(char value) {
    	com.core.util.MessageUtils.setChar(buffer, Offsets.EventCode, value);  									
    }

    @Override
    public boolean hasEventCode() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.EventCode, Lengths.EventCode);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=System");
        builder.append(",MsgType=");
        if (hasMsgType()) {
            builder.append(getMsgType());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",SecurityID=");
        if (hasSecurityID()) {
            builder.append(getSecurityID());
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
        builder.append(",EventCode=");
        if (hasEventCode()) {
            builder.append(getEventCode());
        }
        else {
            builder.append("<NULL>");
        }
        return builder.toString();        
    }

    private static class Offsets {
        static int MsgType = 0;
        static int SecurityID = 1;
        static int Timestamp = 3;
        static int EventCode = 11;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int SecurityID = 2;
        static int Timestamp = 8;
        static int EventCode = 1;
    }
} 
