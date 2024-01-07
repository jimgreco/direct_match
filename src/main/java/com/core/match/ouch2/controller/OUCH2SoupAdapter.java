package com.core.match.ouch2.controller;

import com.core.connector.Dispatcher;
import com.core.connector.soup.SoupBinTCPServerConnector;
import com.core.connector.soup.SoupConnectionListener;
import com.core.connector.soup.msgs.*;
import com.core.match.ouch2.msgs.*;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.store.FileIndexedStore;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimerService;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by hli on 4/1/16.
 */
public class OUCH2SoupAdapter implements
        OUCH2Adaptor,
        SoupSequencedDataListener,
        SoupUnsequencedDataListener {

    private static final String MESSAGE = ""; //TODO
    private final SoupBinTCPServerConnector soupConnector;
    private final OUCH2ByteBufferMessages ouchMessages;
    private final OUCH2ByteBufferDispatcher dispatcher;
    protected ByteBuffer buffer;

    public OUCH2SoupAdapter(String name,
                            Log log,
                            FileFactory fileFactory,
                            TCPSocketFactory tcpFactory,
                            TimerService timers,
                            int port,
                            String username,
                            String password) throws IOException {
        FileIndexedStore store = new FileIndexedStore(log, fileFactory, name);
        SoupByteBufferMessages soupMessages = new SoupByteBufferMessages();
        SoupByteBufferDispatcher soupDispatcher = new SoupByteBufferDispatcher(soupMessages);
        this.soupConnector = new SoupBinTCPServerConnector(log, tcpFactory, timers, store, port, soupDispatcher, soupMessages, username, password);
        this.ouchMessages = new OUCH2ByteBufferMessages();
        this.dispatcher = new OUCH2ByteBufferDispatcher(ouchMessages);
        log.debug(log.log().add("OUCH 2.0 Port configured with Username:").add(username));
        soupDispatcher.subscribe(this);
    }


    @Override
    public void open() throws IOException {
        soupConnector.open();

    }

    @Override
    public void close() throws IOException {
        soupConnector.close();
    }

    @Override
    public void setSession(String session) {
        soupConnector.setSession(session);
    }

    @Override
    public void send(OUCH2CommonCommand cmd) {
        ByteBuffer buffer = cmd.getRawBuffer();
        buffer.position(buffer.position() + cmd.getLength());
        soupConnector.sendMessage();
    }

    @Override
    public void enableRead(boolean enableRead) {
        this.soupConnector.enableRead(enableRead);
    }

    @Override
    public Dispatcher getOUCHDispatcher() {
        return dispatcher;
    }

    @Override
    public OUCH2AcceptedCommand getOUCHAccepted() {
        buffer = soupConnector.getMessageBuffer();
        return ouchMessages.getOUCH2AcceptedCommand(buffer);
    }

    @Override
    public OUCH2RejectedCommand getOUCHRejected() {
        buffer = soupConnector.getMessageBuffer();
        return ouchMessages.getOUCH2RejectedCommand(buffer);
    }

    @Override
    public OUCH2CanceledCommand getOUCHCanceled() {
        buffer = soupConnector.getMessageBuffer();
        return ouchMessages.getOUCH2CanceledCommand(buffer);
    }

    @Override
    public OUCH2ReplacedCommand getOUCHReplaced() {
        buffer = soupConnector.getMessageBuffer();
        return ouchMessages.getOUCH2ReplacedCommand(buffer);
    }

    @Override
    public OUCH2FillCommand getOUCHFillCommand() {
        buffer = soupConnector.getMessageBuffer();
        return ouchMessages.getOUCH2FillCommand(buffer);
    }

    @Override
    public OUCH2CancelRejectedCommand getOUCHCancelRejected() {
        buffer = soupConnector.getMessageBuffer();
        return ouchMessages.getOUCH2CancelRejectedCommand(buffer);
    }

    @Override
    public OUCH2TradeConfirmationCommand getOUCHTradeConfirmationCommand() {
        buffer = soupConnector.getMessageBuffer();
        return ouchMessages.getOUCH2TradeConfirmationCommand(buffer);
    }

    @Override
    public void addConnectionListener(SoupConnectionListener listener) {
        soupConnector.addConnectionListener(listener);
    }

    @Override
    public boolean isConnected() {
        return soupConnector.isConnected();
    }

    @Override
    public void sendSessionPassiveDebug(long clOrId) {
        soupConnector.sendDebugMessage(MESSAGE+clOrId);

    }

    @Override
    public void sendDebugMessage(String message) {
        soupConnector.sendDebugMessage(message);
    }

    @Override
    public void closeAllClients() {
        soupConnector.closeAllClients();
    }

    @Override
    public Boolean isLoggedIn() {
        return soupConnector.isLoggedIn();
    }

    @Override
    public void onSoupSequencedData(SoupSequencedDataEvent msg) {
        dispatcher.dispatch(msg.getMessage());
    }

    @Override
    public void onSoupUnsequencedData(SoupUnsequencedDataEvent msg) {
        dispatcher.dispatch(msg.getMessage());
    }
}
