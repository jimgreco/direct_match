package com.core.match.ouch.msgs;

public interface OUCHRejectedEvent extends OUCHCommonEvent {
    OUCHRejectedCommand toCommand();

    char getReason();
    boolean hasReason();
} 
