package com.core.match.ouch.msgs;

public interface OUCHCancelRejectedEvent extends OUCHCommonEvent {
    OUCHCancelRejectedCommand toCommand();

    char getReason();
    boolean hasReason();
} 
