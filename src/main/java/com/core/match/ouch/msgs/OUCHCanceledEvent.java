package com.core.match.ouch.msgs;

public interface OUCHCanceledEvent extends OUCHCommonEvent {
    OUCHCanceledCommand toCommand();

    char getReason();
    boolean hasReason();
} 
