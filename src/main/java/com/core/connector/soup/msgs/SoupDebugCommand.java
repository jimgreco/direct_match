package com.core.connector.soup.msgs;

public interface SoupDebugCommand extends SoupCommonCommand {
    void copy(SoupDebugEvent cmd);
    SoupDebugEvent toEvent();

    void setText(java.nio.ByteBuffer val);
    void setText(String val);
}
