package com.core.connector.soup;

import com.core.GenericTest;
import com.core.connector.soup.msgs.SoupByteBufferDispatcher;
import com.core.connector.soup.msgs.SoupByteBufferMessages;
import com.core.connector.soup.msgs.SoupConstants;
import com.core.connector.soup.msgs.SoupLoginAcceptedEvent;
import com.core.connector.soup.msgs.SoupLoginRejectedEvent;
import com.core.connector.soup.msgs.SoupLoginRequestCommand;
import com.core.connector.soup.msgs.SoupLoginRequestEvent;
import com.core.connector.soup.msgs.SoupSequencedDataCommand;
import com.core.connector.soup.msgs.SoupSequencedDataEvent;
import com.core.connector.soup.msgs.SoupUnsequencedDataCommand;
import com.core.connector.soup.msgs.SoupUnsequencedDataEvent;
import com.core.connector.soup.msgs.SoupUnsequencedDataListener;
import com.core.util.BinaryUtils;
import com.core.util.TimeUtils;
import com.core.util.store.StubIndexedStore;
import com.core.util.tcp.StubTCPClientSocket;
import com.gs.collections.impl.list.mutable.FastList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.Assert.assertNull;

/**
 * Created by jgreco on 6/25/15.
 */
public class SoupBinTCPConnectorTest extends GenericTest {
    private static final String HOST = "127.0.0.1";
    private static final short PORT = 1000;
    private static final String USERNAME = "MYUN";
    private static final String PASSWORD = "MYPW";
    private static final String SESSION = "20150630AA";

    private static final int SEND_HEARTBEAT_MILLIS = (int) (SoupBinTCPCommonConnector.SEND_HEARTBEAT_TIMEOUT / TimeUtils.NANOS_PER_MILLI);
    private static final int RECV_HEARTBEAT_MILLIS = (int) (SoupBinTCPCommonConnector.RECV_HEARTBEAT_TIMEOUT / TimeUtils.NANOS_PER_MILLI);

    private SoupByteBufferMessages messages = new SoupByteBufferMessages();

    private SoupBinTCPServerConnector server;
    private StubTCPClientSocket serverSocket;
    private StubIndexedStore serverStore;

    private SoupBinTCPClientConnector client;
    private StubTCPClientSocket clientSocket;
    private SoupByteBufferDispatcher serverDispatcher;

    @Before
    public void before() {
        tcpSockets.createServerClientPair();

        serverStore = new StubIndexedStore();
    }

    @Test
    public void testLoginWrongUsername() throws IOException {
        connectInvalid("FOOBAR", PASSWORD, 1, SoupConstants.RejectReason.NotAuthorized);
    }

    @Test
    public void testLoginWrongPassword() throws IOException {
        connectInvalid(USERNAME, "WTFB", 1, SoupConstants.RejectReason.NotAuthorized);
    }

    @Test
    public void testNoHeartbeatIfNotLoggedIn() throws IOException {
        connectNoLogin(SESSION);

        select.runFor(2 * SEND_HEARTBEAT_MILLIS);
        assertNoMessages();
    }

    @Test
    public void testDisconnectIfNoHeartbeat() throws IOException {
        connectNoLogin(SESSION);

        select.runFor(RECV_HEARTBEAT_MILLIS / 2);
        assertNoMessages();
        Assert.assertTrue(client.isConnected());
        Assert.assertTrue(server.isConnected());

        select.runFor(RECV_HEARTBEAT_MILLIS / 2);
        assertNoMessages();
        Assert.assertFalse(client.isConnected());
        Assert.assertFalse(server.isConnected());
    }

    @Test
    public void testHeartbeatAfterLoginIfNoMessages() throws IOException {
        connectValid();
        //login();

        select.runFor(SEND_HEARTBEAT_MILLIS / 2);
        assertNoMessages();

        select.runFor(SEND_HEARTBEAT_MILLIS / 2);
        Assert.assertEquals('H', messages.getSoupServerHeartbeatEvent(clientSocket.removeRecv()).getMsgType());
        Assert.assertEquals('R', messages.getSoupClientHeartbeatEvent(serverSocket.removeRecv()).getMsgType());
        assertNoMessages();
    }

    @Test
    public void testMultipleHeartbeats() throws IOException {
        connectValid();
        //login();

        for (int i=0; i<5; i++) {
            select.runFor(SEND_HEARTBEAT_MILLIS);
            Assert.assertEquals('H', messages.getSoupServerHeartbeatEvent(clientSocket.removeRecv()).getMsgType());
            Assert.assertEquals('R', messages.getSoupClientHeartbeatEvent(serverSocket.removeRecv()).getMsgType());
        }
        assertNoMessages();
    }

