package com.core.match.ouch2.msgs;

public interface OUCH2RejectedEvent extends OUCH2CommonEvent {
    OUCH2RejectedCommand toCommand();

    char getReason();
    boolean hasReason();
} 
