package com.core.connector.soup.msgs;

public interface SoupLogoutRequestCommand extends SoupCommonCommand {
    void copy(SoupLogoutRequestEvent cmd);
    SoupLogoutRequestEvent toEvent();
}