    @Test
    public void testNoHeartbeatOnMessage() throws IOException {
        connectNoLogin(SESSION);
        select.runFor(SEND_HEARTBEAT_MILLIS / 2);

        client.login(USERNAME, PASSWORD, "", 1);
        checkLogin(1, 1);

        select.runFor(SEND_HEARTBEAT_MILLIS / 2);
        assertNoMessages();

        server.sendDebug("FOO");
        select.runFor(SEND_HEARTBEAT_MILLIS / 2);
        Assert.assertEquals("FOO", messages.getSoupDebugEvent(clientSocket.removeRecv()).getTextAsString());
        Assert.assertEquals('R', messages.getSoupClientHeartbeatEvent(serverSocket.removeRecv()).getMsgType());
        assertNoMessages();

        select.runFor(SEND_HEARTBEAT_MILLIS / 2);
        Assert.assertEquals('H', messages.getSoupServerHeartbeatEvent(clientSocket.removeRecv()).getMsgType());
        assertNoMessages();

        select.runFor(SEND_HEARTBEAT_MILLIS / 2);
        Assert.assertEquals('R', messages.getSoupClientHeartbeatEvent(serverSocket.removeRecv()).getMsgType());
        assertNoMessages();
    }

    @Test
    public void testClientDisconnectNoHeartbeat() throws IOException {
        connectValid();
        //login();

        clientSocket.dropRecv(true);

        select.runFor(5 * RECV_HEARTBEAT_MILLIS / 10);
        Assert.assertTrue(client.isConnected());

        select.runFor(5 * RECV_HEARTBEAT_MILLIS / 10);
        Assert.assertFalse(client.isConnected());
    }

    @Test
    public void testServerDisconnectNoHeartbeat() throws IOException {
        connectValid();
        //login();

        serverSocket.dropRecv(true);

        select.runFor(5 * RECV_HEARTBEAT_MILLIS / 10);
        Assert.assertTrue(server.isConnected());

        select.runFor(5 * RECV_HEARTBEAT_MILLIS / 10);
        Assert.assertFalse(server.isConnected());
    }

    @Test
    public void testEndOfSession() throws IOException {
        connectValid();
        //login();

        server.endSession();
        Assert.assertEquals('Z', messages.getSoupServerHeartbeatEvent(clientSocket.removeRecv()).getMsgType());
        Assert.assertTrue(server.isConnected());

        select.runFor(RECV_HEARTBEAT_MILLIS);
        Assert.assertFalse(server.isConnected());
    }

    @Test
    public void testLoginEndOfSessionFails() throws IOException {
        SoupByteBufferMessages serverMessages = new SoupByteBufferMessages();
        serverDispatcher = new SoupByteBufferDispatcher(serverMessages);
        server = new SoupBinTCPServerConnector(log, tcpSockets, select, serverStore, PORT, serverDispatcher, serverMessages, USERNAME, PASSWORD);
        server.setSession(SESSION);
        server.open();
        server.endSession();

        SoupByteBufferMessages clientMessages = new SoupByteBufferMessages();
        SoupByteBufferDispatcher clientDispatcher = new SoupByteBufferDispatcher(clientMessages);
        client = new SoupBinTCPClientConnector(log, tcpSockets, select, clientMessages, clientDispatcher, HOST, PORT, USERNAME, PASSWORD);
        client.open();

        clientSocket = tcpSockets.getClient(0);
        serverSocket = tcpSockets.getClient(1);

        SoupLoginRejectedEvent rejected = messages.getSoupLoginRejectedEvent(clientSocket.removeRecv());
        Assert.assertEquals(SoupConstants.RejectReason.SessionNotAvailable, rejected.getRejectReasonCode());
    }

    @Test
    public void testSingleEndOfSessionMessage() throws IOException {
        connectValid();
        //login();

        server.endSession();
        Assert.assertEquals('Z', messages.getSoupEndOfSessionEvent(clientSocket.removeRecv()).getMsgType());
        Assert.assertTrue(server.isConnected());

        server.endSession();
        assertNoMessages();

        select.runFor(RECV_HEARTBEAT_MILLIS);
        Assert.assertFalse(server.isConnected());
    }

