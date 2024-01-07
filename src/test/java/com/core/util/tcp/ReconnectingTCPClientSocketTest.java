package com.core.util.tcp;

import com.core.GenericSelectorTest;
import com.core.util.log.SystemOutLog;
import com.core.util.udp.ReadWriteUDPSocket;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Random;

/**
 * Created by jgreco on 1/5/15.
 */
@Category(ReadWriteUDPSocket.class)
public class ReconnectingTCPClientSocketTest extends GenericSelectorTest implements TCPClientSocketListener {
	TCPClientSocket client;
	private ServerSocketChannel server;
	TCPClientSocketListener listener;
    private int port;
	private SocketChannel serverClient;

	@Before
	public void before() throws IOException {
		SystemOutLog log = new SystemOutLog("CORE03-1", "SELECT", time);
		log.setDebug(true);

		Random random = new Random();
		port = random.nextInt(10000) + 10000;

		listener = Mockito.mock(TCPClientSocketListener.class);
		client = new ReconnectingTCPClientSocket(select, select, log, this, 5);
        server = ServerSocketChannel.open();
        server.configureBlocking(false);

		run();
	}

	@Override
	protected boolean isSimulatedTime() {
		return true;
	}

	private void connectClient() throws IOException {
		client.connect("127.0.0.1", (short) port);
		run();
        run();
	}

    private void bindServer() throws IOException {
        server.bind(new InetSocketAddress(port));
        run();
    }

    private void acceptClient() throws IOException {
        serverClient = server.accept();
        //Assert.assertNotNull(serverClient);
        serverClient.configureBlocking(false);
        run();
    }

    private void disconnectClient() throws IOException {
        serverClient.close();
        serverClient = null;
        run();
    }

    private void clientRecognizeDisconnect() {
        client.read(ByteBuffer.allocate(10));
        run();
    }

	@After
	public void after() {
		// on windows machines none of this will have been set
		if (server != null) {
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (client != null) {
			client.close();
			run();
		}
	}

	@Test
	public void testOpenAndCloseRepeatedly() throws IOException {
		bindServer();

        for (int i=0; i<10; i++) {
            connectClient();
            //acceptServer(); // not needed???
            Assert.assertTrue(client.isConnected());
            client.close();
            Assert.assertFalse(client.isConnected());
        }
	}

    @Test
    public void testRetryConnect() throws IOException {
        connectClient();

        advanceTime(2000);
        Assert.assertFalse(client.isConnected());

        bindServer();

        advanceTime(2000);
        Assert.assertFalse(client.isConnected());

        advanceTime(2000);
        acceptClient();
        Assert.assertTrue(client.isConnected());
    }

    @Test
    public void testRetryConnectMultiple() throws IOException {
        connectClient();

        advanceTime(2000);
        Assert.assertFalse(client.isConnected());

        advanceTime(3000);
        Assert.assertFalse(client.isConnected());

        // gonna try reconnect @ 5 s

        advanceTime(3000);
        Assert.assertFalse(client.isConnected());

        bindServer();

        advanceTime(2000);
         Assert.assertFalse(client.isConnected());

        // gonna try reconnect again
        advanceTime(1000);
        advanceTime(1000);
        advanceTime(1000);
        advanceTime(1000);
        advanceTime(1000);
        acceptClient();
        Assert.assertTrue(client.isConnected());
    }

    @Test
    public void testRetryAfterDisconnect() throws IOException {
        bindServer();
        connectClient();
        acceptClient();
        Assert.assertTrue(client.isConnected());

        disconnectClient();
        clientRecognizeDisconnect();
        Assert.assertFalse(client.isConnected());

        advanceTime(2000);
        Assert.assertFalse(client.isConnected());

        advanceTime(3000);
        acceptClient();
        Assert.assertTrue(client.isConnected());
    }

    @Override
    public void onConnect(TCPClientSocket clientSocket) {

    }

    @Override
    public void onDisconnect(TCPClientSocket clientSocket) {

    }

    @Override
    public void onReadAvailable(TCPClientSocket clientSocket) {

    }

    @Override
    public void onWriteAvailable(TCPClientSocket clientSocket) {

    }

    @Override
    public void onWriteUnavailable(TCPClientSocket clientSocket) {

    }
}
