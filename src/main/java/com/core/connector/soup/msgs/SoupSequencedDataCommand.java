package com.core.connector.soup.msgs;

public interface SoupSequencedDataCommand extends SoupCommonCommand {
    void copy(SoupSequencedDataEvent cmd);
    SoupSequencedDataEvent toEvent();

    void setMessage(java.nio.ByteBuffer val);
    void setMessage(String val);
}
