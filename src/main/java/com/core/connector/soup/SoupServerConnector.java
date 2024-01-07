package com.core.connector.soup;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 6/30/15.
 */
public interface SoupServerConnector extends SoupCommonConnector {
    void enableRead(boolean enableRead);
    
    int getNextExpectedSequence();
    boolean isCaughtUp();

    void setSession(String session);
    void endSession();

    ByteBuffer getMessageBuffer();
    void sendMessage();

    void sendDebugMessage(String s);
}
