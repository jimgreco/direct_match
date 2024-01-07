package com.core.connector.mold;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.core.app.CommandException;
import com.core.match.sequencer.BackupEventQueue;
import com.core.sequencer.BackupQueue;
import org.junit.Before;
import org.junit.Test;

import com.core.util.NullLog;
import com.core.nio.SimulatedSelectorService;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;
import com.core.util.log.SystemOutLog;
import com.core.util.store.MemoryIndexedStore1Index;
import com.core.util.time.SimulatedTimeSource;
import com.core.util.udp.StubUDPSocketFactory;
import com.core.util.udp.StubWritableUDPSocket;
import org.mockito.Mockito;

/**
 * Created by jgreco on 6/15/15.
 */
public class Mold64UDPEventSenderTest {
    private Mold64UDPEventSenderImpl sender;
    private StubWritableUDPSocket socket;
    private String session;
    private SimulatedSelectorService select;
    private MemoryIndexedStore1Index storage;
    private StubUDPSocketFactory socketFactory;
    private BackupQueue backUpQueue;

    @Before
    public void before() throws IOException {
        SimulatedTimeSource timeSource = new SimulatedTimeSource();
        backUpQueue = Mockito.mock(BackupEventQueue.class);
        Log logger = new NullLog();
        select = new SimulatedSelectorService(new SystemOutLog("CORE04-1", "SELECT", timeSource), timeSource);
        socketFactory = new StubUDPSocketFactory();
        storage = new MemoryIndexedStore1Index(1500);
        session = "20150615BB";
        sender = new Mold64UDPEventSenderImpl(logger, timeSource, select, 1, socketFactory, "lo0", "224.0.0.2", (short) 24000, storage,backUpQueue);
        sender.setSession(session);
        sender.setSendEnabled(true);
    }

    @Test
    public void testSendHeartbeat() throws IOException {
        open();

        sender.sendHeartbeat();
        assertEquals(1, socket.getSentPackets());

        ByteBuffer buffer = socket.remove();
        verifySession(buffer, session, 1, (short) 0);

        assertEquals(0, storage.size());
    }

    @Test
    public void testSendMessage_senderEnabledFalse_BackupQueueueInvoked() throws IOException {
        open();
        sender.setSendEnabled(false);
        String msg= "FOO";

        ByteBuffer datagram = ByteBuffer.allocateDirect(2 * Mold64UDPPacket.MTU_SIZE);
        datagram.put(msg.getBytes());

        sendTextMsg(msg);
        sender.flush();

        Mockito.verify(backUpQueue).add(any(ByteBuffer.class));
        assertEquals(0, socket.getSentPackets());
        assertEquals(0, storage.size());

    }

    @Test
    public void testSendMessage_senderEnabledTrue_BackupQueueueNotInvokedAndStuffGotSent() throws IOException {
        open();
        sender.setSendEnabled(true);
        String msg= "FOO";

        ByteBuffer datagram = ByteBuffer.allocateDirect(2 * Mold64UDPPacket.MTU_SIZE);
        datagram.put(msg.getBytes());

        sendTextMsg(msg);
        sender.flush();

        Mockito.verifyZeroInteractions(backUpQueue);
        assertEquals(1, socket.getSentPackets());
        assertEquals(1, storage.size());


    }



    @Test
    public void testSendMessage() throws IOException {
        open();

        sendTextMsg("FOO");
        sender.flush();

        assertEquals(1, socket.getSentPackets());
        ByteBuffer buffer = socket.remove();
        verifySession(buffer, session, 1, (short) 1);
        verifyTextMsgWithLengthPrefix(buffer, "FOO");

        assertEquals(1, storage.size());
        verifyTextMsg2(storage.get(1), "FOO");
    }

    @Test(expected = CommandException.class)
    public void sendMessage_noSessionSet_throwsException() throws IOException {
        //Arrange
        sender.setSession(null);
        open();

        //Act
        sendTextMsg("FOO");

        //Assert

    }
    @Test
    public void testSendMultipleMessages() throws IOException {
        open();

        sendTextMsg("FOO");
        sendTextMsg("BAR");
        sender.flush();

        assertEquals(1, socket.getSentPackets());
        ByteBuffer buffer = socket.remove();
        verifySession(buffer, session, 1, (short) 2);
        verifyTextMsgWithLengthPrefix(buffer, "FOO");
        verifyTextMsgWithLengthPrefix(buffer, "BAR");

        assertEquals(2, storage.size());
        verifyTextMsg2(storage.get(1), "FOO");
        verifyTextMsg2(storage.get(2), "BAR");
    }

    @Test
    public void testMultiplePackets() throws IOException {
        open();

        sendTextMsg("FOO");
        sendTextMsg("BAR");
        sender.flush();

        sendTextMsg("SOO");
        sendTextMsg("ME");
        sendTextMsg("DO");
        sender.flush();

        assertEquals(2, socket.getSentPackets());

        ByteBuffer buffer = socket.remove();
        verifySession(buffer, session, 1, (short) 2);
        verifyTextMsgWithLengthPrefix(buffer, "FOO");
        verifyTextMsgWithLengthPrefix(buffer, "BAR");

        buffer = socket.remove();
        verifySession(buffer, session, 3, (short) 3);
        verifyTextMsgWithLengthPrefix(buffer, "SOO");
        verifyTextMsgWithLengthPrefix(buffer, "ME");
        verifyTextMsgWithLengthPrefix(buffer, "DO");

        assertEquals(5, storage.size());
        verifyTextMsg2(storage.get(1), "FOO");
        verifyTextMsg2(storage.get(2), "BAR");
        verifyTextMsg2(storage.get(3), "SOO");
        verifyTextMsg2(storage.get(4), "ME");
        verifyTextMsg2(storage.get(5), "DO");
    }

