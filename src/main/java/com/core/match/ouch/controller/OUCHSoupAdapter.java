package com.core.match.ouch.controller;

import com.core.connector.Dispatcher;
import com.core.connector.soup.SoupBinTCPServerConnector;
import com.core.connector.soup.SoupConnectionListener;
import com.core.connector.soup.SoupServerConnector;
import com.core.connector.soup.msgs.SoupByteBufferDispatcher;
import com.core.connector.soup.msgs.SoupByteBufferMessages;
import com.core.connector.soup.msgs.SoupSequencedDataEvent;
import com.core.connector.soup.msgs.SoupSequencedDataListener;
import com.core.connector.soup.msgs.SoupUnsequencedDataEvent;
import com.core.connector.soup.msgs.SoupUnsequencedDataListener;
import com.core.match.ouch.msgs.OUCHAcceptedCommand;
import com.core.match.ouch.msgs.OUCHByteBufferDispatcher;
import com.core.match.ouch.msgs.OUCHByteBufferMessages;
import com.core.match.ouch.msgs.OUCHCancelRejectedCommand;
import com.core.match.ouch.msgs.OUCHCanceledCommand;
import com.core.match.ouch.msgs.OUCHCommonCommand;
import com.core.match.ouch.msgs.OUCHFillCommand;
import com.core.match.ouch.msgs.OUCHRejectedCommand;
import com.core.match.ouch.msgs.OUCHReplacedCommand;
import com.core.match.ouch.msgs.OUCHTradeConfirmationCommand;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.store.FileIndexedStore;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimerService;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by jgreco on 6/29/15.
 */
public class OUCHSoupAdapter implements
        OUCHAdapter,
        SoupSequencedDataListener,
        SoupUnsequencedDataListener {
    private static final String DROPPED_MESSAGE_ERROR = "Session is Passive. Message ClOrId: ";
    private final SoupServerConnector soupConnector;
    private final OUCHByteBufferMessages ouchMessages;
    private final OUCHByteBufferDispatcher dispatcher;

    protected ByteBuffer buffer;

    public OUCHSoupAdapter(String name,
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
        soupConnector = new SoupBinTCPServerConnector(log, tcpFactory, timers, store, port, soupDispatcher, soupMessages, username, password);

        this.ouchMessages = new OUCHByteBufferMessages();
        this.dispatcher = new OUCHByteBufferDispatcher(ouchMessages);
        log.warn(log.log().add("Username is ").add(username));
        soupDispatcher.subscribe(this);
    }

    @Override
    public void onSoupSequencedData(SoupSequencedDataEvent msg) {
        dispatcher.dispatch(msg.getMessage());
    }

    @Override
    public void onSoupUnsequencedData(SoupUnsequencedDataEvent msg) {
        dispatcher.dispatch(msg.getMessage());
    }

    @Override
	public Dispatcher getOUCHDispatcher() {
        return dispatcher;
    }

    @Override
    public OUCHCancelRejectedCommand getOUCHCancelRejected() {
    	buffer = soupConnector.getMessageBuffer();
    	return ouchMessages.getOUCHCancelRejectedCommand(buffer);
    }
    
    @Override
    public OUCHRejectedCommand getOUCHRejected() {
    	buffer = soupConnector.getMessageBuffer();
    	return ouchMessages.getOUCHRejectedCommand(buffer);
    }

    @Override
    public OUCHAcceptedCommand getOUCHAccepted() {
        buffer = soupConnector.getMessageBuffer();
        return ouchMessages.getOUCHAcceptedCommand(buffer);
    }

    @Override
    public OUCHCanceledCommand getOUCHCanceled()
    {
    	buffer = soupConnector.getMessageBuffer();
    	return ouchMessages.getOUCHCanceledCommand(buffer);
    }
    
    @Override
    public OUCHReplacedCommand getOUCHReplaced()
    {
    	buffer = soupConnector.getMessageBuffer();
    	return ouchMessages.getOUCHReplacedCommand(buffer);
    }
    
    @Override
    public OUCHFillCommand getOUCHFillCommand()
    {
    	buffer = soupConnector.getMessageBuffer();
    	return ouchMessages.getOUCHFillCommand(buffer);
    }
    

	@Override
	public OUCHTradeConfirmationCommand getOUCHTradeConfirmationCommand()
	{
		buffer = soupConnector.getMessageBuffer(); 
		return ouchMessages.getOUCHTradeConfirmationCommand(buffer);
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
    public void open() throws IOException {
        soupConnector.open();
    }

    @Override
    public void close() throws IOException {
        soupConnector.close();
    }
    @Override
    public void closeAllClients(){
        soupConnector.closeAllClients();
    }

    @Override
    public Boolean isLoggedIn() {
        return soupConnector.isLoggedIn();
    }

    @Override
    public void setSession(String session) {
        soupConnector.setSession(session);
    }

    @Override
    public void send(OUCHCommonCommand cmd) {
        ByteBuffer buffer = cmd.getRawBuffer();
        buffer.position(buffer.position() + cmd.getLength());
        soupConnector.sendMessage();
    }
    @Override
    public void sendSessionPassiveDebug(long clOrId){
        soupConnector.sendDebugMessage(DROPPED_MESSAGE_ERROR+clOrId);
    }

    @Override
	public void enableRead(boolean enableRead) {
    	this.soupConnector.enableRead(enableRead);
    }
}
