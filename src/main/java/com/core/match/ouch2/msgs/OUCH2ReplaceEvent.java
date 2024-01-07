package com.core.match.ouch2.msgs;

public interface OUCH2ReplaceEvent extends OUCH2CommonEvent {
    OUCH2ReplaceCommand toCommand();

    long getNewClOrdID();
    boolean hasNewClOrdID();

    int getNewQty();
    double getNewQtyAsQty();
    boolean hasNewQty();

    long getNewPrice();
    String getNewPriceAs32nd();
    double getNewPriceAsDouble();
    boolean hasNewPrice();

    int getReserved();
    boolean hasReserved();
} 
