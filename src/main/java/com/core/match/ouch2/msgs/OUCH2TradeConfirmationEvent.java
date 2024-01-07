package com.core.match.ouch2.msgs;

public interface OUCH2TradeConfirmationEvent extends OUCH2CommonEvent {
    OUCH2TradeConfirmationCommand toCommand();

    int getExecQty();
    double getExecQtyAsQty();
    boolean hasExecQty();

    long getExecPrice();
    String getExecPriceAs32nd();
    double getExecPriceAsDouble();
    boolean hasExecPrice();

    int getMatchID();
    boolean hasMatchID();

    char getSide();
    boolean hasSide();

    java.nio.ByteBuffer getSecurity();
    int getSecurityLength();
    String getSecurityAsString();
    boolean hasSecurity();

    int getTradeDate();
    java.time.LocalDate getTradeDateAsDate();
    boolean hasTradeDate();

    long getTradeTime();
    boolean hasTradeTime();

    int getSettlementDate();
    java.time.LocalDate getSettlementDateAsDate();
    boolean hasSettlementDate();

    long getCommissionAmount();
    String getCommissionAmountAs32nd();
    double getCommissionAmountAsDouble();
    boolean hasCommissionAmount();

    java.nio.ByteBuffer getTrader();
    int getTraderLength();
    String getTraderAsString();
    boolean hasTrader();
} 