    @Test
    public void testSendSequenced() throws IOException {
        connectValid();
        //login();

        ByteBuffer buffer = server.getMessageBuffer();
        BinaryUtils.copy(buffer, "FOO");
        server.sendMessage();

        SoupSequencedDataEvent seqData = messages.getSoupSequencedDataEvent(clientSocket.removeRecv());
        Assert.assertEquals('S', seqData.getMsgType());
        Assert.assertEquals("FOO", BinaryUtils.toString(seqData.getMessage()));
//        Assert.assertEquals(1, client.getSequence());
        Assert.assertEquals(1, serverStore.size());

        server.getMessageBuffer();
        BinaryUtils.copy(buffer, "BAR");
        server.sendMessage();

        seqData = messages.getSoupSequencedDataEvent(clientSocket.removeRecv());
        Assert.assertEquals('S', seqData.getMsgType());
        Assert.assertEquals("BAR", BinaryUtils.toString(seqData.getMessage()));
        Assert.assertEquals(2, serverStore.size());
//        Assert.assertEquals(2, client.getSequence());

        assertNoMessages();
    }

    @Test
    public void testSendUnsequenced() throws IOException {
        connectValid();
        //login();

        SoupUnsequencedDataCommand cmd = messages.getSoupUnsequencedDataCommand();
        cmd.setMessage(ByteBuffer.wrap("FOO".getBytes()));
        client.send(cmd);

        SoupUnsequencedDataEvent data = messages.getSoupUnsequencedDataEvent(serverSocket.removeRecv());
        Assert.assertEquals('U', data.getMsgType());
        Assert.assertEquals("FOO", BinaryUtils.toString(data.getMessage()));

        cmd = messages.getSoupUnsequencedDataCommand();
        cmd.setMessage(ByteBuffer.wrap("BAR".getBytes()));
        client.send(cmd);

        data = messages.getSoupUnsequencedDataEvent(serverSocket.removeRecv());
        Assert.assertEquals('U', data.getMsgType());
        Assert.assertEquals("BAR", BinaryUtils.toString(data.getMessage()));
    }

    @Test
    public void testStoreSizeIsSeqNum() throws IOException {
        addQuickMessage("FOO");
        addQuickMessage("FOO");
        addQuickMessage("FOO");
        addQuickMessage("FOO");

        SoupByteBufferMessages serverMessages = new SoupByteBufferMessages();
        serverDispatcher = new SoupByteBufferDispatcher(serverMessages);
        server = new SoupBinTCPServerConnector(log, tcpSockets, select, serverStore, PORT, serverDispatcher, serverMessages, USERNAME, PASSWORD);
        Assert.assertEquals(4, server.getSequence());
    }

    @Test
    public void testFailedLoginSeqNumTooHigh() throws IOException {
        testFailedLoginForSession(SESSION, SESSION, 5);
    }

    @Test
    public void testFailedLoginWrongSession() throws IOException {
        testFailedLoginForSession(SESSION, "FOO", 1);
    }

    @Test
    public void testLoginNoSession() throws IOException {
        testFailedLoginForSession("", SESSION, 1);
    }

    @Test
    public void testLoginZeroIsCurrentSequenceNumber() throws IOException {
        serverStore.add(ByteBuffer.wrap("FOO".getBytes()));
        serverStore.add(ByteBuffer.wrap("BAR".getBytes()));
        serverStore.add(ByteBuffer.wrap("SOO".getBytes()));

        connectValid(USERNAME, PASSWORD, 0, 4);
        //login(0, 4);

        Assert.assertTrue(server.isLoggedIn());
        Assert.assertTrue(client.isLoggedIn());
    }

    @Test
    public void testLoginLowerCaseUNAndPW() throws IOException {
        connectValid(USERNAME.toLowerCase(), PASSWORD.toLowerCase(), 1, 1);
        //login(0, 4);

        Assert.assertTrue(server.isLoggedIn());
        Assert.assertTrue(client.isLoggedIn());
    }

    @Test
    public void testFullReplayOnLogin() throws IOException {
        addQuickMessage("FOO");
        addQuickMessage("BAR");
        addQuickMessage("SOO");

        connectValid();
        //login();

        Assert.assertEquals("FOO", getNextSequencedData());
        Assert.assertEquals("BAR", getNextSequencedData());
        Assert.assertEquals("SOO", getNextSequencedData());
        assertNoMessages();
    }

    @Test
    public void testReplayPartialOnLogin() throws IOException {
        addQuickMessage("FOO");
        addQuickMessage("BAR");
        addQuickMessage("SOO");

        connectValid(USERNAME, PASSWORD, 2, 2);
        //login(2, 2);

        Assert.assertEquals("BAR", getNextSequencedData());
        Assert.assertEquals("SOO", getNextSequencedData());
        assertNoMessages();
    }

