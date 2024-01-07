package com.core.match.ouch.msgs;

public interface OUCHReplaceEvent extends OUCHCommonEvent {
    OUCHReplaceCommand toCommand();

    long getNewClOrdID();
    boolean hasNewClOrdID();

    int getNewQty();
    double getNewQtyAsQty();
    boolean hasNewQty();

    long getNewPrice();
    String getNewPriceAs32nd();
    double getNewPriceAsDouble();
    boolean hasNewPrice();

    int getNewMaxDisplayedQty();
    double getNewMaxDisplayedQtyAsQty();
    boolean hasNewMaxDisplayedQty();
} 
