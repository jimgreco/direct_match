package com.core.connector.mold.rewindable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyShort;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.core.util.NullLog;
import com.core.connector.ByteBufferDispatcher;
import com.core.connector.SessionSourceListener;
import com.core.connector.mold.Mold64UDPPacket;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;
import com.core.util.tcp.TCPClientSocket;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.udp.ReadWriteUDPSocket;
import com.core.util.udp.UDPSocketFactory;

@RunWith(MockitoJUnitRunner.class)
public class RewindableMold64UDPConnectorTest {

	private Log logger;
	private String multicastGroup;
	private String multicastInterface;
	private short receivePort;
	private Set<RewindLocation> rewindLocations;
	
	@Mock
	private ByteBufferDispatcher dispatcher;
	@Mock
	private UDPSocketFactory udpSocketFactory;
	@Mock
	private TCPSocketFactory tcpSocketFactory;
	@Mock
	private TCPClientSocket tcpClientSocket;
	@Mock
	private ReadWriteUDPSocket readWriteUDPSocket;
	
	private ByteBuffer datagram;
	private InetSocketAddress addr;
	private String session;
	private long sequenceNumber;
	
	private ByteBuffer message1;
	private ByteBuffer message2;
	
	private MessageStore messageStore;
	private String rewindHost;
	private short rewindPort;
	
	@Before
	public void setUp() throws IOException {
		logger = new NullLog();
		multicastGroup = "127.0.0.1";
		multicastInterface = "lo";
		rewindHost = "127.0.0.1";
		rewindPort = 12345;
		rewindLocations = new HashSet<>();
		
		receivePort = 456;
		
		addr = new InetSocketAddress(multicastGroup, receivePort);
		datagram = ByteBuffer.allocate(Mold64UDPPacket.MTU_SIZE);
		
		session = "ASDF123456";
		sequenceNumber = 1;
		
		message1 = ByteBuffer.allocate(3);
		BinaryUtils.copy(message1, "ABC");
		message1.flip();
		message2 = ByteBuffer.allocate(3);
		BinaryUtils.copy(message2, "DEF");
		message2.flip();
		
		messageStore = spy(new MemoryBackedMessageStore(logger));
		
		when(udpSocketFactory.createReadWriteUDPSocket(any())).thenReturn(readWriteUDPSocket);
		when(tcpSocketFactory.createTCPClientSocket()).thenReturn(tcpClientSocket);
	}
	
	private RewindableMold64UDPConnector createTarget() throws IOException {
		rewindLocations.add(new RewindLocation(rewindHost, rewindPort));
		return new RewindableMold64UDPConnector(logger, dispatcher, udpSocketFactory, tcpSocketFactory, messageStore, multicastGroup, multicastInterface, rewindLocations, receivePort);
	}
	
	private void setupMessage(long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
		setupMessage();
	}

	private void setupHeartbeat(long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
		datagram.clear();
		Mold64UDPPacket.init(datagram, session, sequenceNumber, 0);
		datagram.flip();
	}

	private void setupMessage() {
		datagram.clear();
		Mold64UDPPacket.init(datagram, session, sequenceNumber, 1);
		datagram.putShort((short) message1.capacity());
		datagram.put(message1);
		datagram.flip();
		message1.position(0);
	}
	
	private void setupMultipleMessage() {
		datagram.clear();
		Mold64UDPPacket.init(datagram, session, sequenceNumber, 2);
		datagram.putShort((short) message1.capacity());
		datagram.put(message1);
		message1.position(0);
		datagram.putShort((short) message2.capacity());
		datagram.put(message2);
		message2.position(0);
		datagram.flip();
	}
	
	private void verifyMessageSent(ByteBuffer message) {
		verifyMessageSent(message, 1);
	}
	
	private void verifyMessageSent(ByteBuffer message, int messageNumber) {
		ArgumentCaptor<ByteBuffer> messageSent = ArgumentCaptor.forClass(ByteBuffer.class);
		verify(dispatcher, atLeastOnce()).dispatch(messageSent.capture());
		List<ByteBuffer> invocations = messageSent.getAllValues();
		BinaryUtils.compare(invocations.get(messageNumber - 1), message);
	}
	
