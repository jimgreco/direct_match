package com.core.match;

import com.core.connector.AllCommandsClearedListener;
import com.core.connector.ContributorDefinedListener;
import com.core.match.msgs.MatchBaseDispatcher;
import com.core.match.msgs.MatchCommonCommand;
import com.core.match.msgs.MatchCommonEvent;
import com.core.match.msgs.MatchContributorEvent;
import com.core.match.msgs.MatchOrderCommand;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchTestDispatcher;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * User: jgreco
 */
public class StubMatchCommandSender implements MatchCommandSender, MatchBaseDispatcher.MatchAfterListener {
    private int nextSeqNum = 1;
    private int nextOrderId = 1;
    private boolean canWrite = true;
    private short contribID;

    private final String name;
    private final MatchTestDispatcher dispatcher;
    private final List<MatchCommonCommand> sent = new LinkedList<>();
    private final IntObjectHashMap<MatchOrderEvent> orderMessages = new IntObjectHashMap<MatchOrderEvent>();
	
    private boolean dontDispatch;
	private Queue<MatchCommonCommand> disconnectQueue = new LinkedList<>();
	private Queue<MatchCommonCommand> queue = new LinkedList<>();
    private List<AllCommandsClearedListener> listeners = new FastList<>();
    private boolean dispatchAllCommandsCleared = true;
    private boolean active=false;

    public StubMatchCommandSender(String name, short contribID, MatchTestDispatcher dispatcher) {
        this.name = name;
        this.contribID = contribID;
        this.dispatcher = dispatcher;
        this.dispatcher.subscribe(this);
    }
    
    public void setDontDispatch( boolean dontDispatch) 
    {
    	this.dontDispatch = dontDispatch; 
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }

    public int getNextSeqNum() {
        return nextSeqNum;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public short getContribID() {
        return contribID;
    }

    @Override
    public void setContribID(short contributorID) {
        this.contribID = contributorID;
    }

    @Override
    public String getDestinationAddress() {
        return "TargetHost";
    }

    @Override
    public String getSession() {
        return "SESSION";
    }

    @Override
    public int getLastSeenContribSeqNum() {
        return getContribSeqNum();
    }

    @Override
    public int getContribSeqNum() {
        return nextSeqNum - 1;
    }

    @Override
    public int incContribSeqNum() {
        return 0;
    }

    @Override
    public boolean canSend() {
        return canWrite() && isActive() && isCaughtUp();
    }

    @Override
    public boolean isActive() {

        return active;
    }

    @Override
    public boolean isCaughtUp() {
        return true;
    }

    @Override
    public boolean canWrite() {
        return canWrite;
    }

    @Override
    public void setActive() {
        active=true;

    }

    @Override
    public void setPassive() {
        active=false;


    }

    public MatchOrderEvent getOrderMsg(int id) {
        return orderMessages.get(id);
    }

    public void setDispatchAllCommandsCleared(boolean dispatchAllCommandsCleared) {
        this.dispatchAllCommandsCleared = dispatchAllCommandsCleared;
    }

    @Override
	public boolean send(MatchCommonCommand command) {
        MatchCommonEvent e1 = (MatchCommonEvent) command;
        if (e1.getMsgType() == 'O') {
            MatchOrderCommand orderCommand = (MatchOrderCommand) command;
            orderCommand.setOrderID(nextOrderId);
            orderMessages.put(nextOrderId, orderCommand.toEvent());
            nextOrderId++;
        }

        command.setContributorID(contribID);
        command.setContributorSeq(nextSeqNum++);
        sent.add(command);

        System.out.println(command.toString());
        
        if( !this.dontDispatch )
    	{
    		dispatcher.dispatch(command);

            if (dispatchAllCommandsCleared) {
                for (AllCommandsClearedListener listener : listeners) {
                    listener.onAllCommandsCleared();
                }
            }
    	}
        else
        {
        	disconnectQueue.add(command);
        }
        
        return true;
    }
    
    public int queueSize()
    {
    	return queue.size();
    }
    public int senderSize()
    {
        return sent.size();
    }
    public Object pollDisconnectQueue()
    {
    	return this.disconnectQueue.poll();
    }
    public void dequeue()
	{
    	dispatcher.dispatch( disconnectQueue.poll() );
	}

    @Override
    public void init() {

    }

    @Override
    public void onMyMessage(int contributorSeq) {
    	if( dispatchAllCommandsCleared)
    	{
	    	for( AllCommandsClearedListener listener: listeners )
	    	{
	    		listener.onAllCommandsCleared();
	    	}
    	}
    }

    @Override
	public void add(MatchCommonCommand command) {
        command.setContributorID(contribID);
        command.setContributorSeq(nextSeqNum++);
        queue.add(command);
    }

    @Override
    public void add(ByteBuffer buffer) {
        throw new RuntimeException("Not implemented");
    }
    
    @Override
    public boolean send() {
        queue.forEach(this::send);
        queue.clear();
        return true;
    }

    @Override
    public void addAllCommandsClearedListener(AllCommandsClearedListener listener) {
        listeners.add(listener);
    }

    @Override
    public void addContributorDefinedListener(ContributorDefinedListener listener) {
        listener.onContributorDefined(contribID, name);
    }

    public boolean hasMessages() {
        return sent.size() > 0;
    }

    @SuppressWarnings("unchecked")
	public <T> T getMessage(@SuppressWarnings("unused") Class<T> cls) {
        return (T)sent.remove(0);
    }

    @Override
    public void onMatchContributor(MatchContributorEvent msg) {
        contribID = msg.getSourceContributorID();
    }

	@Override
	public void onMatchAfterListener(MatchCommonEvent msg)
	{
		this.onMyMessage(msg.getContributorSeq());
	}
}
