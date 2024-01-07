package com.core.match;

import com.core.connector.mold.QueueingMold64UDPCommandSender;
import com.core.match.msgs.MatchAccountCommand;
import com.core.match.msgs.MatchByteBufferMessages;
import com.core.match.msgs.MatchContributorCommand;
import com.core.match.msgs.MatchContributorEvent;
import com.core.match.msgs.MatchMessages;
import com.core.match.msgs.MatchTraderCommand;
import com.core.nio.SelectorService;
import com.core.util.TimeUtils;
import com.core.util.log.SystemOutLog;
import com.core.util.time.SimulatedTimeSource;
import com.core.util.udp.ReadWriteUDPSocket;
import com.core.util.udp.UDPSocketReadWriteListener;
import com.core.util.udp.WritableUDPSocket;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * User: jgreco
 */
@Category(value = WritableUDPSocket.class)
public class MatchCommandSenderTest {
	private SimulatedTimeSource time;
	private MatchCommandSender sender;
	private MatchMessages messages;
	private SelectorService select;
	private int port;
    private String intf = "lo0";
	private QueueingMold64UDPCommandSender queueingMold64UDPCommandSender;

	@Before
	public void before() throws IOException {
		String property = System.getProperty("os.name");
		Assume.assumeTrue(!property.toLowerCase().contains("window"));
		
        if (property.equals("Windows")) {
            intf = null;
        }

		Random random = new Random();
		port = random.nextInt(20000) + 5000;

		messages = new MatchByteBufferMessages();
		time = new SimulatedTimeSource();
		SystemOutLog log = new SystemOutLog("CORE03-1", "SELECT", time);
		log.setDebug(true);
		this.select = new SelectorService(log, time);
		 queueingMold64UDPCommandSender = new QueueingMold64UDPCommandSender("TEST01",
				new SystemOutLog("CORE03-1", "SENDER", time),
				this.select,
				this.select,
				intf,
				"224.0.0.1",
				(short) port);
		this.sender = new MatchByteBufferCommandSender(log, time, queueingMold64UDPCommandSender);

		queueingMold64UDPCommandSender.onSessionDefinition("20140815AA");
	}

	@After
	public void after() throws InterruptedException {
		// on windows machines none of this will have been set
		if (sender == null) return;
		sender.setPassive();
		run(1);
	}

	@Test
	public void testSender_3MsgsInQueueThenCommandCallback_() throws IOException, InterruptedException {
		Listener listener = setupListener();
		defineContributor();

		sender.setActive();
		MatchAccountCommand account = messages.getMatchAccountCommand();
		account.setAccountID((short) 1);
		account.setName(ByteBuffer.wrap("Foo".getBytes()));

		sender.send(account);
		assertEquals(1, queueingMold64UDPCommandSender.getCurrentMessageContribSeqNUm());
		assertEquals(1, queueingMold64UDPCommandSender.getQueueSize());

		MatchAccountCommand account2 = messages.getMatchAccountCommand();
		account2.setAccountID((short) 2);
		account2.setName(ByteBuffer.wrap("Bar".getBytes()));
		sender.send(account2);
		assertEquals(2, queueingMold64UDPCommandSender.getCurrentMessageContribSeqNUm());
		assertEquals(2, queueingMold64UDPCommandSender.getQueueSize());

		MatchAccountCommand account3 = messages.getMatchAccountCommand();
		account3.setAccountID((short) 3);
		account3.setName(ByteBuffer.wrap("HAHA".getBytes()));
		sender.send(account3);
		assertEquals(3, queueingMold64UDPCommandSender.getCurrentMessageContribSeqNUm());
		assertEquals(3, queueingMold64UDPCommandSender.getQueueSize());


		queueingMold64UDPCommandSender.onMyMessage(1);
		assertEquals(3, queueingMold64UDPCommandSender.getCurrentMessageContribSeqNUm());
		assertEquals(1, queueingMold64UDPCommandSender.getContribSeqNum());
		assertEquals(1, queueingMold64UDPCommandSender.getLastSeenContribSeqNum());

		assertEquals(2, queueingMold64UDPCommandSender.getQueueSize());
		queueingMold64UDPCommandSender.onMyMessage(2);
		assertEquals(3, queueingMold64UDPCommandSender.getCurrentMessageContribSeqNUm());
		assertEquals(2, queueingMold64UDPCommandSender.getContribSeqNum());
		assertEquals(2, queueingMold64UDPCommandSender.getLastSeenContribSeqNum());

		assertEquals(1, queueingMold64UDPCommandSender.getQueueSize());
		queueingMold64UDPCommandSender.onMyMessage(3);
		assertEquals(3, queueingMold64UDPCommandSender.getCurrentMessageContribSeqNUm());
		assertEquals(3, queueingMold64UDPCommandSender.getContribSeqNum());
		assertEquals(3, queueingMold64UDPCommandSender.getLastSeenContribSeqNum());

		assertEquals(0, queueingMold64UDPCommandSender.getQueueSize());

		MatchAccountCommand acct4 = messages.getMatchAccountCommand();
		acct4.setAccountID((short) 3);
		acct4.setName(ByteBuffer.wrap("HAHA".getBytes()));
		sender.send(acct4);
		assertEquals(4, queueingMold64UDPCommandSender.getCurrentMessageContribSeqNUm());
		assertEquals(4, queueingMold64UDPCommandSender.getContribSeqNum());//Because Send Successful
		assertEquals(3, queueingMold64UDPCommandSender.getLastSeenContribSeqNum());
		assertEquals(1, queueingMold64UDPCommandSender.getQueueSize());
	}


