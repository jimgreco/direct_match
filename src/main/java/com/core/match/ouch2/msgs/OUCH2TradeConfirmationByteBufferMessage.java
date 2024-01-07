package com.core.match.ouch2.msgs;

import java.nio.ByteBuffer;

public class OUCH2TradeConfirmationByteBufferMessage implements 
	OUCH2TradeConfirmationEvent, 
	OUCH2TradeConfirmationCommand {
    private static final int DEFAULT_LENGTH = 82;
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
    public void copy(OUCH2TradeConfirmationEvent cmd) {
        setMsgType(cmd.getMsgType());
        setTimestamp(cmd.getTimestamp());
        setClOrdID(cmd.getClOrdID());
        setExecQty(cmd.getExecQty());
        setExecPrice(cmd.getExecPrice());
        setMatchID(cmd.getMatchID());
        setSide(cmd.getSide());
        setSecurity(cmd.getSecurity());
        setTradeDate(cmd.getTradeDate());
        setTradeTime(cmd.getTradeTime());
        setSettlementDate(cmd.getSettlementDate());
        setCommissionAmount(cmd.getCommissionAmount());
        setTrader(cmd.getTrader());
    }

    @Override
    public OUCH2TradeConfirmationEvent toEvent() {
        return this;
    }

    @Override
    public OUCH2TradeConfirmationCommand toCommand() {
        return this;
    }

    @Override
    public String getMsgName() {
        return "TradeConfirmation";
    }
 
    public OUCH2TradeConfirmationCommand wrapCommand(ByteBuffer buf) {
        buffer = buf;
        buffer.mark();
        buffer.put(EMPTY);
        buffer.reset();
        setMsgType('T');
        stringsBlockLength = 0;
        return this;
    }

    public OUCH2TradeConfirmationEvent wrapEvent(ByteBuffer buf) {
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
     public long getClOrdID() {
         return com.core.util.MessageUtils.getLong(buffer, Offsets.ClOrdID);  	 
     }

    @Override
    public void setClOrdID(long value) {
    	com.core.util.MessageUtils.setLong(buffer, Offsets.ClOrdID, value);  									
    }

    @Override
    public boolean hasClOrdID() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.ClOrdID, Lengths.ClOrdID);  
    }

     @Override
     public int getExecQty() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.ExecQty);  	 
     }

     @Override
     public double getExecQtyAsQty() {
         return com.core.match.util.MessageUtils.toExternalQtyRoundLot(buffer, Offsets.ExecQty);  	 
     }

    @Override
    public void setExecQty(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.ExecQty, value);  									
    }

    @Override
    public boolean hasExecQty() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.ExecQty, Lengths.ExecQty);  
    }

     @Override
     public long getExecPrice() {
         return com.core.util.MessageUtils.getLong(buffer, Offsets.ExecPrice);  	 
     }

     @Override
     public String getExecPriceAs32nd() {
         return com.core.match.util.MessageUtils.to32ndPrice(buffer, Offsets.ExecPrice);  	 
     }

     @Override
     public double getExecPriceAsDouble() {
         return com.core.match.util.MessageUtils.getDoublePrice(buffer, Offsets.ExecPrice);  	 
     }

    @Override
    public void setExecPrice(long value) {
    	com.core.util.MessageUtils.setLong(buffer, Offsets.ExecPrice, value);  									
    }

    @Override
    public void setExecPrice(double value) {
    	com.core.match.util.MessageUtils.setDoublePrice(buffer, Offsets.ExecPrice, value);  									
    }

    @Override
    public boolean hasExecPrice() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.ExecPrice, Lengths.ExecPrice);  
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
     public char getSide() {
         return com.core.util.MessageUtils.getChar(buffer, Offsets.Side);  	 
     }

    @Override
    public void setSide(char value) {
    	com.core.util.MessageUtils.setChar(buffer, Offsets.Side, value);  									
    }

    @Override
    public boolean hasSide() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Side, Lengths.Side);  
    }

     @Override
     public java.nio.ByteBuffer getSecurity() {
	     return com.core.util.MessageUtils.getRightPaddedFixedString(buffer, Offsets.Security, Lengths.Security);
     }

     @Override
     public int getSecurityLength() {
	     return com.core.util.MessageUtils.getFixedStringLength(buffer, Offsets.Security, Lengths.Security);
     }

     @Override
     public String getSecurityAsString() {
	     return com.core.util.MessageUtils.toRightPaddedFixedString(buffer, Offsets.Security, Lengths.Security);
     }

    @Override
    public void setSecurity(java.nio.ByteBuffer value) {
    	com.core.util.MessageUtils.setRightPaddedFixedString(buffer, Offsets.Security, value, Lengths.Security);  						
    }

    @Override
    public void setSecurity(String value) {
    	com.core.util.MessageUtils.setRightPaddedFixedString(buffer, Offsets.Security, value, Lengths.Security);  						
    }

    @Override
    public boolean hasSecurity() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Security, Lengths.Security);  
    }

     @Override
     public int getTradeDate() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.TradeDate);  	 
     }

     @Override
     public java.time.LocalDate getTradeDateAsDate() {
         return com.core.util.MessageUtils.getDate(buffer, Offsets.TradeDate);  	 
     }

    @Override
    public void setTradeDate(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.TradeDate, value);  									
    }

    @Override
    public void setTradeDateAsDate(java.time.LocalDate value) {
    	com.core.util.MessageUtils.setDate(buffer, Offsets.TradeDate, value);  									
    }

    @Override
    public boolean hasTradeDate() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.TradeDate, Lengths.TradeDate);  
    }

     @Override
     public long getTradeTime() {
         return com.core.util.MessageUtils.getLong(buffer, Offsets.TradeTime);  	 
     }

    @Override
    public void setTradeTime(long value) {
    	com.core.util.MessageUtils.setLong(buffer, Offsets.TradeTime, value);  									
    }

    @Override
    public boolean hasTradeTime() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.TradeTime, Lengths.TradeTime);  
    }

     @Override
     public int getSettlementDate() {
         return com.core.util.MessageUtils.getInt(buffer, Offsets.SettlementDate);  	 
     }

     @Override
     public java.time.LocalDate getSettlementDateAsDate() {
         return com.core.util.MessageUtils.getDate(buffer, Offsets.SettlementDate);  	 
     }

    @Override
    public void setSettlementDate(int value) {
    	com.core.util.MessageUtils.setInt(buffer, Offsets.SettlementDate, value);  									
    }

    @Override
    public void setSettlementDateAsDate(java.time.LocalDate value) {
    	com.core.util.MessageUtils.setDate(buffer, Offsets.SettlementDate, value);  									
    }

    @Override
    public boolean hasSettlementDate() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.SettlementDate, Lengths.SettlementDate);  
    }

     @Override
     public long getCommissionAmount() {
         return com.core.util.MessageUtils.getLong(buffer, Offsets.CommissionAmount);  	 
     }

     @Override
     public String getCommissionAmountAs32nd() {
         return com.core.match.util.MessageUtils.to32ndPrice(buffer, Offsets.CommissionAmount);  	 
     }

     @Override
     public double getCommissionAmountAsDouble() {
         return com.core.match.util.MessageUtils.getDoublePrice(buffer, Offsets.CommissionAmount);  	 
     }

    @Override
    public void setCommissionAmount(long value) {
    	com.core.util.MessageUtils.setLong(buffer, Offsets.CommissionAmount, value);  									
    }

    @Override
    public void setCommissionAmount(double value) {
    	com.core.match.util.MessageUtils.setDoublePrice(buffer, Offsets.CommissionAmount, value);  									
    }

    @Override
    public boolean hasCommissionAmount() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.CommissionAmount, Lengths.CommissionAmount);  
    }

     @Override
     public java.nio.ByteBuffer getTrader() {
	     return com.core.util.MessageUtils.getRightPaddedFixedString(buffer, Offsets.Trader, Lengths.Trader);
     }

     @Override
     public int getTraderLength() {
	     return com.core.util.MessageUtils.getFixedStringLength(buffer, Offsets.Trader, Lengths.Trader);
     }

     @Override
     public String getTraderAsString() {
	     return com.core.util.MessageUtils.toRightPaddedFixedString(buffer, Offsets.Trader, Lengths.Trader);
     }

    @Override
    public void setTrader(java.nio.ByteBuffer value) {
    	com.core.util.MessageUtils.setRightPaddedFixedString(buffer, Offsets.Trader, value, Lengths.Trader);  						
    }

    @Override
    public void setTrader(String value) {
    	com.core.util.MessageUtils.setRightPaddedFixedString(buffer, Offsets.Trader, value, Lengths.Trader);  						
    }

    @Override
    public boolean hasTrader() {
        return com.core.util.MessageUtils.doesFieldExist(buffer, Offsets.Trader, Lengths.Trader);  
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=TradeConfirmation");
        builder.append(",MsgType=");
        if (hasMsgType()) {
            builder.append(getMsgType());
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
        builder.append(",ClOrdID=");
        if (hasClOrdID()) {
            builder.append(getClOrdID());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",ExecQty=");
        if (hasExecQty()) {
            builder.append(getExecQtyAsQty());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",ExecPrice=");
        if (hasExecPrice()) {
            builder.append(getExecPriceAs32nd());
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
        builder.append(",Side=");
        if (hasSide()) {
            builder.append(getSide());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Security=");
        if (hasSecurity()) {
            builder.append(getSecurityAsString());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",TradeDate=");
        if (hasTradeDate()) {
            builder.append(getTradeDate());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",TradeTime=");
        if (hasTradeTime()) {
            builder.append(getTradeTime());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",SettlementDate=");
        if (hasSettlementDate()) {
            builder.append(getSettlementDate());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",CommissionAmount=");
        if (hasCommissionAmount()) {
            builder.append(getCommissionAmountAs32nd());
        }
        else {
            builder.append("<NULL>");
        }
        builder.append(",Trader=");
        if (hasTrader()) {
            builder.append(getTraderAsString());
        }
        else {
            builder.append("<NULL>");
        }
        return builder.toString();        
    }

    private static class Offsets {
        static int MsgType = 0;
        static int Timestamp = 1;
        static int ClOrdID = 9;
        static int ExecQty = 17;
        static int ExecPrice = 21;
        static int MatchID = 29;
        static int Side = 33;
        static int Security = 34;
        static int TradeDate = 46;
        static int TradeTime = 50;
        static int SettlementDate = 58;
        static int CommissionAmount = 62;
        static int Trader = 70;
    }
	
    private static class Lengths {
        static int MsgType = 1;
        static int Timestamp = 8;
        static int ClOrdID = 8;
        static int ExecQty = 4;
        static int ExecPrice = 8;
        static int MatchID = 4;
        static int Side = 1;
        static int Security = 12;
        static int TradeDate = 4;
        static int TradeTime = 8;
        static int SettlementDate = 4;
        static int CommissionAmount = 8;
        static int Trader = 12;
    }
} 
