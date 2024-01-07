package com.core.match.ouch;

import com.core.connector.Dispatcher;
import com.core.connector.soup.SoupConnectionListener;
import com.core.match.ouch.controller.OUCHAdapter;
import com.core.match.ouch.msgs.OUCHAcceptedCommand;
import com.core.match.ouch.msgs.OUCHCancelRejectedCommand;
import com.core.match.ouch.msgs.OUCHCanceledCommand;
import com.core.match.ouch.msgs.OUCHCommonCommand;
import com.core.match.ouch.msgs.OUCHFillCommand;
import com.core.match.ouch.msgs.OUCHRejectedCommand;
import com.core.match.ouch.msgs.OUCHReplacedCommand;
import com.core.match.ouch.msgs.OUCHTestMessages;
import com.core.match.ouch.msgs.OUCHTradeConfirmationCommand;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by jgreco on 6/30/15.
 */
public class TestOUCHAdapter implements OUCHAdapter {
    private final OUCHTestMessages messages;
    private final Dispatcher dispatcher;
    private Object lastMessage;
    private final Queue<Object> queue = new LinkedList<Object>();
	private boolean connected;
	Set<Long> listOfDebugMsgSent =new HashSet<>();

	public TestOUCHAdapter(OUCHTestMessages messages, Dispatcher dispatcher) {
        this.messages = messages;
        this.dispatcher = dispatcher;
    }

    @Override
    public Dispatcher getOUCHDispatcher() {
        return dispatcher;
    }

    @Override
    public OUCHAcceptedCommand getOUCHAccepted() {
        lastMessage = messages.getOUCHAcceptedCommand();
        return ( OUCHAcceptedCommand ) lastMessage;
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
    public void send(OUCHCommonCommand command) {
    	this.queue.add( lastMessage );
    }

	@Override
	public OUCHRejectedCommand getOUCHRejected()
	{
		lastMessage = messages.getOUCHRejectedCommand();
        return ( OUCHRejectedCommand ) lastMessage;
	}

	@Override
	public OUCHCanceledCommand getOUCHCanceled()
	{
		lastMessage = messages.getOUCHCanceledCommand();
        return ( OUCHCanceledCommand) lastMessage;
	}

	@Override
	public OUCHReplacedCommand getOUCHReplaced()
	{
		lastMessage = messages.getOUCHReplacedCommand();
        return ( OUCHReplacedCommand) lastMessage;
	}

	@Override
	public OUCHFillCommand getOUCHFillCommand()
	{
		lastMessage = messages.getOUCHFillCommand();
        return ( OUCHFillCommand) lastMessage;
	}

	@Override
	public OUCHTradeConfirmationCommand getOUCHTradeConfirmationCommand()
	{
		lastMessage = messages.getOUCHTradeConfirmationCommand();
		return ( OUCHTradeConfirmationCommand ) lastMessage;
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
	public OUCHCancelRejectedCommand getOUCHCancelRejected()
	{
		lastMessage = messages.getOUCHCancelRejectedCommand();
        return ( OUCHCancelRejectedCommand ) lastMessage;
	}
}
