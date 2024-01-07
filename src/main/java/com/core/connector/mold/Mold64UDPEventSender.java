package com.core.connector.mold;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by jgreco on 7/22/15.
 */
public interface Mold64UDPEventSender {
    ByteBuffer startMessage();
    void finalizeMessage(int length);
    void flush();

    int getNextSeqNumToSend();
    String getSession();
    void open() throws IOException;
    void close();

    void setSendEnabled(boolean isPrimary);
    boolean isSenderEnabled();

    void setSession(String session);
}
