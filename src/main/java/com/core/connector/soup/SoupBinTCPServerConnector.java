package com.core.connector.soup;

import com.core.connector.soup.msgs.SoupBaseDispatcher;
import com.core.connector.soup.msgs.SoupByteBufferDispatcher;
import com.core.connector.soup.msgs.SoupConstants;
import com.core.connector.soup.msgs.SoupLoginAcceptedCommand;
import com.core.connector.soup.msgs.SoupLoginRejectedCommand;
import com.core.connector.soup.msgs.SoupLoginRequestEvent;
import com.core.connector.soup.msgs.SoupLoginRequestListener;
import com.core.connector.soup.msgs.SoupLogoutRequestEvent;
import com.core.connector.soup.msgs.SoupLogoutRequestListener;
import com.core.connector.soup.msgs.SoupMessages;
import com.core.connector.soup.msgs.SoupSequencedDataCommand;
import com.core.util.BinaryUtils;
import com.core.util.ByteStringBuffer;
import com.core.util.TextUtils;
import com.core.util.log.Log;
import com.core.util.store.IndexedStore;
import com.core.util.tcp.TCPClientSocket;
import com.core.util.tcp.TCPClientSocketListener;
import com.core.util.tcp.TCPServerSocketSingleClient;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimerHandler;
import com.core.util.time.TimerService;

import java.io.IOException;
import java.nio.ByteBuffer;



/**
 * Created by jgreco on 6/23/15.
 */
