package com.core.match.msgs;

import java.nio.ByteBuffer;

public class MatchQuoteByteBufferMessage implements 
	MatchQuoteEvent, 
	MatchQuoteCommand {
    private static final int DEFAULT_LENGTH = 42;
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
    public void copy(MatchQuoteEvent cmd) {
        setMsgType(cmd.getMsgType());
        setContributorID(cmd.getContributorID());
        setContributorSeq(cmd.getContributorSeq());
        setTimestamp(cmd.getTimestamp());
        setSecurityID(cmd.getSecurityID());
        setBidPrice(cmd.getBidPrice());
        setOfferPrice(cmd.getOfferPrice());
        setVenueCode(cmd.getVenueCode());
        setSourceTimestamp(cmd.getSourceTimestamp());
    }

    @Override
    public MatchQuoteEvent toEvent() {
        return this;
    }

    @Override
    public MatchQuoteCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "Quote";
    }
 
    public MatchQuoteCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('D');
        stringsBlockLength = 0;
        return this;
    }

    public MatchQuoteEvent wrapEvent(ByteBuffer buf) {
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
     public long getBidPrice() {
         return com.core.util.MessageUtils.getLong(buffer, Offsets.BidPrice);  	 
     }

     @Override
     public String getBidPriceAs32nd() {
         return com.core.match.util.MessageUtils.to32ndPrice(buffer, Offsets.BidPrice);  	 
     }

     @Override
     public double getBidPriceAsDouble() {
         return com.core.match.util.MessageUtils.getDoublePrice(buffer, Offsets.BidPrice);  	 
     }

    @Override
    public void setBidPrice(long value) {
    	com.core.util.MessageUtils.setLong(buffer, Offsets.BidPrice, value);  									
    }

    @Override
    public void setBidPrice(double value) {
    	com.core.match.util.MessageUtils.setDoublePrice(buffer, Offsets.BidPrice, value);  									
    }

    @Override
    public boolean hasBidPrice() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.BidPrice, Lengths.BidPrice);  
    }

     @Override
     public long getOfferPrice() {
         return com.core.util.MessageUtils.getLong(buffer, Offsets.OfferPrice);  	 
     }

     @Override
     public String getOfferPriceAs32nd() {
         return com.core.match.util.MessageUtils.to32ndPrice(buffer, Offsets.OfferPrice);  	 
     }

     @Override
     public double getOfferPriceAsDouble() {
         return com.core.match.util.MessageUtils.getDoublePrice(buffer, Offsets.OfferPrice);  	 
     }

    @Override
    public void setOfferPrice(long value) {
    	com.core.util.MessageUtils.setLong(buffer, Offsets.OfferPrice, value);  									
    }

    @Override
    public void setOfferPrice(double value) {
    	com.core.match.util.MessageUtils.setDoublePrice(buffer, Offsets.OfferPrice, value);  									
    }

    @Override
    public boolean hasOfferPrice() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.OfferPrice, Lengths.OfferPrice);  
    }

     @Override
     public char getVenueCode() {
         return com.core.util.MessageUtils.getChar(buffer, Offsets.VenueCode);  	 
     }

    @Override
    public void setVenueCode(char value) {
    	com.core.util.MessageUtils.setChar(buffer, Offsets.VenueCode, value);  									
    }

    @Override
    public boolean hasVenueCode() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.VenueCode, Lengths.VenueCode);  
    }

     @Override
     public long getSourceTimestamp() {
         return com.core.util.MessageUtils.getLong(buffer, Offsets.SourceTimestamp);  	 
     }

     @Override
     public java.time.LocalDateTime getSourceTimestampAsTime() {
         return com.core.util.MessageUtils.getDateTime(buffer, Offsets.SourceTimestamp);  	 
     }

    @Override
    public void setSourceTimestamp(long value) {
    	com.core.util.MessageUtils.setLong(buffer, Offsets.SourceTimestamp, value);  									
    }

    @Override
    public boolean hasSourceTimestamp() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.SourceTimestamp, Lengths.SourceTimestamp);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Quote");
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
        builder.append(",SecurityID=");
        if (hasSecurityID()) {
            builder.append(getSecurityID());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",BidPrice=");
        if (hasBidPrice()) {
            builder.append(getBidPriceAs32nd());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",OfferPrice=");
        if (hasOfferPrice()) {
            builder.append(getOfferPriceAs32nd());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",VenueCode=");
        if (hasVenueCode()) {
            builder.append(MatchConstants.Venue.toString(getVenueCode()));
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",SourceTimestamp=");
        if (hasSourceTimestamp()) {
            builder.append(getSourceTimestampAsTime());
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
        static int SecurityID = 15;
        static int BidPrice = 17;
        static int OfferPrice = 25;
        static int VenueCode = 33;
        static int SourceTimestamp = 34;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int ContributorID = 2;
        static int ContributorSeq = 4;
        static int Timestamp = 8;
        static int SecurityID = 2;
        static int BidPrice = 8;
        static int OfferPrice = 8;
        static int VenueCode = 1;
        static int SourceTimestamp = 8;
    }
} 
