package com.core.connector.soup.msgs;

public interface SoupUnsequencedDataCommand extends SoupCommonCommand {
    void copy(SoupUnsequencedDataEvent cmd);
    SoupUnsequencedDataEvent toEvent();

    void setMessage(java.nio.ByteBuffer val);
    void setMessage(String val);
}
