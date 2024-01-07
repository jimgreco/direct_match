package com.core.match.ouch2.msgs;

public interface OUCH2ReplacedEvent extends OUCH2CommonEvent {
    OUCH2ReplacedCommand toCommand();

    long getOldClOrdId();
    boolean hasOldClOrdId();

    int getQty();
    double getQtyAsQty();
    boolean hasQty();

    long getPrice();
    String getPriceAs32nd();
    double getPriceAsDouble();
    boolean hasPrice();

    int getReserved();
    double getReservedAsQty();
    boolean hasReserved();

    int getExternalOrderID();
    boolean hasExternalOrderID();
} 
