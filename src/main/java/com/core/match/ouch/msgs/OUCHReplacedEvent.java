package com.core.match.ouch.msgs;

public interface OUCHReplacedEvent extends OUCHCommonEvent {
    OUCHReplacedCommand toCommand();

    long getOldClOrdId();
    boolean hasOldClOrdId();

    int getQty();
    double getQtyAsQty();
    boolean hasQty();

    long getPrice();
    String getPriceAs32nd();
    double getPriceAsDouble();
    boolean hasPrice();

    int getMaxDisplayedQty();
    double getMaxDisplayedQtyAsQty();
    boolean hasMaxDisplayedQty();
} 