	private void setupForRewind(RewindableMold64UDPConnector target) {
		setupMessage(100);
		target.onDatagram(readWriteUDPSocket, datagram, addr);
	}
	
	@Test
	public void constructor_always_callsCreateOnTCPClientSocketFactory() throws IOException {

		// Arrange & Act
		RewindableMold64UDPConnector target = createTarget();

		// Assert
		verify(tcpSocketFactory).createTCPClientSocket();
	}
	
	@Test
	public void constructor_always_callsSetListenerOnTcpClientSocket() throws IOException {

		// Arrange & Act
		RewindableMold64UDPConnector target = createTarget();

		// Assert
		verify(tcpClientSocket).setListener(target);
	}
	
	@Test(expected = IllegalStateException.class)
	public void open_calledTwice_throwsIllegalStateException() throws IOException {

		// Arrange
		RewindableMold64UDPConnector target = createTarget();

		// Act & Assert
		target.open();
		target.open();

	}
	
	@Test
	public void open_always_callsJoinOnMulticastSocket() throws IOException {

		// Arrange
		RewindableMold64UDPConnector target = createTarget();

		// Act
		target.open();

		// Assert
		verify(readWriteUDPSocket).join(multicastInterface, multicastGroup, receivePort);
	}
	
	@Test
	public void open_always_callsEnableReadOnMulticastSocket() throws IOException {

		// Arrange
		RewindableMold64UDPConnector target = createTarget();

		// Act
		target.open();

		// Assert
		verify(readWriteUDPSocket).enableRead(true);
	}



	@Test
	public void onDatagram_forcedSingleNewRewinderLocationAndNeedsRewind_callsConnectOnRewindSocket() throws IOException {

		// Arrange
		setupMessage(10);
		RewindableMold64UDPConnector target = createTarget();
		target.setRewindLocation("abc",123,true);
		target.onDatagram(readWriteUDPSocket, datagram, addr);
		target.onDisconnect(tcpClientSocket);
		//Needs Rewind again and verify that we are picking the same port and host as rewinder target
		setupMessage(100);



		// Act
		target.onDatagram(readWriteUDPSocket, datagram, addr);

		// Assert
		verify(tcpClientSocket,times(2)).connect("abc", (short)123);
	}

	@Test
	public void onDatagram_addONeNewRewinderLocationAndNeedsRewind_callsConnectOnRewindSocket() throws IOException {

		// Arrange
		setupMessage(10);
		RewindableMold64UDPConnector target = createTarget();
		target.setRewindLocation("abc",123,false);
		target.onDatagram(readWriteUDPSocket, datagram, addr);
		target.onDisconnect(tcpClientSocket);
		//Needs Rewind again and verify that we are picking the same port and host as rewinder target
		setupMessage(100);



		// Act
		target.onDatagram(readWriteUDPSocket, datagram, addr);

		// Assert
		verify(tcpClientSocket,times(1)).connect("abc", (short)123);
		verify(tcpClientSocket,times(1)).connect(rewindHost, rewindPort);

	}
	@Test
	public void onDatagram_forcedOneRewinderLocationAndNeedsRewind_callsConnectOnRewindSocket() throws IOException {

		// Arrange
		setupMessage(10);
		RewindableMold64UDPConnector target = createTarget();
		target.setRewindLocation("abc",123,true);


		// Act
		target.onDatagram(readWriteUDPSocket, datagram, addr);


		// Assert
		verify(tcpClientSocket).connect("abc", (short)123);
	}

	@Test(expected = IllegalStateException.class)
	public void open_isRewinding_throwsIllegalStateException() throws IOException {

		// Arrange
		RewindableMold64UDPConnector target = createTarget();
		setupForRewind(target);
		
		// Act & Assert
		target.open();
		
	}
	
	@Test
	public void constructor_always_setsCurrentSequenceToOne() throws IOException {

		// Arrange & Act
		long expected = 1;
		RewindableMold64UDPConnector target = createTarget();

		// Assert
		long actual = target.getCurrentSeq();
		assertEquals(expected, actual);
	}

