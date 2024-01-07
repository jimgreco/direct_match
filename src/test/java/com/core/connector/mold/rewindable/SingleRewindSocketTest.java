package com.core.connector.mold.rewindable;

import com.core.util.NullLog;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;
import com.core.util.store.IndexedStore;
import com.core.util.tcp.TCPClientSocket;
import com.core.util.tcp.TCPServerSocket;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by hli on 6/7/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class SingleRewindSocketTest {
    private Log logger;
    private int listenPort;

    @Mock
    private IndexedStore store;

    @Mock
    private TCPServerSocket tcpServerSocket;

    @Mock
    private TCPClientSocket tcpClientSocket;

    @Before
    public void setUp(){
        logger=new NullLog();
        doAnswer(new WriteMessageAnswer()).when(store).get(anyLong(), any());
    }
    private SingleRewindSocket createTarget(){
        return new SingleRewindSocket(logger,store,tcpServerSocket);
    }

    @Test
    public void onDisconnect_always_doesNotAlterReadabilityOnRewindServerSocket() throws IOException {

        // Arrange
        SingleRewindSocket target = createTarget();

        // Act
        target.onDisconnect(tcpClientSocket);

        // Assert
        verify(tcpServerSocket,never()).enableAccept(true);
        verify(tcpServerSocket,never()).enableAccept(false);

    }

    @Test
    public void onReadAvailable_bufferContainsInvalidRequest_callsCloseOnClientSocket() throws IOException{

        // Arrange
        SingleRewindSocket target = createTarget();
        setupClientRead((t) -> t.put((byte) 'a')); // Invalid

        // Act
        target.onReadAvailable(tcpClientSocket);

        // Assert
        verify(tcpClientSocket).close();
    }

    @SuppressWarnings("boxing")
    @Test
    public void onReadAvailable_bufferContainsSequenceGreaterThanStoreHas_callsCloseOnClientSocket() throws IOException {

        // Arrange
        SingleRewindSocket target = createTarget();
        setupClientRead((t) -> t.putLong(1000));
        when(store.getCurrentIndex()).thenReturn(1);

        // Act
        target.onReadAvailable(tcpClientSocket);

        // Assert
        verify(tcpClientSocket).close();
    }

    @SuppressWarnings("boxing")
    @Test
    public void onReadAvailable_bufferContainsValidSequenceNumber_writesAvailableMessagesInStoreToSocket() throws IOException {

        // Arrange
        SingleRewindSocket target = createTarget();
        setupClientRead();
        when(store.getCurrentIndex()).thenReturn(10);
        when(tcpClientSocket.canWrite()).thenReturn(true);

        // Act
        target.onReadAvailable(tcpClientSocket);

        // Assert
        verify(tcpClientSocket, times(11)).write(any());
    }

    @SuppressWarnings("boxing")
    @Test
    public void onReadAvailable_bufferContainsValidSequenceNumber_writesContentOfAvailableMessagesInStoreToSocket() throws IOException {

        // Arrange
        SingleRewindSocket target = createTarget();
        setupClientRead();
        when(store.getCurrentIndex()).thenReturn(10);
        when(tcpClientSocket.canWrite()).thenReturn(true);

        // Act
        target.onReadAvailable(tcpClientSocket);

        // Assert
        ArgumentCaptor<ByteBuffer> captor = ArgumentCaptor.forClass(ByteBuffer.class);
        verify(tcpClientSocket, times(11)).write(captor.capture());
        List<ByteBuffer> messagesSent = captor.getAllValues();
        for(int i=1; i<10; i++) {
            ByteBuffer message = messagesSent.get(i);
            message.position(4); // skip length prefix
            assertTrue(BinaryUtils.compare(message, "ABC"));
        }
    }

    @SuppressWarnings("boxing")
    @Test
    public void onReadAvailable_bufferContainsValidSequenceNumberAndCanWriteIsLimited_writesContentUntilCanWriteReturnsFalse() throws IOException {

        // Arrange
        SingleRewindSocket target = createTarget();
        setupClientRead();
        when(store.getCurrentIndex()).thenReturn(10);
        when(tcpClientSocket.canWrite()).thenReturn(true).thenReturn(false);

        // Act
        target.onReadAvailable(tcpClientSocket);

        // Assert
        verify(tcpClientSocket, times(1)).write(any());
    }

    @SuppressWarnings("boxing")
    @Test
    public void onReadAvailable_bufferContainsValidSequenceNumberAndAllMessagesWrittenToSocket_callsCloseOnSocket() throws IOException {

        // Arrange
        SingleRewindSocket target = createTarget();
        setupClientRead();
        when(store.getCurrentIndex()).thenReturn(3);
        when(tcpClientSocket.canWrite()).thenReturn(true);

        // Act
        target.onReadAvailable(tcpClientSocket);

        // Assert
        verify(tcpClientSocket).close();
    }

    @SuppressWarnings("boxing")
    @Test
    public void onReadAvailable_activelyRewinding_callsCloseOnSocket() throws IOException {

        // Arrange
        SingleRewindSocket target = createTarget();
        setupClientRead();
        when(store.getCurrentIndex()).thenReturn(10);
        when(tcpClientSocket.canWrite()).thenReturn(true).thenReturn(false);

        target.onReadAvailable(tcpClientSocket);


        // Act
        target.onReadAvailable(tcpClientSocket);

        // Assert
        verify(tcpClientSocket).close();
    }

    @SuppressWarnings("boxing")
    @Test
    public void onWriteAvailable_notRewinding_doesNotCallWriteOnSocket() throws IOException {

        // Arrange
        SingleRewindSocket target = createTarget();
        setupClientRead();
        when(store.getCurrentIndex()).thenReturn(10);
        when(tcpClientSocket.canWrite()).thenReturn(true).thenReturn(false);

        // Act
        target.onWriteAvailable(tcpClientSocket);

        // Assert
        verifyNoMoreInteractions(tcpClientSocket);
    }

    @SuppressWarnings("boxing")
    @Test
    public void onDisconnect_any_closesClientSocket() throws IOException {

        // Arrange
        SingleRewindSocket target = createTarget();
        setupClientRead();
        when(store.getCurrentIndex()).thenReturn(10);
        when(tcpClientSocket.canWrite()).thenReturn(true).thenReturn(false);

        // Act
        target.onDisconnect(tcpClientSocket);

        // Assert
        verify(tcpServerSocket).closeClient(tcpClientSocket);
    }

    @SuppressWarnings("boxing")
    @Test
    public void onWriteAvailable_rewinding_callsWriteOnSocket() throws IOException {

        // Arrange
        SingleRewindSocket target = createTarget();
        setupClientRead();
        when(store.getCurrentIndex()).thenReturn(10);
        when(tcpClientSocket.canWrite()).thenReturn(true).thenReturn(false);
        target.onReadAvailable(tcpClientSocket);
        when(tcpClientSocket.canWrite()).thenReturn(true).thenReturn(false);

        // Act
        target.onWriteAvailable(tcpClientSocket);

        // Assert
        verify(tcpClientSocket, times(2)).write(any());
    }

    private void setupClientRead() {
        setupClientRead((a) -> a.putLong(1));
    }

    private void setupClientRead(Consumer<ByteBuffer> consumer) {
        doAnswer(new ClientSocketReadAnswer(consumer)).when(tcpClientSocket).read(any());
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