package com.core.match.itch;

import com.core.app.AppConstructor;
import com.core.app.CommandException;
import com.core.app.Param;
import com.core.app.heartbeats.HeartBeatFieldIDEnum;
import com.core.app.heartbeats.HeartbeatBooleanField;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.app.heartbeats.HeartbeatNumberField;
import com.core.app.heartbeats.HeartbeatStringField;
import com.core.connector.Connector;
import com.core.connector.Dispatcher;
import com.core.connector.soup.SoupBinTCPServerConnector;
import com.core.connector.soup.msgs.SoupByteBufferDispatcher;
import com.core.connector.soup.msgs.SoupByteBufferMessages;
import com.core.match.MatchApplication;
import com.core.match.itch.msgs.ITCHByteBufferMessages;
import com.core.match.itch.msgs.ITCHCommonCommand;
import com.core.match.msgs.MatchConstants;
import com.core.match.services.events.SystemEventService;
import com.core.match.services.order.DisplayedOrder;
import com.core.match.services.order.DisplayedOrderService;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.SecurityService;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.store.FileIndexedStore;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimerService;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by jgreco on 7/1/15.
 */
public class ITCHSoupServer extends MatchApplication implements
        ITCHServerListener
{
    private final SoupBinTCPServerConnector connector;
    private final int port;
    private ByteBuffer buffer;
    private HeartbeatBooleanField connectedHeartbeat;
    private HeartbeatBooleanField loggedInHeartbeat;
    private HeartbeatNumberField sequenceHeartbeat;
    private HeartbeatNumberField clientSequenceHeartbeat;
    private HeartbeatNumberField portField;
    private HeartbeatStringField sessionStateField;

    @SuppressWarnings("unused")
	@AppConstructor
    public ITCHSoupServer(
            Log log,
            Dispatcher dispatch,
            TCPSocketFactory tcpFactory,
            FileFactory fileFactory,
            TimerService timers,
            SecurityService<BaseSecurity> securities,
            SystemEventService systemEventService,
            Connector coreConnector,
            @Param(name = "Name") String name,
            @Param(name = "Port") int port,
            @Param(name = "Username") String username,
            @Param(name = "Password") String password) throws IOException {
        super(log);

        this.port = port;

        ITCHByteBufferMessages messages = new ITCHByteBufferMessages();

            DisplayedOrderService<DisplayedOrder> orders = new DisplayedOrderService<>(DisplayedOrder.class, log, dispatch, MatchConstants.MAX_LIVE_ORDERS);
            new ITCHMessageService(messages, orders, securities, systemEventService, this);


        FileIndexedStore store = new FileIndexedStore(log, fileFactory, name);

        SoupByteBufferMessages soupMessages = new SoupByteBufferMessages();
        SoupByteBufferDispatcher soupDispatcher = new SoupByteBufferDispatcher(soupMessages);
        connector = new SoupBinTCPServerConnector(log, tcpFactory, timers, store, port, soupDispatcher, soupMessages, username, password);

        coreConnector.addSessionSourceListener(connector::setSession);
    }

    @Override
    protected void onActive() {
        try {
            connector.open();
        } catch (IOException e) {
            throw new CommandException("Could not open connector " + e);
        }
    }

    @Override
    protected void onPassive() {
        connector.close();
    }

    @Override
    public ByteBuffer getBuffer() {
        return buffer = connector.getMessageBuffer();
    }

    @Override
    public void onMessage(ITCHCommonCommand msg) {
        if(log.isDebugEnabled()){
            log.debug(log.log().add("Itch TX:").add(msg.toString()));
        }
        buffer.position(buffer.position() + msg.getLength());
        connector.sendMessage();
    }

    @Override
    public void onAddHeartbeatFields(HeartbeatFieldRegister fieldRegister) {
        portField=fieldRegister.addNumberField("SOUP", HeartBeatFieldIDEnum.Port);
        connectedHeartbeat = fieldRegister.addBoolField("SOUP", HeartBeatFieldIDEnum.Connected);
        loggedInHeartbeat = fieldRegister.addBoolField("SOUP", HeartBeatFieldIDEnum.LoggedIn);
        sequenceHeartbeat = fieldRegister.addNumberField("SOUP", HeartBeatFieldIDEnum.Sequence);
        clientSequenceHeartbeat = fieldRegister.addNumberField("SOUP", HeartBeatFieldIDEnum.ClientSequence);
        sessionStateField=fieldRegister.addStringField("SOUP",HeartBeatFieldIDEnum.SessionState);
    }

    @Override
    public void onUpdateHeartbeatFields(HeartbeatFieldUpdater fieldUpdater) {
        portField.set(port);
        connectedHeartbeat.set(connector.isConnected());
        loggedInHeartbeat.set(connector.isLoggedIn() );
        sequenceHeartbeat.set(connector.getSequence());
        clientSequenceHeartbeat.set(connector.getNextExpectedSequence());
        sessionStateField.set(connector.getSessionState());
    }
}
