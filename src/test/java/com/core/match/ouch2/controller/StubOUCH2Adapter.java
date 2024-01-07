package com.core.match.ouch2.controller;

import com.core.connector.Dispatcher;
import com.core.connector.soup.SoupConnectionListener;
import com.core.match.ouch.msgs.OUCHTestMessages;
import com.core.match.ouch2.controller.OUCH2Adaptor;
import com.core.match.ouch2.msgs.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by jgreco on 6/30/15.
 */
public class StubOUCH2Adapter implements OUCH2Adaptor {
    private final OUCH2TestMessages messages;
    private final Dispatcher dispatcher;
    private Object lastMessage;
    private final Queue<Object> queue = new LinkedList<Object>();
	private boolean connected;
	Set<Long> listOfDebugMsgSent =new HashSet<>();

	public StubOUCH2Adapter(OUCH2TestMessages messages, Dispatcher dispatcher) {
        this.messages = messages;
        this.dispatcher = dispatcher;
    }

    @Override
    public Dispatcher getOUCHDispatcher() {
        return dispatcher;
    }

    @Override
    public OUCH2AcceptedCommand getOUCHAccepted() {
        lastMessage = messages.getOUCH2AcceptedCommand();
        return (OUCH2AcceptedCommand) lastMessage;
    }

    @Override
    public void open() throws IOException {
		connected=true;

    }

    @Override
    public void close() throws IOException {
		connected=false;
    }

    @Override
    public void setSession(String session) {

    }

    @Override
    public void send(OUCH2CommonCommand command) {
    	this.queue.add( lastMessage );
    }

	@Override
	public OUCH2RejectedCommand getOUCHRejected()
	{
		lastMessage = messages.getOUCH2RejectedCommand();
        return ( OUCH2RejectedCommand ) lastMessage;
	}

	@Override
	public OUCH2CanceledCommand getOUCHCanceled()
	{
		lastMessage = messages.getOUCH2CanceledCommand();
        return ( OUCH2CanceledCommand) lastMessage;
	}

	@Override
	public OUCH2ReplacedCommand getOUCHReplaced()
	{
		lastMessage = messages.getOUCH2ReplacedCommand();
        return (OUCH2ReplacedCommand) lastMessage;
	}

	@Override
	public OUCH2FillCommand getOUCHFillCommand()
	{
		lastMessage = messages.getOUCH2FillCommand();
        return ( OUCH2FillCommand) lastMessage;
	}
	
	@Override
	public OUCH2TradeConfirmationCommand getOUCHTradeConfirmationCommand()
	{
		lastMessage = messages.getOUCH2TradeConfirmationCommand();
		return ( OUCH2TradeConfirmationCommand ) lastMessage;
	}
	
    @Override
    public void addConnectionListener(SoupConnectionListener listener) {

    }

    public Queue<Object> getQueue()
	{
		return this.queue;
	}

	@Override
	public void enableRead(boolean enableRead)
	{
		return;
	}

	public void setConnected( boolean connected )
	{
		this.connected = connected;
	}
	
	@Override
	public boolean isConnected()
	{
		return connected;
	}

	@Override
	public void sendSessionPassiveDebug(long clOrId) {
		listOfDebugMsgSent.add(clOrId);
	}

	@Override
	public void closeAllClients() {
		connected=false;

	}

	@Override
	public Boolean isLoggedIn() {
		return true;
	}


	@Override
	public OUCH2CancelRejectedCommand getOUCHCancelRejected()
	{
		lastMessage = messages.getOUCH2CancelRejectedCommand();
        return (OUCH2CancelRejectedCommand) lastMessage;
	}

    @Override
    public void sendDebugMessage(String message) {
        // TODO Auto-generated method stub
        
    }
}
