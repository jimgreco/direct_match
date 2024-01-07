package com.core.connector.mold.rewindable;

import java.nio.ByteBuffer;

import com.core.connector.mold.Mold64UDPPacket;
import com.core.util.log.Log;

public class MemoryBackedMessageStore implements MessageStore {

	private static final int MAX_STOREAHEAD = 50;
	private static final int STORE_SIZE = 50;
	
	private final Log logger;
	private final ByteBuffer[] store;
	private final boolean[] storePresent;
	
	private long maxSequence;
	
	
	public MemoryBackedMessageStore(Log logger) {
		this.logger = logger;
		store = new ByteBuffer[STORE_SIZE];
		storePresent = new boolean[STORE_SIZE];
		allocateStore();
	}
	
	@Override
	public long getMaxSequence() {
		return maxSequence;
	}
	
	private void allocateStore() {
		for (int i = 0; i < store.length; i++) {
			store[i] = ByteBuffer.allocate(Mold64UDPPacket.MTU_SIZE);
			storePresent[i] = false;
		}
	}

	private int getPosition(long sequence) {
		return (int) (sequence%STORE_SIZE);
	}
	
	@Override
	public boolean putMessage(long sequenceNumber, ByteBuffer message) {
		if(maxSequence == 0) {
			maxSequence = sequenceNumber;
		}
		
		if(sequenceNumber > maxSequence + MAX_STOREAHEAD) {
			logger.debug(logger.log().add("Message store add failure on ").add(sequenceNumber).add(": past max storeahead"));
			return false;
		}
		if(sequenceNumber > maxSequence) {
			setMaxSequence(sequenceNumber);
		}
		storePresent[getPosition(sequenceNumber)] = true;
		ByteBuffer buffer = store[getPosition(sequenceNumber)];
		buffer.clear();
		buffer.put(message);
		logger.debug(logger.log().add("Added message sequence ").add(sequenceNumber).add(" to store"));
		return true;
	}

	@Override
	public void setMaxSequence(long sequenceNumber) {
		long previousMaxSequence = maxSequence;
		maxSequence = sequenceNumber;
		// drop the records that have fallen out of range
		for(int i=1; i<(maxSequence - previousMaxSequence); i++) {
			long oldMessageSequence = maxSequence + MAX_STOREAHEAD + i;
			storePresent[getPosition(oldMessageSequence)] = false;
		}

	}
	
	@Override
	public boolean hasMessage(long sequenceNumber) {
		if(sequenceNumber > maxSequence + MAX_STOREAHEAD) { // TODO: guard against values that have been destroyed
			return false;
		}
		return storePresent[getPosition(sequenceNumber)];
	}

	@Override
	public ByteBuffer getMessage(long sequenceNumber) {
		if(sequenceNumber > maxSequence + MAX_STOREAHEAD) {
			return null;
		}
		if(storePresent[getPosition(sequenceNumber)] == false) {
			return null;
		}
		ByteBuffer message = store[getPosition(sequenceNumber)];
		if(message != null) {
			message = message.duplicate();
			message.flip();
		}
		return message;
	}

	@Override
	public void discardMessage(long sequenceNumber) {
		if(sequenceNumber > maxSequence + MAX_STOREAHEAD) {
			return;
		}
		storePresent[getPosition(sequenceNumber)] = false;
	}

	@Override
	public ByteBuffer popMessage(long sequenceNumber) {
		logger.debug(logger.log().add("Popping message ").add(sequenceNumber).add(" from store"));
		ByteBuffer message = getMessage(sequenceNumber);
		discardMessage(sequenceNumber);
		return message;
	}

}
