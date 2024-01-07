package com.core.connector.soup;

import com.core.connector.soup.msgs.SoupByteBufferDispatcher;
import com.core.connector.soup.msgs.SoupConstants;
import com.core.connector.soup.msgs.SoupEndOfSessionEvent;
import com.core.connector.soup.msgs.SoupEndOfSessionListener;
import com.core.connector.soup.msgs.SoupLoginAcceptedEvent;
import com.core.connector.soup.msgs.SoupLoginAcceptedListener;
import com.core.connector.soup.msgs.SoupLoginRejectedEvent;
import com.core.connector.soup.msgs.SoupLoginRejectedListener;
import com.core.connector.soup.msgs.SoupLoginRequestCommand;
import com.core.connector.soup.msgs.SoupLogoutRequestCommand;
import com.core.connector.soup.msgs.SoupMessages;
import com.core.connector.soup.msgs.SoupSequencedDataEvent;
import com.core.connector.soup.msgs.SoupSequencedDataListener;
import com.core.connector.soup.msgs.SoupUnsequencedDataCommand;
import com.core.util.ByteStringBuffer;
import com.core.util.TextUtils;
import com.core.util.log.Log;
import com.core.util.tcp.ReconnectingTCPClientSocket;
import com.core.util.tcp.TCPClientSocket;
import com.core.util.tcp.TCPClientSocketListener;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimerService;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by jgreco on 6/30/15.
 */
public class SoupBinTCPClientConnector extends SoupBinTCPCommonConnector implements
        TCPClientSocketListener,
        SoupClientConnector,
        SoupLoginRejectedListener,
        SoupLoginAcceptedListener,
        SoupSequencedDataListener,
        SoupEndOfSessionListener {
    private final ByteBuffer seqNumBuffer = ByteBuffer.allocate(20);
    private final ByteBuffer sessionBuffer = ByteBuffer.allocate(10);
    private final String username;
    private final String password;
    private String prevSession;
    private int seqNum;

    private final String host;
    private final int port;

    private TCPClientSocket tcpClient;

    private boolean endOfSession;

    public SoupBinTCPClientConnector(Log log,
                                     TCPSocketFactory tcpFactory,
                                     TimerService timers,
                                     SoupMessages messages,
                                     SoupByteBufferDispatcher dispatcher,
                                     String host,
                                     int port,
                                     String username,
                                     String password) throws IOException {
        super(log, timers, dispatcher, messages, tcpFactory, port);

        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    @Override
    protected TCPClientSocket getClientSocket(TCPSocketFactory tcpFactory, int port) {
        tcpClient = new ReconnectingTCPClientSocket(timers, tcpFactory, log, this);
        return tcpClient;
    }

    @Override
    protected void sendHeartbeat() {
    	 send(messages.getSoupClientHeartbeatCommand(), false);
    }

    @Override
    public void open() throws IOException {
        // clients attempts to connect to the server
        tcpClient.connect(host, (short) port);
        tcpClient.enableRead(true);
    }

    @Override
    public void close() {
        tcpClient.close();
    }

    @Override
    public int getSequence() {
        return seqNum;
    }

    @Override
    public void onSoupEndOfSession(SoupEndOfSessionEvent msg) {
        endOfSession = true;
    }

    @Override
    public void onSoupSequencedData(SoupSequencedDataEvent msg) {
        seqNum++;
    }

    @Override
    public void send(SoupUnsequencedDataCommand cmd) {
        super.send(cmd, false);
    }

    @Override
    public void onSoupLoginAccepted(SoupLoginAcceptedEvent msg) {
        log.info(log.log().add("SOUP Login Accepted"));

        prevSession = msg.getSessionAsString();

        setLoggedIn(true);

    }

    @Override
    public void onSoupLoginRejected(SoupLoginRejectedEvent msg) {
        log.error(log.log().add("SOUP LOGIN ERROR: Code=").add(msg.getRejectReasonCode())
                .add(", Reason=").add(SoupConstants.RejectReason.toString(msg.getRejectReasonCode())));

        setLoggedIn(false);
        close();
    }

    void login(String username, String password, String session, int seqNum) {
        if (isLoggedIn()) {
            log.error(log.log().add("SOUP Tried to login when already logged in"));
            return;
        }

        // start off from where we left off
        seqNumBuffer.clear();
        TextUtils.writeNumberLeftPadded(seqNumBuffer, seqNum, seqNumBuffer.remaining(), ' ').flip();

        sessionBuffer.clear();
        TextUtils.writeStringLeftPadded(sessionBuffer, prevSession != null ? prevSession : session, 10, ' ').flip();

        SoupLoginRequestCommand cmd = messages.getSoupLoginRequestCommand();
        cmd.setUsername(username);
        cmd.setPassword(password);
        cmd.setRequestedSession(sessionBuffer);
        cmd.setRequestedSequenceNumber(seqNumBuffer);
        send(cmd, true);

        log.info(log.log().add("Logging in. Username=").add(username)
                .add(", Session=").add(session)
                .add(", SeqNum=").add(seqNum));

    }

    @Override
    public void onConnect(TCPClientSocket clientSocket) {
        super.onConnect(clientSocket);

        if (username != null && password != null) {
            login(username, password, "", seqNum + 1);
        }

    }

    @Override
    public ByteStringBuffer status() {
        ByteStringBuffer status = super.status();
        status.add("Host: ").add(host).addNL();
        status.add("Port: ").add(port).addNL();
        status.add("Session: ").add(prevSession).addNL();
        status.add("Seq Num: ").add(seqNum).addNL();
        status.add("End Of Session: ").add(endOfSession).addNL();
        return status;
    }

    @Override
    public void onWriteAvailable(TCPClientSocket clientSocket) {

    }

    void setLastSeqNum(int lastSeqNum) {
        this.seqNum = lastSeqNum;
    }

    @Override
    public String getSession() {
        return prevSession;
    }

    @Override
    public String getTargetHost() {
        return host;
    }

    public void sendLogoutMessage(){
        SoupLogoutRequestCommand cmd=messages.getSoupLogoutRequestCommand();
        send(cmd,true);
    }
}
