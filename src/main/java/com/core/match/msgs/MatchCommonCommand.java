package com.core.match.msgs;

public interface MatchCommonCommand extends com.core.connector.CoreCommonCommand {
    MatchCommonEvent toEvent();
}