    @Test
    public void testStopWriting() throws IOException {
        connectValid();
        //login();

        assertNoMessages();

        server.onWriteUnavailable(serverSocket);

        addQuickMessage("FOO");
        addQuickMessage("BAR");
        addQuickMessage("SOO");

        assertNoMessages();

        server.onWriteAvailable(serverSocket);

        Assert.assertEquals("FOO", getNextSequencedData());
        Assert.assertEquals("BAR", getNextSequencedData());
        Assert.assertEquals("SOO", getNextSequencedData());
        assertNoMessages();
    }

    @Test
    public void testStatus() throws IOException {
        connectValid();

        server.status();
        client.status();
    }

    @Test
    public void tryLoginTwiceNoExtraMessage() throws IOException {
        connectValid();
        //login();

        client.login(USERNAME, PASSWORD, "", 1);

        SoupLoginRequestCommand cmd = messages.getSoupLoginRequestCommand();
        cmd.setMsgLength((short) (cmd.getLength() - Short.BYTES));
        cmd.setRequestedSession("");
        cmd.setRequestedSequenceNumber("                   1");
        cmd.setUsername(USERNAME);
        cmd.setPassword(PASSWORD);

        ByteBuffer rawBuffer = cmd.getRawBuffer();
        rawBuffer.limit(cmd.getLength());
        clientSocket.write(rawBuffer);

        assertNull(clientSocket.peekRecv());
    }

    @Test
    public void testReadCompact() throws IOException {
        connectValid();
        //login();

        List<String> unsequencedDispatched = new FastList<>();
        serverDispatcher.subscribe((SoupUnsequencedDataListener) msg -> {
            unsequencedDispatched.add(BinaryUtils.toString(msg.getMessage()));
        });

        ByteBuffer twoMessageBuffer = ByteBuffer.allocate(7 + 6);

        SoupUnsequencedDataCommand cmd = messages.getSoupUnsequencedDataCommand(twoMessageBuffer);
        cmd.setMsgLength((short) (4 + Byte.BYTES));
        cmd.setMessage(ByteBuffer.wrap("SOOK".getBytes()));

        twoMessageBuffer.position(7);

        cmd = messages.getSoupUnsequencedDataCommand(twoMessageBuffer);
        cmd.setMsgLength((short) (3 + Byte.BYTES));
        cmd.setMessage(ByteBuffer.wrap("BAR".getBytes()));

        twoMessageBuffer.position(0);
        twoMessageBuffer.limit(10);

        clientSocket.write(twoMessageBuffer);

        twoMessageBuffer.limit(twoMessageBuffer.capacity());
        clientSocket.write(twoMessageBuffer);

        Assert.assertEquals("SOOK", unsequencedDispatched.get(0));
        Assert.assertEquals("BAR", unsequencedDispatched.get(1));
    }

    private String getNextSequencedData() {
        return BinaryUtils.toString(messages.getSoupSequencedDataEvent(clientSocket.removeRecv()).getMessage());
    }

    @SuppressWarnings("unused")
	private String getNextUnsequencedData() {
        return BinaryUtils.toString(messages.getSoupSequencedDataEvent(serverSocket.removeRecv()).getMessage());
    }

    private void addQuickMessage(String msg) {
        SoupSequencedDataCommand cmd = messages.getSoupSequencedDataCommand(ByteBuffer.allocate(3 + msg.length()));
        cmd.setMsgLength((short) (msg.length() + Byte.BYTES));
        cmd.setMessage(ByteBuffer.wrap(msg.getBytes()));
        serverStore.add(cmd.getRawBuffer());
    }

    private void connectNoLogin(String expectedSession) throws IOException {
        SoupByteBufferMessages serverMessages = new SoupByteBufferMessages();
        serverDispatcher = new SoupByteBufferDispatcher(serverMessages);
        server = new SoupBinTCPServerConnector(log, tcpSockets, select, serverStore, PORT, serverDispatcher, serverMessages, USERNAME, PASSWORD);
        server.setSession(expectedSession);

        SoupByteBufferMessages clientMessages = new SoupByteBufferMessages();
        SoupByteBufferDispatcher clientDispatcher = new SoupByteBufferDispatcher(clientMessages);
        client = new SoupBinTCPClientConnector(log, tcpSockets, select, clientMessages, clientDispatcher, HOST, PORT, null, null);

        Assert.assertFalse(server.isConnected());
        Assert.assertFalse(client.isConnected());

        server.open();

        Assert.assertFalse(server.isConnected());
        Assert.assertFalse(client.isConnected());

        client.open();

        clientSocket = tcpSockets.getClient(0);
        serverSocket = tcpSockets.getClient(1);
    }