	@Test
	public void onDatagram_isFirstMessage_setsSession() throws IOException {

		// Arrange
		String expected = session;
		setupMessage();
		RewindableMold64UDPConnector target = createTarget();

		// Act
		target.onDatagram(readWriteUDPSocket, datagram, addr);

		// Assert
		String actual = target.getSession();
		assertEquals(expected, actual);
	}
	
	@Test
	public void onDatagram_isFirstMessage_notifiesSessionListeners() throws IOException {

		// Arrange
		SessionSourceListener sessionSourceListener = mock(SessionSourceListener.class);
		
		setupMessage();
		RewindableMold64UDPConnector target = createTarget();
		target.addSessionSourceListener(sessionSourceListener);
		
		// Act
		target.onDatagram(readWriteUDPSocket, datagram, addr);

		// Assert
		verify(sessionSourceListener).onSessionDefinition(session);
	}
	
	@Test
	public void onDatagram_isNextExpectedSequenceNumber_dispatchesMessage() throws IOException {

		// Arrange
		setupMessage();
		RewindableMold64UDPConnector target = createTarget();
		
		// Act
		target.onDatagram(readWriteUDPSocket, datagram, addr);

		// Assert
		verifyMessageSent(message1);
	}
	
	@Test
	public void onDatagram_containsMultipleMessagesWithExpectedSequenceNumber_dispatchesAllMessages() throws IOException {

		// Arrange
		setupMultipleMessage();
		RewindableMold64UDPConnector target = createTarget();

		// Act
		target.onDatagram(readWriteUDPSocket, datagram, addr);

		// Assert
		verifyMessageSent(message1);
		verifyMessageSent(message2);
	}
	
	@Test
	public void onDatagram_notFirstMessageAndSessionDoesNotMatch_doesNotDispatchMessage() throws IOException {

		// Arrange
		setupMessage();
		RewindableMold64UDPConnector target = createTarget();
		target.onDatagram(readWriteUDPSocket, datagram, addr);
		
		session = "NOTLASTSES";
		setupMessage(2);
		
		// Act
		target.onDatagram(readWriteUDPSocket, datagram, addr);
		
		// Assert
		verify(dispatcher, times(1)).dispatch(any());
	}
	
	@Test
	public void onDatagram_notFirstMessageAndSequenceSmallerThanExpected_doesNotDispatchMessage() throws IOException {

		// Arrange
		setupMessage();
		RewindableMold64UDPConnector target = createTarget();
		target.onDatagram(readWriteUDPSocket, datagram, addr);
		
		
		setupMessage(1);
		
		// Act
		target.onDatagram(readWriteUDPSocket, datagram, addr);

		// Assert
		verify(dispatcher, times(1)).dispatch(any());
	}
	
	@Test
	public void onDatagram_messageInvalidWireFormat_doesNotDispatchMessage() throws IOException {

		// Arrange
		RewindableMold64UDPConnector target = createTarget();
		datagram.putDouble(12372);
		datagram.flip();
		
		// Act
		target.onDatagram(readWriteUDPSocket, datagram, addr);

		// Assert
		verifyNoMoreInteractions(dispatcher);
	}
	
	@Test
	public void onDatagram_messageReceivedHigherSequenceNumberThanExpected_doesNotDispatchMessage() throws IOException {

		// Arrange
		setupMessage(10);
		RewindableMold64UDPConnector target = createTarget();

		// Act
		target.onDatagram(readWriteUDPSocket, datagram, addr);

		// Assert
		verifyNoMoreInteractions(dispatcher);
	}
	
	@Test
	public void onDatagram_messageReceivedHigherSequenceNumberThanExpected_callsConnectOnRewindSocket() throws IOException {

		// Arrange
		setupMessage(10);
		RewindableMold64UDPConnector target = createTarget();
		
		// Act
		target.onDatagram(readWriteUDPSocket, datagram, addr);
		
		// Assert
		verify(tcpClientSocket).connect(rewindHost, rewindPort);
	}	
	
	@Test
	public void onDatagram_messageReceivedHigherSequenceNumberThanExpected_addsToMessageStore() throws IOException {

		// Arrange
		RewindableMold64UDPConnector target = createTarget();
		setupForRewind(target);
		setupMessage(115);
		
		// Act
		target.onDatagram(readWriteUDPSocket, datagram, addr);

		// Assert
		verify(messageStore, times(1)).putMessage(eq(115L), any());
	}
	
