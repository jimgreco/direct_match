package com.core.connector.mold;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.core.app.heartbeats.StubHeartbeatApp;
import com.core.util.NullLog;
import com.core.match.msgs.MatchBaseDispatcher;
import com.core.match.msgs.MatchByteBufferDispatcher;
import com.core.match.msgs.MatchByteBufferMessages;

/**
 * User: jgreco
 */
public class Mold64UDPConnectorTest {
	private Mold64UDP stream;
	private MatchByteBufferDispatcher dispatcher;
	private Mold64UDPConnector connector;
	private String sessionStr = "20140815AA";
	private ByteBuffer session = ByteBuffer.wrap(sessionStr.getBytes());

	private ByteBuffer sampleMsg = ByteBuffer.wrap("X                                                     ".getBytes());

	@Before
	public void setup() {
		StubHeartbeatApp app = new StubHeartbeatApp("CON01");

		dispatcher = new MatchByteBufferDispatcher(new MatchByteBufferMessages());
		stream = mock(Mold64UDP.class);
		connector = new Mold64UDPConnector(new NullLog(), stream, dispatcher);
		connector.onHeartbeatRegister(app);

		verify(stream).setListener(connector);
	}

	@Test
	public void testIgnoreOldPacket() {
        final int[] count = {0};
        dispatcher.subscribe((MatchBaseDispatcher.MatchAfterListener) msg -> {
            if (msg.getMsgType() == 'X') {
                count[0]++;
            }
        });

		connector.onMold64Packet(session, 1);
		connector.onMold64Message(1, sampleMsg);
		connector.onMold64Message(2, sampleMsg);
		connector.onMold64Message(3, sampleMsg);
		connector.onMold64PacketComplete(session, 4);

		connector.onMold64Packet(session, 2);
		connector.onMold64Message(2, sampleMsg);
		connector.onMold64Message(3, sampleMsg);
		connector.onMold64Message(4, sampleMsg);
		connector.onMold64Message(5, sampleMsg);
		connector.onMold64PacketComplete(session, 6);

        Assert.assertEquals(5, count[0]);
	}

	@Test
	public void testIgnoreOldSession() {
        final int[] count = {0};
		dispatcher.subscribe((MatchBaseDispatcher.MatchAfterListener) msg -> {
            if (msg.getMsgType() == 'X') {
                count[0]++;
            }
        });

		connector.onMold64Packet(session, 1);
		connector.onMold64Message(1, sampleMsg);
		connector.onMold64Message(2, sampleMsg);
		connector.onMold64Message(3, sampleMsg);
		connector.onMold64PacketComplete(session, 4);

		ByteBuffer invalidSession = ByteBuffer.wrap("20140814AA".getBytes());
		Assert.assertFalse(connector.onMold64Packet(invalidSession, 1));
        Assert.assertEquals(3, count[0]);
	}

}