    @Test
    public void testForceFlush() throws IOException {
        open();

        String res = "";
        String abc = "ABCDEFGHIJKLMNOPQRSTUVWXY";
        for (int i=0; i<1000/abc.length(); i++) {
            res += abc;
        }

        sendTextMsg(res);
        sendTextMsg(res);
        sender.flush();

        assertEquals(2, socket.getSentPackets());

        ByteBuffer buffer = socket.remove();
        verifySession(buffer, session, 1, (short) 1);
        verifyTextMsgWithLengthPrefix(buffer, res);

        buffer = socket.remove();
        verifySession(buffer, session, 2, (short) 1);
        verifyTextMsgWithLengthPrefix(buffer, res);

        assertEquals(2, storage.size());
        verifyTextMsg2(storage.get(1), res);
        verifyTextMsg2(storage.get(2), res);
    }

    @Test
    public void testNoQueueHeartbeat() throws IOException {
        sender.sendHeartbeat();
        sender.sendHeartbeat();

        open();

        assertEquals(0, socket.getSentPackets());
        assertEquals(0, storage.size());
    }

    @Test
    public void testdoThatWeDoNotSendQueueMessagesAtStart() throws IOException {
        sendTextMsg("FOO");
        sendTextMsg("BAR");
        sender.flush();

        sendTextMsg("SOO");
        sendTextMsg("ME");
        sendTextMsg("DO");
        sender.flush();

        open();

        //No msg is sent
        assertEquals(0, socket.getSentPackets());

    }

    @Test
    public void testSendMessagesWhenWriteAvailable() throws IOException {
        open();

        socket.setNoWrite(true);

        sendTextMsg("FOO");
        sendTextMsg("BAR");
        sender.flush();

        sendTextMsg("SOO");
        sendTextMsg("ME");
        sendTextMsg("DO");
        sender.flush();

        assertEquals(0, socket.getSentPackets());

        socket.setNoWrite(false);
        sender.onWriteAvailable(null);

        assertEquals(5, socket.getSentPackets());

        ByteBuffer buffer = socket.remove();
        verifySession(buffer, session, 1, (short) 1);
        verifyTextMsgWithLengthPrefix(buffer, "FOO");

        buffer = socket.remove();
        verifySession(buffer, session, 2, (short) 1);
        verifyTextMsgWithLengthPrefix(buffer, "BAR");

        buffer = socket.remove();
        verifySession(buffer, session, 3, (short) 1);
        verifyTextMsgWithLengthPrefix(buffer, "SOO");

        buffer = socket.remove();
        verifySession(buffer, session, 4, (short) 1);
        verifyTextMsgWithLengthPrefix(buffer, "ME");

        buffer = socket.remove();
        verifySession(buffer, session, 5, (short) 1);
        verifyTextMsgWithLengthPrefix(buffer, "DO");

        assertEquals(5, storage.size());
        verifyTextMsg2(storage.get(1), "FOO");
        verifyTextMsg2(storage.get(2), "BAR");
        verifyTextMsg2(storage.get(3), "SOO");
        verifyTextMsg2(storage.get(4), "ME");
        verifyTextMsg2(storage.get(5), "DO");
    }

    @Test
    public void testHeartbeat() throws IOException {
        open();

        assertEquals(0, socket.getSentPackets());

        select.runFor(750);
        assertEquals(0, socket.getSentPackets());

        select.runFor(300);
        assertEquals(1, socket.getSentPackets());

        ByteBuffer buffer = socket.remove();
        verifySession(buffer, session, 1, (short) 0);
    }

    @Test
    public void testHeartbeatWithMessages() throws IOException {
        open();

        assertEquals(0, socket.getSentPackets());

        select.runFor(750);
        assertEquals(0, socket.getSentPackets());

        sendTextMsg("FOO");
        sender.flush();

        assertEquals(1, socket.getSentPackets());
        ByteBuffer buffer = socket.remove();
        verifySession(buffer, session, 1, (short) 1);

        select.runFor(300);
        assertEquals(0, socket.getSentPackets());

        select.runFor(1000);
        assertEquals(1, socket.getSentPackets());
    }

    private void sendTextMsg(String msg) {
        ByteBuffer buf = sender.startMessage();
        buf.mark();
        BinaryUtils.copy(buf, msg);
        buf.reset();
        sender.finalizeMessage(msg.length());
    }

    private static void verifySession(ByteBuffer buffer, String expectedSession, int expectedSequenceNumber, short expectedMessageCount) {
    	String actualSession = BinaryUtils.readString(buffer, 10);
    	long actualSequenceNumber = buffer.getLong();
    	short actualMessageCount = buffer.getShort();
    	
    	assertEquals(expectedSession, actualSession);
    	assertEquals(expectedSequenceNumber, actualSequenceNumber);
    	assertEquals(expectedMessageCount, actualMessageCount);
    }

    private static void verifyTextMsgWithLengthPrefix(ByteBuffer buffer, String expectedMessage) {
    	int oldPosition = buffer.position();
    	short length = buffer.getShort();
    	assertEquals(expectedMessage.length(), length);
    	verifyTextMsg(buffer, expectedMessage);
    	buffer.position(oldPosition + length + 2);
    }
    
    private static void verifyTextMsg(ByteBuffer buffer, String expectedMessage) {
    	String actualMessage = BinaryUtils.readString(buffer, expectedMessage.length());
    	assertEquals(expectedMessage, actualMessage);
    }

    private static void verifyTextMsg2(ByteBuffer buffer, String msg) {
        buffer.flip();
        verifyTextMsg(buffer, msg);
    }

    private void open() throws IOException {
        sender.open();
        socket = socketFactory.popWriteSocket();
    }
}
