package com.core.match.ouch.msgs;

public interface OUCHCancelEvent extends OUCHCommonEvent {
    OUCHCancelCommand toCommand();
} 
