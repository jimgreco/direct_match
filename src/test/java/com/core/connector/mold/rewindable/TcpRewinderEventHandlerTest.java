package com.core.connector.mold.rewindable;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.core.util.NullLog;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;
import com.core.util.store.IndexedStore;
import com.core.util.tcp.TCPClientSocket;
import com.core.util.tcp.TCPServerSocket;
import com.core.util.tcp.TCPSocketFactory;

@RunWith(MockitoJUnitRunner.class)
public class TcpRewinderEventHandlerTest {

	private Log logger;
	private int listenPort;

	@Mock
	private IndexedStore store;
	@Mock
	private TCPSocketFactory tcpSocketFactory;
	@Mock
	private TCPServerSocket tcpServerSocket;
	@Mock
	private TCPClientSocket tcpClientSocket;

	@Mock
	private RewinderSocketPool mockTcpRewinderPool;
	private SingleRewindSocket singleRewindConnectionHandler;


	@Before
	public void setUp() throws IOException{
		logger = new NullLog();
		listenPort = 123;


		when(tcpSocketFactory.createTCPServerSocket(eq(listenPort), any())).thenReturn(tcpServerSocket);
		doAnswer(new WriteMessageAnswer()).when(store).get(anyLong(), any());
	}

	private void setupClientRead() throws IOException {
		setupClientRead((a) -> a.putLong(1));
	}

	private void setupClientRead(Consumer<ByteBuffer> consumer) throws IOException {
		doAnswer(new ClientSocketReadAnswer(consumer)).when(tcpClientSocket).read(any());
		singleRewindConnectionHandler = new SingleRewindSocket(logger,store,tcpServerSocket);

		when(tcpSocketFactory.createTCPServerSocket(eq(listenPort), any())).thenReturn(tcpServerSocket);



	}


	private TcpRewinderEventHandler createTarget() throws IOException {
		return new TcpRewinderEventHandler(logger, store, tcpSocketFactory, listenPort);
	}

	@Test
	public void constructor_always_createsServerSocket() throws IOException {

		// Arrange & Act
		TcpRewinderEventHandler target = createTarget();

		// Assert
		verify(tcpSocketFactory).createTCPServerSocket(listenPort, target);
	}

	@Test
	public void onActive_always_setsEnableAcceptOnRewindServerSocket() throws IOException {

		// Arrange
		TcpRewinderEventHandler target = createTarget();

		// Act
		target.onActive();

		// Assert
		verify(tcpServerSocket).enableAccept(true);
	}

	@Test
	public void onPassive_always_setsDisableAcceptOnRewindServerSocket() throws IOException {

		// Arrange
		TcpRewinderEventHandler target = createTarget();

		// Act
		target.onPassive();

		// Assert
		verify(tcpServerSocket).enableAccept(false);
	}


	@Test
	public void onAccept_always_doNotDisableAcceptOnRewindServerSocket() throws IOException {

		// Arrange
		TcpRewinderEventHandler target = createTarget();

		// Act
		target.onAccept(tcpClientSocket);

		// Assert
		verify(tcpClientSocket).enableRead(true);
	}


	private class ClientSocketReadAnswer implements Answer<Boolean> {

		private final Consumer<ByteBuffer> action;

		public ClientSocketReadAnswer(Consumer<ByteBuffer> action) {
			this.action = action;
		}

		@SuppressWarnings("boxing")
		@Override
		public Boolean answer(InvocationOnMock invocation) throws Throwable {
			Object[] args = invocation.getArguments();
			ByteBuffer buffer = (ByteBuffer) args[0];
			action.accept(buffer);
			return true;

		}

	}

	private class WriteMessageAnswer implements Answer<Void> {

		@Override
		public Void answer(InvocationOnMock invocation) throws Throwable {
			Object[] args = invocation.getArguments();
			ByteBuffer buffer = (ByteBuffer) args[1];
			BinaryUtils.copy(buffer, "ABC");
			return null;
		}

	}
}
