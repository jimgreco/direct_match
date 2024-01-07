package com.core.match.itch.msgs;

public interface ITCHSystemEvent extends ITCHCommonEvent {
    ITCHSystemCommand toCommand();

    char getEventCode();
    boolean hasEventCode();
} 
