package com.core.nio;

import com.core.util.BinaryUtils;
import com.core.util.log.SystemOutLog;
import com.core.util.tcp.TCPClientSocket;
import com.core.util.tcp.TCPClientSocketListener;
import com.core.util.tcp.TCPServerSocket;
import com.core.util.tcp.TCPServerSocketAcceptListener;
import com.core.util.time.SimulatedTimeSource;
import com.core.util.udp.ReadWriteUDPSocket;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by jgreco on 1/5/15.
 */
@Category(ReadWriteUDPSocket.class)
public class SelectorTCPClientSocketServerTest {
	private SelectorService selectorService;
	private TCPServerSocket server;
	TCPClientSocket serverClient;
	private Socket client;
	TCPClientSocketListener listener;
	private ByteBuffer buffer;
	private OutputStream clientOut;
	private InputStream clientIn;

	@Before
	public void before() throws IOException {
		SimulatedTimeSource timeSource = new SimulatedTimeSource();
		SystemOutLog log = new SystemOutLog("CORE03-1", "SELECT", timeSource);
		log.setDebug(true);

		buffer = ByteBuffer.allocate(1024);

		Random random = new Random();
		int port = random.nextInt(10000) + 10000;

		listener = Mockito.mock(TCPClientSocketListener.class);
		selectorService = new SelectorService(log, new com.core.util.time.SystemTimeSource());
		server = selectorService.createTCPServerSocket(port, new TCPServerSocketAcceptListener() {
			@Override
			public TCPClientSocketListener onAccept(TCPClientSocket socket) {
				socket.enableRead(true);
				serverClient = socket;
				return listener;
			}
		});
		server.enableAccept(true);
		run();

		client = new Socket();
		client.connect(new InetSocketAddress("127.0.0.1", port));
		clientOut = client.getOutputStream();
		clientIn = client.getInputStream();

		run();
	}

	@After
	public void after() {
		// on windows machines none of this will have been set
		if (client == null || server == null) return;
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		server.enableAccept(false);
		server.closeClients();
		run();
	}

	@Test
	public void testConnectAndDisconnect() {
		// Assert.assertTrue(serverClient.isConnected());
		Assert.assertTrue(serverClient.canWrite());

		serverClient.close();
		run();

		// Assert.assertFalse(serverClient.isConnected());
		Assert.assertFalse(serverClient.canWrite());
	}

	@Test
	public void testRead() throws IOException {
		String msg = "Hi!!!!!";
		String property = System.getProperty("os.name");
		Assume.assumeTrue(!property.contains("Windows"));
		clientOut.write(msg.getBytes());

		run();
		Assert.assertTrue( client.isConnected() );
		Mockito.verify(listener).onReadAvailable(serverClient);

		buffer.clear();
		serverClient.read(buffer);
		buffer.flip();

		Assert.assertEquals(msg, BinaryUtils.toString(buffer));
	}

	@Test
	public void testWrite() {
		String msg = "Hey dawg";
		setBuffer(msg);
		serverClient.write(buffer);

		run();

		Assert.assertEquals(msg, read());
	}

	private void setBuffer(String msg1) {
		buffer.clear();
		BinaryUtils.copy(buffer, msg1);
		buffer.flip();
	}

	private void run() {
		selectorService.runOnce();
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private String read() {
		byte[] bytes = new byte[1024];
		try {
			int read = clientIn.read(bytes);
			return new String(bytes, 0, read);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
