package com.core.connector.mold.rewindable;

import java.nio.ByteBuffer;

public interface MessageStore {
	void setMaxSequence(long sequenceNumber);
	long getMaxSequence();
	
	boolean putMessage(long sequenceNumber, ByteBuffer message);
	boolean hasMessage(long sequenceNumber);
	ByteBuffer getMessage(long sequenceNumber);
	void discardMessage(long sequenceNumber);
	ByteBuffer popMessage(long sequenceNumber);
	
}
