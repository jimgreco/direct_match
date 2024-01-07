package com.core.connector.soup;

import com.core.util.ByteStringBuffer;

import java.io.IOException;

/**
 * Created by jgreco on 6/30/15.
 */
public interface SoupCommonConnector {
    void open() throws IOException;
    void close() throws IOException;

    boolean isLoggedIn();
    boolean isConnected();

    void sendDebug(String text);

    int getSequence();
    ByteStringBuffer status();

    void addConnectionListener(SoupConnectionListener listener);

    void closeAllClients();

}
