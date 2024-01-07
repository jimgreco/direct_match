package com.core.match.msgs;

import java.nio.ByteBuffer;

public class MatchInboundByteBufferMessage implements 
	MatchInboundEvent, 
	MatchInboundCommand {
    private static final int DEFAULT_LENGTH = 28;
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
    public void copy(MatchInboundEvent cmd) {
        setMsgType(cmd.getMsgType());
        setContributorID(cmd.getContributorID());
        setContributorSeq(cmd.getContributorSeq());
        setTimestamp(cmd.getTimestamp());
        setFixMsgType(cmd.getFixMsgType());
        setBeginSeqNo(cmd.getBeginSeqNo());
        setEndSeqNo(cmd.getEndSeqNo());
        setReqID(cmd.getReqID());
        setSecurityID(cmd.getSecurityID());
    }

    @Override
    public MatchInboundEvent toEvent() {
        return this;
    }

    @Override
    public MatchInboundCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "Inbound";
    }
 
    public MatchInboundCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('I');
        stringsBlockLength = 0;
        return this;
    }

    public MatchInboundEvent wrapEvent(ByteBuffer buf) {
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
     public char getFixMsgType() {
         return com.core.util.MessageUtils.getChar(buffer, Offsets.FixMsgType);  	 
     }

    @Override
    public void setFixMsgType(char value) {
    	com.core.util.MessageUtils.setChar(buffer, Offsets.FixMsgType, value);  									
    }

    @Override
    public boolean hasFixMsgType() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.FixMsgType, Lengths.FixMsgType);  
    }

     @Override
     public int getBeginSeqNo() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.BeginSeqNo);  	 
     }

    @Override
    public void setBeginSeqNo(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.BeginSeqNo, value);  									
    }

    @Override
    public boolean hasBeginSeqNo() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.BeginSeqNo, Lengths.BeginSeqNo);  
    }

     @Override
     public int getEndSeqNo() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.EndSeqNo);  	 
     }

    @Override
    public void setEndSeqNo(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.EndSeqNo, value);  									
    }

    @Override
    public boolean hasEndSeqNo() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.EndSeqNo, Lengths.EndSeqNo);  
    }

     @Override
     public java.nio.ByteBuffer getReqID() {
		 return com.core.util.MessageUtils.getVariableString(buffer, Offsets.ReqID);
     }

     @Override
     public int getReqIDLength() {
		 return com.core.util.MessageUtils.getStringLength(buffer, Offsets.ReqID);
     }

     @Override
     public String getReqIDAsString() {
		 return com.core.util.MessageUtils.toVariableString(buffer, Offsets.ReqID);
     }

    @Override
    public void setReqID(java.nio.ByteBuffer value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.ReqID, value, getLength());  			
    }

    @Override
    public void setReqID(String value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.ReqID, value, getLength());  			
    }

    @Override
    public boolean hasReqID() {
		return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.ReqID, Lengths.ReqID) && getReqIDLength() > 0;  
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Inbound");
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
        builder.append(",FixMsgType=");
        if (hasFixMsgType()) {
            builder.append(getFixMsgType());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",BeginSeqNo=");
        if (hasBeginSeqNo()) {
            builder.append(getBeginSeqNo());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",EndSeqNo=");
        if (hasEndSeqNo()) {
            builder.append(getEndSeqNo());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",ReqID=");
        if (hasReqID()) {
            builder.append(getReqIDAsString());
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
        return builder.toString();        
    }

    private static class Offsets {
        static int MsgType = 0;
        static int ContributorID = 1;
        static int ContributorSeq = 3;
        static int Timestamp = 7;
        static int FixMsgType = 15;
        static int BeginSeqNo = 16;
        static int EndSeqNo = 20;
        static int ReqID = 24;
        static int SecurityID = 26;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int ContributorID = 2;
        static int ContributorSeq = 4;
        static int Timestamp = 8;
        static int FixMsgType = 1;
        static int BeginSeqNo = 4;
        static int EndSeqNo = 4;
        static int ReqID = 2;
        static int SecurityID = 2;
    }
} 
