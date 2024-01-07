package com.core.connector.soup.msgs;

public interface SoupLoginAcceptedCommand extends SoupCommonCommand {
    void copy(SoupLoginAcceptedEvent cmd);
    SoupLoginAcceptedEvent toEvent();

    void setSession(java.nio.ByteBuffer val);
    void setSession(String val);

    void setSequenceNumber(java.nio.ByteBuffer val);
    void setSequenceNumber(String val);
}
