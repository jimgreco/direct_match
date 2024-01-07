package com.core.match.msgs;

import java.nio.ByteBuffer;

public class MatchContributorByteBufferMessage implements 
	MatchContributorEvent, 
	MatchContributorCommand {
    private static final int DEFAULT_LENGTH = 20;
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
    public void copy(MatchContributorEvent cmd) {
        setMsgType(cmd.getMsgType());
        setContributorID(cmd.getContributorID());
        setContributorSeq(cmd.getContributorSeq());
        setTimestamp(cmd.getTimestamp());
        setSourceContributorID(cmd.getSourceContributorID());
        setName(cmd.getName());
        setCancelOnDisconnect(cmd.getCancelOnDisconnect());
    }

    @Override
    public MatchContributorEvent toEvent() {
        return this;
    }

    @Override
    public MatchContributorCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "Contributor";
    }
 
    public MatchContributorCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('C');
        stringsBlockLength = 0;
        return this;
    }

    public MatchContributorEvent wrapEvent(ByteBuffer buf) {
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
     public short getContributorID() {
         return com.core.util.MessageUtils.getShort(buffer, Offsets.ContributorID);  	 
     }

    @Override
    public void setContributorID(short value) {
    	com.core.util.MessageUtils.setShort(buffer, Offsets.ContributorID, value);  									
    }

    @Override
    public boolean hasContributorID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.ContributorID, Lengths.ContributorID);  
    }

     @Override
     public int getContributorSeq() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.ContributorSeq);  	 
     }

    @Override
    public void setContributorSeq(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.ContributorSeq, value);  									
    }

    @Override
    public boolean hasContributorSeq() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.ContributorSeq, Lengths.ContributorSeq);  
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
     public short getSourceContributorID() {
         return com.core.util.MessageUtils.getShort(buffer, Offsets.SourceContributorID);  	 
     }

    @Override
    public void setSourceContributorID(short value) {
    	com.core.util.MessageUtils.setShort(buffer, Offsets.SourceContributorID, value);  									
    }

    @Override
    public boolean hasSourceContributorID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.SourceContributorID, Lengths.SourceContributorID);  
    }

     @Override
     public java.nio.ByteBuffer getName() {
		 return com.core.util.MessageUtils.getVariableString(buffer, Offsets.Name);
     }

     @Override
     public int getNameLength() {
		 return com.core.util.MessageUtils.getStringLength(buffer, Offsets.Name);
     }

     @Override
     public String getNameAsString() {
		 return com.core.util.MessageUtils.toVariableString(buffer, Offsets.Name);
     }

    @Override
    public void setName(java.nio.ByteBuffer value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.Name, value, getLength());  			
    }

    @Override
    public void setName(String value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.Name, value, getLength());  			
    }

    @Override
    public boolean hasName() {
		return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Name, Lengths.Name) && getNameLength() > 0;  
    }

     @Override
     public boolean getCancelOnDisconnect() {
         return com.core.util.MessageUtils.getBool(buffer, Offsets.CancelOnDisconnect);  	 
     }

    @Override
    public void setCancelOnDisconnect(boolean value) {
    	com.core.util.MessageUtils.setBool(buffer, Offsets.CancelOnDisconnect, value);  									
    }

    @Override
    public boolean hasCancelOnDisconnect() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.CancelOnDisconnect, Lengths.CancelOnDisconnect);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Contributor");
        builder.append(",MsgType=");
        if (hasMsgType()) {
            builder.append(getMsgType());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",ContributorID=");
        if (hasContributorID()) {
            builder.append(getContributorID());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",ContributorSeq=");
        if (hasContributorSeq()) {
            builder.append(getContributorSeq());
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
        builder.append(",SourceContributorID=");
        if (hasSourceContributorID()) {
            builder.append(getSourceContributorID());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Name=");
        if (hasName()) {
            builder.append(getNameAsString());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",CancelOnDisconnect=");
        if (hasCancelOnDisconnect()) {
            builder.append(getCancelOnDisconnect());
        }
        else {
            builder.append("<NULL>");
        }
        return builder.toString();        
    }

    private static class Offsets {
        static int MsgType = 0;
        static int ContributorID = 1;
        static int ContributorSeq = 3;
        static int Timestamp = 7;
        static int SourceContributorID = 15;
        static int Name = 17;
        static int CancelOnDisconnect = 19;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int ContributorID = 2;
        static int ContributorSeq = 4;
        static int Timestamp = 8;
        static int SourceContributorID = 2;
        static int Name = 2;
        static int CancelOnDisconnect = 1;
    }
} 
