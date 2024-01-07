package com.core.match.ouch.msgs;

public interface OUCHFillEvent extends OUCHCommonEvent {
    OUCHFillCommand toCommand();

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
