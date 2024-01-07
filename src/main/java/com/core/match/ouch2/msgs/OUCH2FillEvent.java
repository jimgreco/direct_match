package com.core.match.ouch2.msgs;

public interface OUCH2FillEvent extends OUCH2CommonEvent {
    OUCH2FillCommand toCommand();

    int getExecutionQty();
    double getExecutionQtyAsQty();
    boolean hasExecutionQty();

    long getExecutionPrice();
    String getExecutionPriceAs32nd();
    double getExecutionPriceAsDouble();
    boolean hasExecutionPrice();

    int getMatchID();
    boolean hasMatchID();
} 
