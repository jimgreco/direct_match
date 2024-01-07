package com.core.match.msgs;

import java.nio.ByteBuffer;

public class MatchCancelReplaceRejectByteBufferMessage implements 
	MatchCancelReplaceRejectEvent, 
	MatchCancelReplaceRejectCommand {
    private static final int DEFAULT_LENGTH = 27;
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
    public void copy(MatchCancelReplaceRejectEvent cmd) {
        setMsgType(cmd.getMsgType());
        setContributorID(cmd.getContributorID());
        setContributorSeq(cmd.getContributorSeq());
        setTimestamp(cmd.getTimestamp());
        setOrderID(cmd.getOrderID());
        setClOrdID(cmd.getClOrdID());
        setOrigClOrdID(cmd.getOrigClOrdID());
        setText(cmd.getText());
        setIsReplace(cmd.getIsReplace());
        setReason(cmd.getReason());
    }

    @Override
    public MatchCancelReplaceRejectEvent toEvent() {
        return this;
    }

    @Override
    public MatchCancelReplaceRejectCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "CancelReplaceReject";
    }
 
    public MatchCancelReplaceRejectCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('Z');
        stringsBlockLength = 0;
        return this;
    }

    public MatchCancelReplaceRejectEvent wrapEvent(ByteBuffer buf) {
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
     public int getOrderID() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.OrderID);  	 
     }

    @Override
    public void setOrderID(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.OrderID, value);  									
    }

    @Override
    public boolean hasOrderID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.OrderID, Lengths.OrderID);  
    }

     @Override
     public java.nio.ByteBuffer getClOrdID() {
		 return com.core.util.MessageUtils.getVariableString(buffer, Offsets.ClOrdID);
     }

     @Override
     public int getClOrdIDLength() {
		 return com.core.util.MessageUtils.getStringLength(buffer, Offsets.ClOrdID);
     }

     @Override
     public String getClOrdIDAsString() {
		 return com.core.util.MessageUtils.toVariableString(buffer, Offsets.ClOrdID);
     }

    @Override
    public void setClOrdID(java.nio.ByteBuffer value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.ClOrdID, value, getLength());  			
    }

    @Override
    public void setClOrdID(String value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.ClOrdID, value, getLength());  			
    }

    @Override
    public boolean hasClOrdID() {
		return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.ClOrdID, Lengths.ClOrdID) && getClOrdIDLength() > 0;  
    }

     @Override
     public java.nio.ByteBuffer getOrigClOrdID() {
		 return com.core.util.MessageUtils.getVariableString(buffer, Offsets.OrigClOrdID);
     }

     @Override
     public int getOrigClOrdIDLength() {
		 return com.core.util.MessageUtils.getStringLength(buffer, Offsets.OrigClOrdID);
     }

     @Override
     public String getOrigClOrdIDAsString() {
		 return com.core.util.MessageUtils.toVariableString(buffer, Offsets.OrigClOrdID);
     }

    @Override
    public void setOrigClOrdID(java.nio.ByteBuffer value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.OrigClOrdID, value, getLength());  			
    }

    @Override
    public void setOrigClOrdID(String value) {
    	stringsBlockLength += com.core.util.MessageUtils.setVariableString(buffer, Offsets.OrigClOrdID, value, getLength());  			
    }

    @Override
    public boolean hasOrigClOrdID() {
		return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.OrigClOrdID, Lengths.OrigClOrdID) && getOrigClOrdIDLength() > 0;  
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
     public boolean getIsReplace() {
         return com.core.util.MessageUtils.getBool(buffer, Offsets.IsReplace);  	 
     }

    @Override
    public void setIsReplace(boolean value) {
    	com.core.util.MessageUtils.setBool(buffer, Offsets.IsReplace, value);  									
    }

    @Override
    public boolean hasIsReplace() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.IsReplace, Lengths.IsReplace);  
    }

     @Override
     public char getReason() {
         return com.core.util.MessageUtils.getChar(buffer, Offsets.Reason);  	 
     }

    @Override
    public void setReason(char value) {
    	com.core.util.MessageUtils.setChar(buffer, Offsets.Reason, value);  									
    }

    @Override
    public boolean hasReason() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Reason, Lengths.Reason);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=CancelReplaceReject");
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
        builder.append(",OrderID=");
        if (hasOrderID()) {
            builder.append(getOrderID());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",ClOrdID=");
        if (hasClOrdID()) {
            builder.append(getClOrdIDAsString());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",OrigClOrdID=");
        if (hasOrigClOrdID()) {
            builder.append(getOrigClOrdIDAsString());
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
        builder.append(",IsReplace=");
        if (hasIsReplace()) {
            builder.append(getIsReplace());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Reason=");
        if (hasReason()) {
            builder.append(MatchConstants.OrderRejectReason.toString(getReason()));
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
        static int OrderID = 15;
        static int ClOrdID = 19;
        static int OrigClOrdID = 21;
        static int Text = 23;
        static int IsReplace = 25;
        static int Reason = 26;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int ContributorID = 2;
        static int ContributorSeq = 4;
        static int Timestamp = 8;
        static int OrderID = 4;
        static int ClOrdID = 2;
        static int OrigClOrdID = 2;
        static int Text = 2;
        static int IsReplace = 1;
        static int Reason = 1;
    }
} 
