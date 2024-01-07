package com.core.connector.soup.msgs;

public interface SoupLoginRejectedCommand extends SoupCommonCommand {
    void copy(SoupLoginRejectedEvent cmd);
    SoupLoginRejectedEvent toEvent();

    void setRejectReasonCode(char val);
}
