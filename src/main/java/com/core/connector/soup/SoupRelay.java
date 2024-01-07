package com.core.connector.soup;

import com.core.app.AppConstructor;
import com.core.app.CommandException;
import com.core.app.Param;
import com.core.app.UniversalApplication;
import com.core.app.heartbeats.HeartBeatFieldIDEnum;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.app.heartbeats.HeartbeatNumberField;
import com.core.connector.AfterMessageListener;
import com.core.connector.CommandSender;
import com.core.connector.Connector;
import com.core.connector.soup.msgs.SoupByteBufferDispatcher;
import com.core.connector.soup.msgs.SoupByteBufferMessages;
import com.core.connector.soup.msgs.SoupSequencedDataEvent;
import com.core.connector.soup.msgs.SoupSequencedDataListener;
import com.core.util.BinaryUtils;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.store.FileIndexedStore;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimerService;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by jgreco on 7/12/15.
 */
public class SoupRelay extends UniversalApplication implements
        AfterMessageListener,
        SoupSequencedDataListener
{
    final SoupBinTCPServerConnector soupConnector;
    private final int port;

    private HeartbeatNumberField connectedHeartbeat;
    private HeartbeatNumberField loggedInHeartbeat;
    private HeartbeatNumberField sequenceHeartbeat;
    private HeartbeatNumberField clientSequenceHeartbeat;

    @AppConstructor
    public SoupRelay(
            Log log,
            FileFactory fileFactory,
            TCPSocketFactory tcpSocketFactory,
            TimerService timers,
            Connector connector,
            CommandSender sender,
            @Param(name = "Name") String name,
            @Param(name = "Port") int port,
            @Param(name = "Username") String username,
            @Param(name = "Password") String password) throws IOException {
        super(log, sender);

        FileIndexedStore indexedFile = new FileIndexedStore(log, fileFactory, name);
        SoupByteBufferMessages messages = new SoupByteBufferMessages();
        SoupByteBufferDispatcher soupDispatcher = new SoupByteBufferDispatcher(messages);
        this.soupConnector = new SoupBinTCPServerConnector(log, tcpSocketFactory, timers, indexedFile, port, soupDispatcher, messages, username, password);

        this.port = port;

        connector.addAfterListener(this);
        connector.addSessionSourceListener(soupConnector::setSession);
    }

    @Override
    protected void onActive() {
        try {
            soupConnector.open();
        } catch (IOException e) {
            throw new CommandException(e);
        }
    }

    @Override
    protected void onPassive() {
        soupConnector.close();
    }

    @Override
    public void onAfterMessage(ByteBuffer message) {
        ByteBuffer buf = soupConnector.getMessageBuffer();
        BinaryUtils.copy(buf, message);
        soupConnector.sendMessage();
    }

    @Override
    public void onAddHeartbeatFields(HeartbeatFieldRegister fieldRegister) {
        fieldRegister.addNumberField("SOUP", HeartBeatFieldIDEnum.Port).set(port);
        connectedHeartbeat = fieldRegister.addNumberField("SOUP", HeartBeatFieldIDEnum.Connected);
        loggedInHeartbeat = fieldRegister.addNumberField("SOUP", HeartBeatFieldIDEnum.LoggedIn);
        sequenceHeartbeat = fieldRegister.addNumberField("SOUP", HeartBeatFieldIDEnum.SeqNum);
        clientSequenceHeartbeat = fieldRegister.addNumberField("SOUP", HeartBeatFieldIDEnum.ClientSequence);
    }

    @Override
    public void onUpdateHeartbeatFields(HeartbeatFieldUpdater fieldUpdater) {
        connectedHeartbeat.set(soupConnector.isConnected() ? 1 : 0);
        loggedInHeartbeat.set(soupConnector.isLoggedIn() ? 1 : 0);
        sequenceHeartbeat.set(soupConnector.getSequence());
        clientSequenceHeartbeat.set(soupConnector.getNextExpectedSequence());
    }

    @Override
    public void onSoupSequencedData(SoupSequencedDataEvent msg) {
        send(msg.getMessage());
    }
}
