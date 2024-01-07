package com.core.match.ouch2.msgs;

public interface OUCH2CancelRejectedEvent extends OUCH2CommonEvent {
    OUCH2CancelRejectedCommand toCommand();

    char getReason();
    boolean hasReason();
} 
