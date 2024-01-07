package com.core.fix.connector;

import com.core.connector.AllCommandsClearedListener;
import com.core.fix.FixParser;
import com.core.match.fix.FixMachine;
import com.core.match.fix.FixStateMachine;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public interface FixConnector    {
    void resendComplete();
    void addListener(FIXConnectorListener listener);
    void init(FixParser parser, FixMachine machine);
    void open() throws IOException;
    void close() throws IOException;
    boolean send(ByteBuffer msg);
	void addConnectionListener(FIXConnectionListener listener);
}
