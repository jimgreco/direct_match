package com.core.match.msgs;

import java.nio.ByteBuffer;

public class MatchMiscRejectByteBufferMessage implements 
	MatchMiscRejectEvent, 
	MatchMiscRejectCommand {
    private static final int DEFAULT_LENGTH = 17;
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
    public void copy(MatchMiscRejectEvent cmd) {
        setMsgType(cmd.getMsgType());
        setContributorID(cmd.getContributorID());
        setContributorSeq(cmd.getContributorSeq());
        setTimestamp(cmd.getTimestamp());
        setRejectedMsgType(cmd.getRejectedMsgType());
        setRejectReason(cmd.getRejectReason());
    }

    @Override
    public MatchMiscRejectEvent toEvent() {
        return this;
    }

    @Override
    public MatchMiscRejectCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "MiscReject";
    }
 
    public MatchMiscRejectCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('M');
        stringsBlockLength = 0;
        return this;
    }

    public MatchMiscRejectEvent wrapEvent(ByteBuffer buf) {
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
     public char getRejectedMsgType() {
         return com.core.util.MessageUtils.getChar(buffer, Offsets.RejectedMsgType);  	 
     }

    @Override
    public void setRejectedMsgType(char value) {
    	com.core.util.MessageUtils.setChar(buffer, Offsets.RejectedMsgType, value);  									
    }

    @Override
    public boolean hasRejectedMsgType() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.RejectedMsgType, Lengths.RejectedMsgType);  
    }

     @Override
     public char getRejectReason() {
         return com.core.util.MessageUtils.getChar(buffer, Offsets.RejectReason);  	 
     }

    @Override
    public void setRejectReason(char value) {
    	com.core.util.MessageUtils.setChar(buffer, Offsets.RejectReason, value);  									
    }

    @Override
    public boolean hasRejectReason() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.RejectReason, Lengths.RejectReason);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=MiscReject");
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
        builder.append(",RejectedMsgType=");
        if (hasRejectedMsgType()) {
            builder.append(getRejectedMsgType());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",RejectReason=");
        if (hasRejectReason()) {
            builder.append(MatchConstants.MiscRejectReason.toString(getRejectReason()));
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
        static int RejectedMsgType = 15;
        static int RejectReason = 16;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int ContributorID = 2;
        static int ContributorSeq = 4;
        static int Timestamp = 8;
        static int RejectedMsgType = 1;
        static int RejectReason = 1;
    }
} 
