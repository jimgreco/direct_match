package com.core.connector.soup.msgs;

public interface SoupLoginRequestCommand extends SoupCommonCommand {
    void copy(SoupLoginRequestEvent cmd);
    SoupLoginRequestEvent toEvent();

    void setUsername(java.nio.ByteBuffer val);
    void setUsername(String val);

    void setPassword(java.nio.ByteBuffer val);
    void setPassword(String val);

    void setRequestedSession(java.nio.ByteBuffer val);
    void setRequestedSession(String val);

    void setRequestedSequenceNumber(java.nio.ByteBuffer val);
    void setRequestedSequenceNumber(String val);
}
