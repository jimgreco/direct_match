package com.core.connector.soup;

import com.core.connector.soup.msgs.SoupBaseDispatcher;
import com.core.connector.soup.msgs.SoupByteBufferDispatcher;
import com.core.connector.soup.msgs.SoupCommonCommand;
import com.core.connector.soup.msgs.SoupCommonEvent;
import com.core.connector.soup.msgs.SoupDebugCommand;
import com.core.connector.soup.msgs.SoupDebugEvent;
import com.core.connector.soup.msgs.SoupDebugListener;
import com.core.connector.soup.msgs.SoupMessages;
import com.core.util.BinaryUtils;
import com.core.util.ByteStringBuffer;
import com.core.util.TimeUtils;
import com.core.util.log.Log;
import com.core.util.tcp.TCPClientSocket;
import com.core.util.tcp.TCPClientSocketListener;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimerHandler;
import com.core.util.time.TimerService;
import com.gs.collections.impl.list.mutable.FastList;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by jgreco on 6/30/15.
 */
public abstract class SoupBinTCPCommonConnector implements
        TCPClientSocketListener,
        SoupCommonConnector,
        SoupDebugListener,
        SoupBaseDispatcher.SoupBeforeListener {
    public static final long SEND_HEARTBEAT_TIMEOUT = TimeUtils.NANOS_PER_SECOND;
    public static final long RECV_HEARTBEAT_TIMEOUT = 10 * SEND_HEARTBEAT_TIMEOUT;

    protected final Log log;
    protected final SoupMessages messages;
    protected final TimerService timers;

    private final ByteBuffer temp = ByteBuffer.allocate(10000);
    private final ByteStringBuffer status = new ByteStringBuffer();
    private final SoupByteBufferDispatcher dispatcher;

    private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(10000);
    private final TCPClientSocket tcp;
    private final List<SoupConnectionListener> connectionListeners = new FastList<>();

    // timers
    private int sendTimer;
    private int recvTimer;
    private final TimerHandler sendTimerHandler = new HeartbeatSendTimer();
    private final TimerHandler recvTimerHandler = new HeartbeatRecvTimer();

    private boolean connected;
    private boolean loggedIn;

    private int sentMsgs;
    private int recvMsgs;
	boolean ignoreHeartbeats;

    public SoupBinTCPCommonConnector(Log log,
                                     TimerService timers,
                                     SoupByteBufferDispatcher dispatcher,
                                     SoupMessages messages,
                                     TCPSocketFactory tcpFactory,
                                     int port) {
        this.log = log;
        this.dispatcher = dispatcher;
        this.timers = timers;
        this.messages = messages;
        this.tcp = getClientSocket(tcpFactory, port);

        dispatcher.subscribe(this);
    }

    @Override
	public void addConnectionListener(SoupConnectionListener listener) {
        connectionListeners.add(listener);
    }

    protected abstract TCPClientSocket getClientSocket(TCPSocketFactory factory, int port);
    protected abstract void sendHeartbeat();

    @Override
    public void close() {
        tcp.close();

        connected = false;
        loggedIn = false;
    }

    @Override
    public void closeAllClients() {
        tcp.closeClients();

        connected = false;
        loggedIn = false;
    }

    @Override
    public void onConnect(TCPClientSocket clientSocket) {
        connected = true;
        loggedIn = false;

        for (int i = 0; i < connectionListeners.size(); i++) {
            connectionListeners.get(i).onConnect();
        }

        // Once we connect we wait for a login
        // clients must heartbeat N seconds from now or we disconnect
        updateRecvTimer();

        clientSocket.enableRead(true);
    }

    @Override
    public void onDisconnect(TCPClientSocket clientSocket) {
        connected = false;
        loggedIn = false;

        for (int i = 0; i < connectionListeners.size(); i++) {
            connectionListeners.get(i).onDisconnect();
        }
    }

    @Override
    public boolean isLoggedIn() {
        return loggedIn;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    protected void dispatch(ByteBuffer buffer) {
        dispatcher.dispatch(buffer);
    }

    protected void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    //
    // Sending
    //

    protected void send(SoupCommonCommand cmd, boolean force) {
        int length = cmd.getLength();
        // msg length does not include the two bytes for the length
        cmd.setMsgLength((short) (length - Short.BYTES));
        ByteBuffer rawBuffer = cmd.getRawBuffer();
        rawBuffer.limit(rawBuffer.position() + length);

        if (loggedIn || force) {

            log.debug(log.log().add("TX ").add(cmd.toString()));


            tcp.write(rawBuffer);

            sentMsgs++;


            // we need to send a heartbeat 1 second from now if there are no new messages
            updateSendTimer();
        }
    }

    // This is the client reading from the server
    @Override
    public void onReadAvailable(TCPClientSocket clientSocket) {
        //log.debug(log.log().add("Client read available"));
        doRead(clientSocket);
    }

    protected void doRead(TCPClientSocket clientSocket) {
        if (clientSocket.read(readBuffer)) {
            readBuffer.flip();

            // keep going through messages until we're done
            while(clientSocket.canRead() && read(readBuffer)) {
                // the can canRead check is there because we will
                // disable read if we have a pending command
            }

            if (readBuffer.hasRemaining()) {
                // move bytes to the front of the buffer
                readBuffer.compact();
            }
            else {
                // reset the buffer
                // would compact do the same thing here?
                readBuffer.clear();
            }
        }
    }

    private boolean read(ByteBuffer buffer) {
        // the message is at least 2 bytes for the length
        if (Short.BYTES > buffer.remaining()) {
            return false;
        }

        // get the length
        short length = buffer.getShort(buffer.position());

        // make sure we have enough in the buffer for the length
        if (length < 0 || length + Short.BYTES > buffer.remaining()) {
            return false;
        }

        // create our own slice
        int oldPosition = buffer.position();
        int oldLimit = buffer.limit();

        buffer.position(oldPosition);
        buffer.limit(oldPosition + Short.BYTES + length);

        // dispatch the message
        dispatch(buffer);

        // undo the slice
        buffer.limit(oldLimit);
        buffer.position(oldPosition + Short.BYTES + length);

        recvMsgs++;
        return true;
    }

    @Override
    public void onWriteUnavailable(TCPClientSocket clientSocket) {
    }

    //
    // Heartbeat Management
    //

    @Override
    public void onSoupBeforeListener(SoupCommonEvent msg) {
        if (log.isDebugEnabled()) {
            log.debug(log.log().add("RX ").add(msg.toString()));
            log.debug(log.log().add("Recevied :").addAsHex(msg.getRawBuffer()));
        }

        updateRecvTimer();
    }

    protected void updateRecvTimer() {
        timers.cancelTimer(recvTimer);
        recvTimer = timers.scheduleTimer(RECV_HEARTBEAT_TIMEOUT, recvTimerHandler);
    }

    protected void updateSendTimer() {
        timers.cancelTimer(sendTimer);
        sendTimer = timers.scheduleTimer(SEND_HEARTBEAT_TIMEOUT, sendTimerHandler);
    }

    private class HeartbeatSendTimer implements TimerHandler {
		@Override
        public void onTimer(int internalTimerID, int referenceData) {
            // you don't want to send a heartbeat before a login message
            // although this should never really happen
            if (isConnected() && isLoggedIn()) {
                sendHeartbeat();
            }
        }
    }

    private class HeartbeatRecvTimer implements TimerHandler {
		@Override
        public void onTimer(int internalTimerID, int referenceData) {
            if (isConnected()) {
                log.error(log.log().add("SOUP Timeout. Have not received a heartbeat in ").add(RECV_HEARTBEAT_TIMEOUT / TimeUtils.NANOS_PER_SECOND).add(" seconds"));
                closeAllClients();
            }
        }
    }

    //
    // Debug
    //

    @Override
    public void sendDebug(String text) {
        temp.clear();
        BinaryUtils.copy(temp, text);
        temp.flip();

        SoupDebugCommand cmd = messages.getSoupDebugCommand();
        cmd.setText(temp);
        send(cmd, true);
    }

    @Override
    public void onSoupDebug(SoupDebugEvent msg) {
        log.info(log.log().add("SOUP DEBUG RX: ").add(msg.getText()));
    }


    @Override
    public ByteStringBuffer status() {
        status.clear();
        status.add("Connected: ").add(isConnected()).addNL();
        status.add("Logged In: ").add(isLoggedIn()).addNL();
        status.add("Msgs Sent: ").add(sentMsgs).addNL();
        status.add("Msgs Recv: ").add(recvMsgs).addNL();
        return status;
    }
}