	@Test
	public void onDatagram_twoMessagesReceivedWithHigherSequenceNumbersThanExpected_callsConnectOnRewindSocketOnce() throws IOException {

		// Arrange
		RewindableMold64UDPConnector target = createTarget();
		setupForRewind(target);
		setupMessage(115);
		
		// Act
		target.onDatagram(readWriteUDPSocket, datagram, addr);

		// Assert
		verify(tcpClientSocket, times(1)).connect(anyString(), anyShort());
	}
	
	@Test
	public void onConnect_notRewinding_callsCloseOnSocket() throws IOException {

		// Arrange
		RewindableMold64UDPConnector target = createTarget();
		
		// Act
		target.onConnect(tcpClientSocket);
		
		// Assert
		verify(tcpClientSocket).close();
	}
	
	@Test
	public void onConnect_requestedRewind_setsReadEnabledOnSocket() throws IOException {

		// Arrange
		RewindableMold64UDPConnector target = createTarget();
		setupForRewind(target);
		
		// Act
		target.onConnect(tcpClientSocket);
		
		// Assert
		verify(tcpClientSocket).enableRead(true);
	}
	
	@Test
	public void onConnect_requestedRewind_sendsStartingSequenceNumberToSocket() throws IOException {

		// Arrange
		RewindableMold64UDPConnector target = createTarget();
		setupForRewind(target);
		long expected = 1; // starting sequence number
		
		// Act
		target.onConnect(tcpClientSocket);
			
		// Assert
		ArgumentCaptor<ByteBuffer> captor = ArgumentCaptor.forClass(ByteBuffer.class);
		verify(tcpClientSocket).write(captor.capture());
		ByteBuffer requestedSequenceBuffer = captor.getValue();
		long actual = requestedSequenceBuffer.getLong();
		assertEquals(expected, actual);
	}
	
	@SuppressWarnings("boxing")
	@Test
	public void onReadAvailable_always_callsReadBytes() throws IOException {

		// Arrange
		when(tcpClientSocket.readBytes(any())).thenReturn(0);
		RewindableMold64UDPConnector target = createTarget();
		setupForRewind(target);
		
		// Act
		target.onReadAvailable(tcpClientSocket);
		
		// Assert
		verify(tcpClientSocket).readBytes(any());
	}
	
	@Test
	public void onReadAvailable_readBytesReturnsMessage_dispatchesMessage() throws IOException {

		// Arrange
		doAnswer(new ReadBytesAnswer(message1)).when(tcpClientSocket).readBytes(any());
		
		RewindableMold64UDPConnector target = createTarget();
		setupForRewind(target);
		
		// Act
		target.onReadAvailable(tcpClientSocket);

		// Assert
		verifyMessageSent(message1);
	}
	
	@Test
	public void onReadAvailable_readBytesReturnsPartialMessage_keepsReadingThenDispatchesCompleteMessageWhenAvailable() throws IOException {

		// Arrange
		doAnswer(new SplitReadBytesAnswer(message1)).when(tcpClientSocket).readBytes(any());
		
		RewindableMold64UDPConnector target = createTarget();
		setupForRewind(target);

		// Act
		target.onReadAvailable(tcpClientSocket);

		// Assert
		verifyMessageSent(message1);
	}
	
	@SuppressWarnings("boxing")
	@Test
	public void onReadAvailable_readBytesReturnsMessage_closesConnectionAfterReachesMaxSequenceNumber() throws IOException {

		// Arrange
		when(tcpClientSocket.isConnected()).thenReturn(true);
		doAnswer(new ReadBytesAnswer(message1)).when(tcpClientSocket).readBytes(any());
		RewindableMold64UDPConnector target = createTarget();

		setupMessage(2);
		target.onDatagram(readWriteUDPSocket, datagram, addr);
		
		// Act
		target.onReadAvailable(tcpClientSocket);
		
		// Assert
		verify(tcpClientSocket).close();
	}


