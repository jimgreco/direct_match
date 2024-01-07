package com.core.connector.mold.rewindable;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Test;

import com.core.util.NullLog;
import com.core.connector.mold.Mold64UDPPacket;
import com.core.util.BinaryUtils;

public class MemoryBackedMessageStoreTest {

	private NullLog logger;
	private ByteBuffer message;

	@Before
	public void setUp() {
		this.logger = new NullLog();
		
		message = ByteBuffer.allocate(Mold64UDPPacket.MTU_SIZE);
		BinaryUtils.copy(message, "ABC123");
		message.position(0);
	}
	
	private MemoryBackedMessageStore createTarget() {
		return new MemoryBackedMessageStore(logger);
	}
	
	@Test
	public void putMessage_firstMessageInStore_returnsTrue() {

		// Arrange
		MemoryBackedMessageStore target = createTarget();

		// Act
		boolean actual = target.putMessage(1, message);

		// Assert
		assertTrue(actual);
	}
	
	@Test
	public void putMessage_firstMessageInStore_putsIntoStore() {

		// Arrange
		MemoryBackedMessageStore target = createTarget();

		// Act
		target.putMessage(1, message);

		// Assert
		message.position(0);
		ByteBuffer actualMessage = target.getMessage(1);
		assertTrue(BinaryUtils.compare(message, actualMessage));
	}
	
	@Test
	public void putMessage_messageBeyondMaxStoreAhead_returnsFalse() {

		// Arrange
		MemoryBackedMessageStore target = createTarget();
		target.putMessage(1, message);
		message.position(0);
		
		// Act
		boolean actual = target.putMessage(1000, message);

		// Assert
		assertFalse(actual);
	}
	
	@Test
	public void putMessage_messageBeyondMaxStoreAhead_doesNotPutIntoStore() {

		// Arrange
		MemoryBackedMessageStore target = createTarget();
		target.putMessage(1, message);
		message.position(0);
		
		// Act
		target.putMessage(1000, message);

		// Assert
		ByteBuffer actualMessage = target.getMessage(1);
		assertTrue(BinaryUtils.compare(message, actualMessage));
	}
	
	@Test
	public void setMaxSequence_messagesBelowStoreahead_evictsOldMessages() {

		// Arrange
		MemoryBackedMessageStore target = createTarget();
		target.putMessage(1, message);
		
		// Act
		target.setMaxSequence(1000);

		// Assert
		assertFalse(target.hasMessage(1));
	}
	
	@Test
	public void getMessage_messageBeyondMaxStoreahead_returnsNull() {

		// Arrange
		MemoryBackedMessageStore target = createTarget();
		target.putMessage(1, message);
		
		// Act
		ByteBuffer actual = target.getMessage(1000);

		// Assert
		assertNull(actual);
	}
	
	@Test
	public void getMessage_messageNotInStore_returnsNull() {

		// Arrange
		MemoryBackedMessageStore target = createTarget();

		// Act
		ByteBuffer actual = target.getMessage(123);

		// Assert
		assertNull(actual);
	}
	
	@Test
	public void getMessage_messageInStore_returnsMessageStored() {

		// Arrange
		MemoryBackedMessageStore target = createTarget();
		target.putMessage(1, message);
		
		// Act
		ByteBuffer actual = target.getMessage(1);

		// Assert
		message.position(0);
		assertTrue(BinaryUtils.compare(message, actual));
	}
	
	@Test
	public void getMessage_messageInStoreAndOriginalModified_returnsOriginalMessageStored() {

		// Arrange
		MemoryBackedMessageStore target = createTarget();
		target.putMessage(1, message);
		message.position(0);
		ByteBuffer buffer = ByteBuffer.allocate(message.capacity());
		BinaryUtils.copy(buffer, message);
		message.clear();
		BinaryUtils.copy(message, "QWERTYU");
		
		// Act
		ByteBuffer actual = target.getMessage(1);

		// Assert
		buffer.position(0);
		assertTrue(BinaryUtils.compare(buffer, actual));
	}
	
	@Test
	public void discardMessage_sequenceBeyondMaxStoreahead_doesNotChangeMessages() {

		// Arrange
		MemoryBackedMessageStore target = createTarget();
		target.putMessage(1, message);
		
		// Act
		target.discardMessage(52);

		// Assert
		assertTrue(target.hasMessage(1));
	}
	
	@Test
	public void discardMessage_messageInStore_discardsMessage() {

		// Arrange
		MemoryBackedMessageStore target = createTarget();
		target.putMessage(1, message);
		
		// Act
		target.discardMessage(1);
		
		// Assert
		assertFalse(target.hasMessage(1));
	}
	
	@Test
	public void popMessage_messageInStore_returnsMessage() {

		// Arrange
		MemoryBackedMessageStore target = createTarget();
		target.putMessage(1, message);
		
		// Act
		ByteBuffer actual = target.popMessage(1);

		// Assert
		message.position(0);
		assertTrue(BinaryUtils.compare(message, actual));
	}
	
	@Test
	public void popMessage_messageInStore_removesMessage() {

		// Arrange
		MemoryBackedMessageStore target = createTarget();
		target.putMessage(1, message);
		
		// Act
		target.popMessage(1);

		// Assert
		assertFalse(target.hasMessage(1));
	}
	
	@Test
	public void popMessage_messageNotInStore_returnsNull() {

		// Arrange
		MemoryBackedMessageStore target = createTarget();

		// Act
		ByteBuffer actual = target.popMessage(1000);

		// Assert
		assertNull(actual);
	}
		
}
