package com.core.match.itch.msgs;

public interface ITCHSystemCommand extends ITCHCommonCommand {
    void copy(ITCHSystemEvent cmd);
    ITCHSystemEvent toEvent();

    void setEventCode(char val);
}
