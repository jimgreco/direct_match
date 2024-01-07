package com.core.connector;

import com.core.util.ByteStringBuffer;

import java.io.IOException;

/**
 * User: jgreco
 */
public interface Connector {
    void open() throws IOException;
    void close() throws IOException;
    long getCurrentSeq();
    ByteStringBuffer status();
    String getSession();
    void addMessageGroupCompleteListener(MessageGroupCompleteListener listener);
    void addSessionSourceListener(SessionSourceListener listener);
    void addBeforeListener(BeforeMessageListener listener);
    void addAfterListener(AfterMessageListener listener);
}
