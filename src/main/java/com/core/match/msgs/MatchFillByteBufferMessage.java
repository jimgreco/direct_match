package com.core.match.msgs;

import java.nio.ByteBuffer;

public class MatchFillByteBufferMessage implements 
	MatchFillEvent, 
	MatchFillCommand {
    private static final int DEFAULT_LENGTH = 38;
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
    public void copy(MatchFillEvent cmd) {
        setMsgType(cmd.getMsgType());
        setContributorID(cmd.getContributorID());
        setContributorSeq(cmd.getContributorSeq());
        setTimestamp(cmd.getTimestamp());
        setOrderID(cmd.getOrderID());
        setQty(cmd.getQty());
        setPrice(cmd.getPrice());
        setMatchID(cmd.getMatchID());
        setLastFill(cmd.getLastFill());
        setPassive(cmd.getPassive());
        setInBook(cmd.getInBook());
    }

    @Override
    public MatchFillEvent toEvent() {
        return this;
    }

    @Override
    public MatchFillCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "Fill";
    }
 
    public MatchFillCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('F');
        stringsBlockLength = 0;
        return this;
    }

    public MatchFillEvent wrapEvent(ByteBuffer buf) {
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
     public int getQty() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.Qty);  	 
     }

     @Override
     public double getQtyAsQty() {
         return com.core.match.util.MessageUtils.toQtyRoundLot(buffer, Offsets.Qty);  	 
     }

    @Override
    public void setQty(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.Qty, value);  									
    }

    @Override
    public boolean hasQty() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Qty, Lengths.Qty);  
    }

     @Override
     public long getPrice() {
         return com.core.util.MessageUtils.getLong(buffer, Offsets.Price);  	 
     }

     @Override
     public String getPriceAs32nd() {
         return com.core.match.util.MessageUtils.to32ndPrice(buffer, Offsets.Price);  	 
     }

     @Override
     public double getPriceAsDouble() {
         return com.core.match.util.MessageUtils.getDoublePrice(buffer, Offsets.Price);  	 
     }

    @Override
    public void setPrice(long value) {
    	com.core.util.MessageUtils.setLong(buffer, Offsets.Price, value);  									
    }

    @Override
    public void setPrice(double value) {
    	com.core.match.util.MessageUtils.setDoublePrice(buffer, Offsets.Price, value);  									
    }

    @Override
    public boolean hasPrice() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Price, Lengths.Price);  
    }

     @Override
     public int getMatchID() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.MatchID);  	 
     }

    @Override
    public void setMatchID(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.MatchID, value);  									
    }

    @Override
    public boolean hasMatchID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.MatchID, Lengths.MatchID);  
    }

     @Override
     public boolean getLastFill() {
         return com.core.util.MessageUtils.getBool(buffer, Offsets.LastFill);  	 
     }

    @Override
    public void setLastFill(boolean value) {
    	com.core.util.MessageUtils.setBool(buffer, Offsets.LastFill, value);  									
    }

    @Override
    public boolean hasLastFill() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.LastFill, Lengths.LastFill);  
    }

     @Override
     public boolean getPassive() {
         return com.core.util.MessageUtils.getBool(buffer, Offsets.Passive);  	 
     }

    @Override
    public void setPassive(boolean value) {
    	com.core.util.MessageUtils.setBool(buffer, Offsets.Passive, value);  									
    }

    @Override
    public boolean hasPassive() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Passive, Lengths.Passive);  
    }

     @Override
     public boolean getInBook() {
         return com.core.util.MessageUtils.getBool(buffer, Offsets.InBook);  	 
     }

    @Override
    public void setInBook(boolean value) {
    	com.core.util.MessageUtils.setBool(buffer, Offsets.InBook, value);  									
    }

    @Override
    public boolean hasInBook() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.InBook, Lengths.InBook);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Fill");
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
        builder.append(",Qty=");
        if (hasQty()) {
            builder.append(getQtyAsQty());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Price=");
        if (hasPrice()) {
            builder.append(getPriceAs32nd());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",MatchID=");
        if (hasMatchID()) {
            builder.append(getMatchID());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",LastFill=");
        if (hasLastFill()) {
            builder.append(getLastFill());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Passive=");
        if (hasPassive()) {
            builder.append(getPassive());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",InBook=");
        if (hasInBook()) {
            builder.append(getInBook());
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
        static int Qty = 19;
        static int Price = 23;
        static int MatchID = 31;
        static int LastFill = 35;
        static int Passive = 36;
        static int InBook = 37;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int ContributorID = 2;
        static int ContributorSeq = 4;
        static int Timestamp = 8;
        static int OrderID = 4;
        static int Qty = 4;
        static int Price = 8;
        static int MatchID = 4;
        static int LastFill = 1;
        static int Passive = 1;
        static int InBook = 1;
    }
} 