public class SoupBinTCPServerConnector extends SoupBinTCPCommonConnector implements
        TCPClientSocketListener,
        SoupServerConnector,
        SoupLoginRequestListener,
        SoupLogoutRequestListener,
        SoupBaseDispatcher.SoupBeforeListener {
    private static final int USERNAME_LENGTH = 6;
    private static final int PASSWORD_LENGTH = 10;

    private final ByteBuffer sessionBuffer = ByteBuffer.allocate(10);
    private final ByteBuffer emptySessionBuffer = ByteBuffer.allocate(10);
    private final ByteBuffer seqNumBuffer = ByteBuffer.allocate(20);
    private final ByteBuffer tempWriteBuffer = ByteBuffer.allocate(1500);
    private final IndexedStore store;
    private final int port;

    private SessionState sessionState = SessionState.None;

    private String username;
    private String password;

    private final TimerHandler endOfSessionHandler = new EndOfSessionTimer();

    private SoupSequencedDataCommand messageCommand;
    private ByteBuffer messageBuffer;
    private int messagePosition;

    private int nextExpectedSeqNum;
    TCPServerSocketSingleClient serverTCP;

    public SoupBinTCPServerConnector(Log log,
                                     TCPSocketFactory tcpFactory,
                                     TimerService timers,
                                     IndexedStore store,
                                     int port,
                                     SoupByteBufferDispatcher dispatcher,
                                     SoupMessages messages,
                                     String username,
                                     String password) throws IOException {
        super(log, timers, dispatcher, messages, tcpFactory, port);

        this.username = username;
        this.password = password;
        this.store = store;
        this.port = port;
        if(username.length()> USERNAME_LENGTH){
            log.error(log.log().add("Username cannot be longer than 6 characters. This will result in error in authenticating."));
        }
        if(password.length()> PASSWORD_LENGTH){
            log.error(log.log().add("Password cannot be longer than 10 characters. This will result in error in authenticating."));
        }

        while(emptySessionBuffer.hasRemaining()) {
            emptySessionBuffer.put((byte)' ');
        }
        emptySessionBuffer.flip();

        dispatcher.subscribe(this);
    }

    @Override
    protected TCPClientSocket getClientSocket(TCPSocketFactory factory, int port)  {
        serverTCP = new TCPServerSocketSingleClient(log, factory, port, this);
        return serverTCP;
    }

    @Override
    protected void sendHeartbeat() {
    	if(  isCaughtUp() ) send(messages.getSoupServerHeartbeatCommand(), false);
    }

    @Override
    public void open() throws IOException {
        serverTCP.open();
        enableRead(true);
    }

    // use this to only read a single message at a time
    @Override
    public void enableRead(boolean enableRead) {
        serverTCP.enableRead(enableRead);

        if (enableRead) {
            doRead(serverTCP);
        }
    }

    @Override
    public ByteBuffer getMessageBuffer() {
        // This is for sequenced and unsequenced data
        // we just use sequenced for simplicity
        // we'll overwrite the message type later
        messageCommand = messages.getSoupSequencedDataCommand();
        messageBuffer = messageCommand.getRawBuffer();
        messagePosition = messageBuffer.position();
        // +3 is the start of the mesage data
        messageBuffer.position(messagePosition + 3);
        return messageBuffer;
    }

    @Override
    public void sendMessage() {
        int newLimit = messageBuffer.position();

        // now that message buffer has been written to, let's rewind
        messageBuffer.limit(newLimit);
        messageBuffer.position(messagePosition);
        int length = messageBuffer.remaining();

        // 2 bytes for MsgType, 1 byte for MsgType
        messageCommand.setLength(length);

        if (isCaughtUp()) {
            send(messageCommand, false);
            nextExpectedSeqNum++;
        }
        else {
            // we have to make sure set the length to add to the store
            messageCommand.setMsgLength((short) (length - Short.BYTES));
        }

        messageBuffer.position(messagePosition);
        messageBuffer.limit(newLimit);
        store.add(messageBuffer);

        messageCommand = null;
        messageBuffer = null;
        messagePosition = -1;
    }

    //When session is passive, we send debug message indicating to the client that we dropped the packet on an inactive session.
    //Upon reaching the max number of dopped packets allowed we disconnect the session.
    @Override
    public void sendDebugMessage(String s) {
        sendDebug(s);

    }

    private void sendQueue() {
        while (serverTCP.canWrite() && !isCaughtUp()) {
            tempWriteBuffer.clear();
            // store is zero-indexed, sequence is 1 indexed
            store.get(nextExpectedSeqNum - 1, tempWriteBuffer);
            nextExpectedSeqNum++;
            tempWriteBuffer.flip();

            if (!serverTCP.write(tempWriteBuffer)) {
                // we'll pick this up again when the write becomes available
                return;
            }
        }
    }

    @Override
    public void onWriteAvailable(TCPClientSocket clientSocket) {
        if (isLoggedIn()) {
            sendQueue();
        }
    }

    @Override
    public void setSession(String session) {
        if (session == null || session.length() == 0) {
            sessionState = SessionState.None;
        }

        sessionState = SessionState.InProgress;
        sessionBuffer.clear();
        TextUtils.writeStringLeftPadded(sessionBuffer, session, sessionBuffer.remaining(), ' ').flip();
    }

    @Override
    public void endSession() {
        // can only end the session a single time
        if (sessionState == SessionState.End) {
            log.error(log.log().add("Tried to end session twice"));
            return;
        }

        sessionState = SessionState.End;
        send(messages.getSoupEndOfSessionCommand(), false);

        timers.scheduleTimer(RECV_HEARTBEAT_TIMEOUT, endOfSessionHandler);
    }

    @Override
    public int getSequence() {
        return store != null ? store.size() : 0;
    }

    @Override
    public int getNextExpectedSequence() {
        return nextExpectedSeqNum;
    }

    @Override
    public boolean isCaughtUp() {
        return getNextExpectedSequence() == getSequence() + 1;
    }

    @Override
    public void onSoupLoginRequest(SoupLoginRequestEvent msg) {
        log.debug(log.log().add("Received login request ").add(msg.toString()));

        if (isLoggedIn()) {
            log.error(log.log().add("Received login request twice"));
            return;
        }

        if (!username.equalsIgnoreCase(msg.getUsernameAsString())) {
            log.error(log.log().add("Received invalid username: ").add(msg.getUsernameAsString()).add("; Server is configured with:").add(username));
            SoupLoginRejectedCommand cmd = messages.getSoupLoginRejectedCommand();
            cmd.setRejectReasonCode(SoupConstants.RejectReason.NotAuthorized);
            send(cmd, true);
            closeAllClients();

            return;
        }

        if (!password.equalsIgnoreCase(msg.getPasswordAsString())) {
            log.error(log.log().add("Received invalid password: ").add(password).add(" for user ").add(username));
            SoupLoginRejectedCommand cmd = messages.getSoupLoginRejectedCommand();
            cmd.setRejectReasonCode(SoupConstants.RejectReason.NotAuthorized);
            send(cmd, true);
            closeAllClients();

            return;
        }

        // check that we've started a session
        // on the core this means that we haven't even started up for the day
        if (sessionState != SessionState.InProgress) {
            log.error(log.log().add("Connection when not in session"));
            SoupLoginRejectedCommand cmd = messages.getSoupLoginRejectedCommand();
            cmd.setRejectReasonCode(SoupConstants.RejectReason.SessionNotAvailable);
            send(cmd, true);
            closeAllClients();

            return;
        }

        // check that the session buffer is either empty (no session specified)
        // or that the session is specified and it's the current session
        if (!BinaryUtils.compare(emptySessionBuffer, msg.getRequestedSession()) && !BinaryUtils.compare(sessionBuffer, msg.getRequestedSession())) {
            log.error(log.log().add("Received invalid session: ").add(msg.getRequestedSession()));
            SoupLoginRejectedCommand cmd = messages.getSoupLoginRejectedCommand();
            cmd.setRejectReasonCode(SoupConstants.RejectReason.SessionNotAvailable);
            send(cmd, true);
            closeAllClients();

            return;
        }

        int requestedSeqNum = TextUtils.parseNumberLeftPadded(msg.getRequestedSequenceNumber());

        // bounds checking for seq num
        if (requestedSeqNum < 0 || requestedSeqNum > getSequence() + 1) {
            log.error(log.log().add("Received invalid seqNum: ").add(msg.getRequestedSequenceNumber()));
            SoupLoginRejectedCommand cmd = messages.getSoupLoginRejectedCommand();
            cmd.setRejectReasonCode(SoupConstants.RejectReason.SessionNotAvailable);
            send(cmd, true);
            closeAllClients();

            return;
        }

        // a zero sequence number means we just want the next outgoing sequence number
        if (requestedSeqNum == 0) {
            requestedSeqNum = getSequence() + 1;
        }

        // this is the next sequence number they are expecting
        nextExpectedSeqNum = Math.min(requestedSeqNum, getSequence() + 1);

        seqNumBuffer.clear();
        TextUtils.writeNumberLeftPadded(seqNumBuffer, nextExpectedSeqNum, seqNumBuffer.remaining(), ' ').flip();

        SoupLoginAcceptedCommand cmd = messages.getSoupLoginAcceptedCommand();
        cmd.setSession(sessionBuffer);
        cmd.setSequenceNumber(seqNumBuffer);
        send(cmd, true);
        setLoggedIn(true);

        sendQueue();
    }

    @Override
    public void onSoupLogoutRequest(SoupLogoutRequestEvent msg) {
        log.debug(log.log().add("Received SoupLogoutRequestEvent, closing all connection. Msg:").add(msg.toString()));
        closeAllClients();

    }

    public String  getSessionState() {
        return sessionState.name();
    }


    private class EndOfSessionTimer implements TimerHandler {
		@Override
        public void onTimer(int internalTimerID, int referenceData) {
            if (isConnected() ) {
                log.error(log.log().add("SOUP End Of Session. Disconnecting"));
                //serverTCP.close();
                serverTCP.closeClients();
            }
        }
    }

    @Override
    public ByteStringBuffer status() {
        ByteStringBuffer status = super.status();
        status.add("Port: ").add(port).addNL();
        status.add("Session: ").add(sessionBuffer).addNL();
        status.add("Username: ").add(username).addNL();
        status.add("Password: ").add(password).addNL();
        status.add("Seq Num: ").add(getSequence()).addNL();
        status.add("Session: ").add(sessionState.toString()).addNL();
        return status;
    }

    private enum SessionState {
        None,
        InProgress,
        End
    }
}
