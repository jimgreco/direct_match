package com.core.match.msgs;

public interface MatchSystemEventCommand extends com.core.match.msgs.MatchCommonCommand {
    void copy(MatchSystemEventEvent cmd);
    MatchSystemEventEvent toEvent();

    void setEventType(char val);
}
