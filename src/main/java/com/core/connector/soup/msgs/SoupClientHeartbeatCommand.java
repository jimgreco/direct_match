package com.core.connector.soup.msgs;

public interface SoupClientHeartbeatCommand extends SoupCommonCommand {
    void copy(SoupClientHeartbeatEvent cmd);
    SoupClientHeartbeatEvent toEvent();
}