    private void testFailedLoginForSession(String expectedSession, String sentSession, int seqNum) throws IOException {
        connectNoLogin(expectedSession);

        client.login(USERNAME, PASSWORD, sentSession, seqNum);

        SoupLoginRejectedEvent logReq = messages.getSoupLoginRejectedEvent(clientSocket.removeRecv());
        Assert.assertEquals(SoupConstants.RejectReason.SessionNotAvailable, logReq.getRejectReasonCode());

        Assert.assertFalse(server.isLoggedIn());
        Assert.assertFalse(client.isLoggedIn());
    }

    private void connectValid() throws IOException {
        connectValid(USERNAME, PASSWORD, 1, 1);
    }

    private void connectInvalid(String username, String password, int sendSeqNum, char rejectReason) throws IOException {
        connectCommon(username, password, sendSeqNum);

        Assert.assertFalse(server.isLoggedIn());
        Assert.assertFalse(client.isLoggedIn());

        SoupLoginRequestEvent loginRequest = messages.getSoupLoginRequestEvent(serverSocket.removeRecv());
        Assert.assertEquals('L', loginRequest.getMsgType());
        Assert.assertEquals(Integer.toString(sendSeqNum), loginRequest.getRequestedSequenceNumberAsString());
        Assert.assertEquals("          ", loginRequest.getRequestedSessionAsString());

        SoupLoginRejectedEvent loginAccept = messages.getSoupLoginRejectedEvent(clientSocket.removeRecv());
        Assert.assertEquals('J', loginAccept.getMsgType());
        Assert.assertEquals(rejectReason, loginAccept.getRejectReasonCode());

        Assert.assertFalse(server.isConnected());
        Assert.assertFalse(client.isConnected());
    }

    private void connectValid(String username, String password, int sendSeqNum, int expectedSeqNum) throws IOException {
        connectCommon(username, password, sendSeqNum);
        checkLogin(sendSeqNum, expectedSeqNum);
    }

    private void checkLogin(int sendSeqNum, int expectedSeqNum) {
        Assert.assertTrue(server.isLoggedIn());
        Assert.assertTrue(client.isLoggedIn());

        SoupLoginRequestEvent loginRequest = messages.getSoupLoginRequestEvent(serverSocket.removeRecv());
        Assert.assertEquals('L', loginRequest.getMsgType());
        Assert.assertEquals(Integer.toString(sendSeqNum), loginRequest.getRequestedSequenceNumberAsString());
        Assert.assertEquals("          ", loginRequest.getRequestedSessionAsString());

        SoupLoginAcceptedEvent loginAccept = messages.getSoupLoginAcceptedEvent(clientSocket.removeRecv());
        Assert.assertEquals('A', loginAccept.getMsgType());
        Assert.assertEquals(Integer.toString(expectedSeqNum), loginAccept.getSequenceNumberAsString());
        Assert.assertEquals(SESSION, loginAccept.getSessionAsString());

        Assert.assertTrue(server.isConnected());
        Assert.assertTrue(client.isConnected());
    }

    private void connectCommon(String username, String password, int sendSeqNum) throws IOException {
        SoupByteBufferMessages serverMessages = new SoupByteBufferMessages();
        serverDispatcher = new SoupByteBufferDispatcher(serverMessages);
        server = new SoupBinTCPServerConnector(log, tcpSockets, select, serverStore, PORT, serverDispatcher, serverMessages, USERNAME, PASSWORD);
        server.setSession(SESSION);

        SoupByteBufferMessages clientMessages = new SoupByteBufferMessages();
        SoupByteBufferDispatcher clientDispatcher = new SoupByteBufferDispatcher(clientMessages);
        client = new SoupBinTCPClientConnector(log, tcpSockets, select, clientMessages, clientDispatcher, HOST, PORT, username, password);
        client.setLastSeqNum(sendSeqNum - 1);

        Assert.assertFalse(server.isConnected());
        Assert.assertFalse(client.isConnected());

        server.open();

        Assert.assertFalse(server.isConnected());
        Assert.assertFalse(client.isConnected());

        client.open();

        clientSocket = tcpSockets.getClient(0);
        serverSocket = tcpSockets.getClient(1);
    }

    private void assertNoMessages() {
        assertNull(serverSocket.peekRecv());
        assertNull(clientSocket.peekRecv());
    }
}
