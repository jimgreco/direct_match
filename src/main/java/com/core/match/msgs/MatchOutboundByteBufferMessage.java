package com.core.match.msgs;

import java.nio.ByteBuffer;

public class MatchOutboundByteBufferMessage implements 
	MatchOutboundEvent, 
	MatchOutboundCommand {
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
    public void copy(MatchOutboundEvent cmd) {
        setMsgType(cmd.getMsgType());
        setContributorID(cmd.getContributorID());
        setContributorSeq(cmd.getContributorSeq());
        setTimestamp(cmd.getTimestamp());
        setFixMsgType(cmd.getFixMsgType());
        setReqID(cmd.getReqID());
        setText(cmd.getText());
        setRefMsgType(cmd.getRefMsgType());
        setRefSeqNum(cmd.getRefSeqNum());
        setRefTagID(cmd.getRefTagID());
        setSessionRejectReason(cmd.getSessionRejectReason());
    }

    @Override
    public MatchOutboundEvent toEvent() {
        return this;
    }

    @Override
    public MatchOutboundCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "Outbound";
    }
 
    public MatchOutboundCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('J');
        stringsBlockLength = 0;
        return this;
    }

    public MatchOutboundEvent wrapEvent(ByteBuffer buf) {
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
     public java.nio.ByteBuffer getText() {
		 return com.core.util.MessageUtils.getVariableString(buffer, Offsets.Text);
     }

     @Override
     public int getTextLength() {
		 return com.core.util.MessageUtils.getStringLength(buffer, Offsets.Text);
     }

     @Override
     public String getTextAsString() {
		 return com.core.util.MessageUtils.toVariableString(buffer, Offsets.Text);
     }

    @Override
    public void setText(java.nio.ByteBuffer value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.Text, value, getLength());  			
    }

    @Override
    public void setText(String value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.Text, value, getLength());  			
    }

    @Override
    public boolean hasText() {
		return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Text, Lengths.Text) && getTextLength() > 0;  
    }

     @Override
     public char getRefMsgType() {
         return com.core.util.MessageUtils.getChar(buffer, Offsets.RefMsgType);  	 
     }

    @Override
    public void setRefMsgType(char value) {
    	com.core.util.MessageUtils.setChar(buffer, Offsets.RefMsgType, value);  									
    }

    @Override
    public boolean hasRefMsgType() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.RefMsgType, Lengths.RefMsgType);  
    }

     @Override
     public int getRefSeqNum() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.RefSeqNum);  	 
     }

    @Override
    public void setRefSeqNum(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.RefSeqNum, value);  									
    }

    @Override
    public boolean hasRefSeqNum() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.RefSeqNum, Lengths.RefSeqNum);  
    }

     @Override
     public short getRefTagID() {
         return com.core.util.MessageUtils.getShort(buffer, Offsets.RefTagID);  	 
     }

    @Override
    public void setRefTagID(short value) {
    	com.core.util.MessageUtils.setShort(buffer, Offsets.RefTagID, value);  									
    }

    @Override
    public boolean hasRefTagID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.RefTagID, Lengths.RefTagID);  
    }

     @Override
     public char getSessionRejectReason() {
         return com.core.util.MessageUtils.getChar(buffer, Offsets.SessionRejectReason);  	 
     }

    @Override
    public void setSessionRejectReason(char value) {
    	com.core.util.MessageUtils.setChar(buffer, Offsets.SessionRejectReason, value);  									
    }

    @Override
    public boolean hasSessionRejectReason() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.SessionRejectReason, Lengths.SessionRejectReason);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Outbound");
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
        builder.append(",ReqID=");
        if (hasReqID()) {
            builder.append(getReqIDAsString());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Text=");
        if (hasText()) {
            builder.append(getTextAsString());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",RefMsgType=");
        if (hasRefMsgType()) {
            builder.append(getRefMsgType());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",RefSeqNum=");
        if (hasRefSeqNum()) {
            builder.append(getRefSeqNum());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",RefTagID=");
        if (hasRefTagID()) {
            builder.append(getRefTagID());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",SessionRejectReason=");
        if (hasSessionRejectReason()) {
            builder.append(getSessionRejectReason());
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
        static int ReqID = 16;
        static int Text = 18;
        static int RefMsgType = 20;
        static int RefSeqNum = 21;
        static int RefTagID = 25;
        static int SessionRejectReason = 27;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int ContributorID = 2;
        static int ContributorSeq = 4;
        static int Timestamp = 8;
        static int FixMsgType = 1;
        static int ReqID = 2;
        static int Text = 2;
        static int RefMsgType = 1;
        static int RefSeqNum = 4;
        static int RefTagID = 2;
        static int SessionRejectReason = 1;
    }
} 
