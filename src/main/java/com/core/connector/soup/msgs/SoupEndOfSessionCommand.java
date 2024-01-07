package com.core.connector.soup.msgs;

public interface SoupEndOfSessionCommand extends SoupCommonCommand {
    void copy(SoupEndOfSessionEvent cmd);
    SoupEndOfSessionEvent toEvent();
}