	@Test
	public void testDefineContributor() throws InterruptedException {
		try {
			sender.setActive();
			fail("Able to set active without defining contributor");
		} catch (Exception ignored) {
		}

		defineContributor();

		sender.setActive();
		run(1);

		sender.setPassive();
		run(1);
	}

	@Test
	public void testFailToSend() throws InterruptedException {
		MatchAccountCommand account = messages.getMatchAccountCommand();
		account.setAccountID((short) 1);
		account.setName(ByteBuffer.wrap("Foo".getBytes()));

		assertFalse(sender.send(account));

		defineAndSetActive();
		run(1);

		sender.setPassive();
		run(1);
	}

	@Test
	public void testRetryTimer_whenSenderCannotSend() throws IOException, InterruptedException {
		Listener listener = setupListener();

		MatchAccountCommand account = messages.getMatchAccountCommand();
		account.setAccountID((short) 1);
		account.setName(ByteBuffer.wrap("Foo".getBytes()));

		assertFalse(sender.send(account));

		assertEquals(0, listener.datagramRcvCount);

		MatchAccountCommand account2 = messages.getMatchAccountCommand();
		account2.setAccountID((short) 2);
		account2.setName(ByteBuffer.wrap("Bar".getBytes()));

		assertFalse(sender.send(account));

		defineAndSetActive();

		System.out.println("SetActive");

		assertTrue(sender.send(account));
		run(1);

		assertEquals(1, listener.datagramRcvCount);
		MatchTraderCommand tcmd = messages.getMatchTraderCommand();
		tcmd.setAccountID((short) 1);
		tcmd.setName("test");
		sender.send(tcmd);
		run(5);
		assertEquals(1, listener.datagramRcvCount);

		run(125);
		assertEquals(2, listener.datagramRcvCount);

		sender.onMyMessage(1);
		run(1);
		assertEquals(3, listener.datagramRcvCount);

		sender.setPassive();
		run(1);
	}



	@Test
	public void testRetryMax() throws IOException, InterruptedException {
		Listener listener = setupListener();

		Thread.sleep(1000);

		MatchAccountCommand account = messages.getMatchAccountCommand();
		account.setAccountID((short) 1);
		account.setName(ByteBuffer.wrap("Foo".getBytes()));
        account.setNetDV01Limit(100);

		assertFalse(sender.send(account));
		assertEquals(0, listener.datagramRcvCount);

		defineAndSetActive();

		assertTrue(sender.send(account));
		run(1);
		assertEquals(1, listener.datagramRcvCount);

		assertTrue(sender.isActive());

		for (int i = 2; i < 150; i++) {
			run(12500);
			System.out.print("loop:" + i + "count" + listener.datagramRcvCount + "\n");
			assertEquals(Math.min(i, 101), listener.datagramRcvCount);
		}

		assertFalse(sender.isActive());
	}

	private Listener setupListener() throws IOException, InterruptedException {
		Listener listener = new Listener();

		ReadWriteUDPSocket socket = select.createReadWriteUDPSocket(listener);
		socket.open();
		socket.join(intf, "224.0.0.1", (short) port);
		socket.enableRead(true);
		run(1);
		return listener;
	}

	private void defineContributor() {
		MatchContributorCommand contributor = messages.getMatchContributorCommand();
		contributor.setSourceContributorID((short) 1);
		contributor.setName(ByteBuffer.wrap("TEST01".getBytes()));
		sender.onMatchContributor((MatchContributorEvent) contributor);
	}

	private void defineAndSetActive() throws InterruptedException {
		defineContributor();
		sender.setActive();
		run(1);
	}

	private void run(int millis) throws InterruptedException {
		time.setTimestamp(time.getTimestamp() + millis * TimeUtils.NANOS_PER_MILLI);
		select.runOnce();
		Thread.sleep(1);
		select.runOnce();
	}

	public static class Listener
			implements
			UDPSocketReadWriteListener {
		public int datagramRcvCount;

		@Override
		public void onDatagram(ReadWriteUDPSocket clientSocket, ByteBuffer datagram, InetSocketAddress addr) {

			datagramRcvCount++;
		}

		@Override
		public void onWriteAvailable(WritableUDPSocket clientSocket) {

		}

		@Override
		public void onWriteUnavailable(WritableUDPSocket clientSocket) {

		}
	}
}
