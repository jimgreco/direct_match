package com.core.connector.soup.msgs;

public interface SoupServerHeartbeatCommand extends SoupCommonCommand {
    void copy(SoupServerHeartbeatEvent cmd);
    SoupServerHeartbeatEvent toEvent();
}
