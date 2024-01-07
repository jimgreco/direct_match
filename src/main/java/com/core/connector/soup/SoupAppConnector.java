package com.core.connector.soup;

import com.core.app.Exposed;
import com.core.app.Param;
import com.core.app.heartbeats.HeartBeatFieldIDEnum;
import com.core.app.heartbeats.HeartbeatBooleanField;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.app.heartbeats.HeartbeatNumberField;
import com.core.app.heartbeats.HeartbeatSource;
import com.core.app.heartbeats.HeartbeatStringField;
import com.core.connector.BaseConnector;
import com.core.connector.ByteBufferDispatcher;
import com.core.connector.Connector;
import com.core.connector.soup.msgs.SoupByteBufferDispatcher;
import com.core.connector.soup.msgs.SoupByteBufferMessages;
import com.core.connector.soup.msgs.SoupLoginAcceptedEvent;
import com.core.connector.soup.msgs.SoupLoginAcceptedListener;
import com.core.connector.soup.msgs.SoupMessages;
import com.core.connector.soup.msgs.SoupSequencedDataEvent;
import com.core.connector.soup.msgs.SoupSequencedDataListener;
import com.core.util.ByteStringBuffer;
import com.core.util.log.Log;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimerService;

import java.io.IOException;

/**
 * Created by jgreco on 7/12/15.
 */
public class SoupAppConnector extends BaseConnector implements
        Connector,
        SoupLoginAcceptedListener,
        SoupSequencedDataListener,
        HeartbeatSource
{
    private final Log log;
    private final SoupBinTCPClientConnector connector;

    private HeartbeatStringField sessionStrMonitor;
    private HeartbeatNumberField seqNumMonitor;
    private HeartbeatBooleanField connectedMonitor;
    private HeartbeatBooleanField loggedInMonitor;

    public SoupAppConnector(Log log,
                            TCPSocketFactory tcpFactory,
                            TimerService timers,
                            String host,
                            int port,
                            boolean ignoreHeartbeats,
                            String username,
                            String password,
                            ByteBufferDispatcher dispatcher) throws IOException {
        super(dispatcher);

        this.log = log;

        SoupMessages soupMessages = new SoupByteBufferMessages();
        SoupByteBufferDispatcher soupDispatcher = new SoupByteBufferDispatcher(soupMessages);
        connector = new SoupBinTCPClientConnector(log, tcpFactory, timers, soupMessages, soupDispatcher, host, port, username, password);

        soupDispatcher.subscribe(this);
    }

    @Exposed(name = "open")
    @Override
    public void open() throws IOException {
        connector.open();
    }

    @Exposed(name = "close")
    @Override
    public void close() throws IOException {
        connector.close();
    }

    @Exposed(name="setDebug")
    public void setDebug(@Param(name="setDebug") boolean debug) {
        log.setDebug(debug);
    }

    @Override
    public long getCurrentSeq() {
        return connector.getSequence();
    }

    @Override
    public ByteStringBuffer status() {
        return connector.status();
    }

    @Override
    public String getSession() {
        return connector.getSession();
    }

    @Override
    public void onSoupSequencedData(SoupSequencedDataEvent msg) {
        dispatchMessage(msg.getMessage());
    }

    @Override
    public void onHeartbeatRegister(HeartbeatFieldRegister register) {
        sessionStrMonitor = register.addStringField("SOUP", HeartBeatFieldIDEnum.Session);
        seqNumMonitor = register.addNumberField("SOUP", HeartBeatFieldIDEnum.SeqNum);
        connectedMonitor = register.addBoolField("SOUP", HeartBeatFieldIDEnum.Connected);
        loggedInMonitor = register.addBoolField("SOUP", HeartBeatFieldIDEnum.LoggedIn);
    }

    @Override
    public void onHeartbeatUpdate(HeartbeatFieldUpdater register) {
        sessionStrMonitor.set(connector.getSession());
        seqNumMonitor.set(connector.getSequence());
        connectedMonitor.set(connector.isConnected() );
        loggedInMonitor.set(connector.isLoggedIn() );
    }

    public SoupClientConnector getClientConnector() {
        return connector;
    }

    @Override
    public void onSoupLoginAccepted(SoupLoginAcceptedEvent msg) {
        dispatchSession(connector.getSession());
    }
}
