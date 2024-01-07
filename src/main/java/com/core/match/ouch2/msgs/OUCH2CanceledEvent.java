package com.core.match.ouch2.msgs;

public interface OUCH2CanceledEvent extends OUCH2CommonEvent {
    OUCH2CanceledCommand toCommand();

    char getReason();
    boolean hasReason();

    int getCanceledQty();
    double getCanceledQtyAsQty();
    boolean hasCanceledQty();
} 