	@SuppressWarnings("boxing")
	@Test
	public void onReadAvailable_testHeartbeatRewind() throws IOException {

		// Arrange
		when(tcpClientSocket.isConnected()).thenReturn(true);
		doAnswer(new ReadBytesAnswer(message1)).when(tcpClientSocket).readBytes(any());
		RewindableMold64UDPConnector target = createTarget();

		setupHeartbeat(2);
		target.onDatagram(readWriteUDPSocket, datagram, addr);

		// Act
		target.onReadAvailable(tcpClientSocket);

		// Assert
		verify(tcpClientSocket).close();
	}
	
	@Test
	public void onDisconnect_always_callsCreateOnTCPSocketFactory() throws IOException {

		// Arrange
		RewindableMold64UDPConnector target = createTarget();

		// Act
		target.onDisconnect(tcpClientSocket);

		// Assert
		verify(tcpSocketFactory, times(2)).createTCPClientSocket();
	}
	
	@Test
	public void onDisconnect_always_setsListenerOnTcpClientSocket() throws IOException {

		// Arrange
		RewindableMold64UDPConnector target = createTarget();

		// Act
		target.onDisconnect(tcpClientSocket);

		// Assert
		verify(tcpClientSocket, times(2)).setListener(target);
	}

	@SuppressWarnings("boxing")
	@Test
	public void onDisconnect_messageStoreHasMessage_processesMessageFromStore() throws IOException {

		// Arrange
		messageStore = mock(MessageStore.class);
		when(messageStore.hasMessage(1)).thenReturn(true);
		when(messageStore.popMessage(1)).thenReturn(message1);
		
		RewindableMold64UDPConnector target = createTarget();
		setupForRewind(target);
		setupMessage(1);
		target.onDatagram(readWriteUDPSocket, datagram, addr);
		
		// Act
		target.onDisconnect(tcpClientSocket);

		// Assert
		verifyMessageSent(message1);
	}
	
	@SuppressWarnings("boxing")
	@Test
	public void onDisconnect_messageStoreHasMultipleMessages_processesMessagesFromStore() throws IOException {

		// Arrange
		messageStore = mock(MessageStore.class);
		when(messageStore.hasMessage(1)).thenReturn(true);
		when(messageStore.hasMessage(2)).thenReturn(true);
		when(messageStore.popMessage(1)).thenReturn(message1);
		when(messageStore.popMessage(2)).thenReturn(message2);
		
		RewindableMold64UDPConnector target = createTarget();
		setupForRewind(target);
		setupMessage(1);
		target.onDatagram(readWriteUDPSocket, datagram, addr);
		
		// Act
		target.onDisconnect(tcpClientSocket);

		// Assert
		verifyMessageSent(message1, 1);
		verifyMessageSent(message2, 2);
	}
	
	private class ReadBytesAnswer implements Answer<Integer> {

		private ByteBuffer message;
		private boolean called = false;

		public ReadBytesAnswer(ByteBuffer message) {
			this.message = message;
		}
		
		@Override
		public Integer answer(InvocationOnMock invocation) throws Throwable {
			if(called) {
				return new Integer(0);
			}
			called = true;
			
			Object[] args = invocation.getArguments();
			ByteBuffer buffer = (ByteBuffer) args[0];
			buffer.putInt(message.limit());
			BinaryUtils.copy(buffer, message);
			return new Integer(message.limit() + 4); // ? bytes of data, 4 bytes of length prefix
		}
		
	}
	
	private class SplitReadBytesAnswer implements Answer<Integer> {
		
		private ByteBuffer message;
		private int calledCount = 0;
		
		public SplitReadBytesAnswer(ByteBuffer message) {
			this.message = message;
		}
		
		@SuppressWarnings("boxing")
		@Override
		public Integer answer(InvocationOnMock invocation) throws Throwable {
			Object[] args = invocation.getArguments();
			ByteBuffer buffer = (ByteBuffer) args[0];
			
			if(calledCount == 2) {
				return 0;
			}
			if(calledCount == 0) {
				calledCount++;
				buffer.putInt(message.limit());
				buffer.put(message.get());
				return 4 + 1; // 4 bytes of length prefix, 1 byte of message
			}
			// calledCount == 1
			calledCount++;
			BinaryUtils.copy(buffer, message);
			return message.limit() - 1; // 1 byte already written earlier
		}
	}
}
