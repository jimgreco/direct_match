package com.core.connector.soup;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.core.connector.ByteBufferDispatcher;
import com.core.connector.soup.msgs.SoupByteBufferDispatcher;
import com.core.connector.soup.msgs.SoupByteBufferMessages;
import com.core.connector.soup.msgs.SoupLoginAcceptedEvent;
import com.core.connector.soup.msgs.SoupLoginAcceptedListener;
import com.core.connector.soup.msgs.SoupLoginEventListener;
import com.core.connector.soup.msgs.SoupLoginRejectedEvent;
import com.core.connector.soup.msgs.SoupLoginRejectedListener;
import com.core.connector.soup.msgs.SoupSequencedDataEvent;
import com.core.connector.soup.msgs.SoupSequencedDataListener;
import com.core.connector.soup.msgs.SoupUnsequencedDataCommand;
import com.core.util.log.Log;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimerService;

/**
 * Created by jgreco on 7/1/15.
 */
public class SoupBinTCPClientAdapter implements SoupSequencedDataListener, SoupLoginAcceptedListener, SoupLoginRejectedListener
{
    private final SoupBinTCPClientConnector soupConnector;
    private final ByteBufferDispatcher dispatcher;
    private final Log log;
    private final SoupByteBufferMessages soupMessages;
	private SoupLoginEventListener parentListener;

    public SoupBinTCPClientAdapter(Log log,
                                   TCPSocketFactory tcpFactory,
                                   TimerService timers,
                                   String host,
                                   int port,
                                   ByteBufferDispatcher dispatcher,
                                   boolean ignoreHeartbeats,
                                   String username,
                                   String password) throws IOException {
        this.log = log;

        soupMessages = new SoupByteBufferMessages();
        SoupByteBufferDispatcher soupDispatcher = new SoupByteBufferDispatcher(soupMessages);
        soupDispatcher.subscribe(this);
        this.soupConnector = new SoupBinTCPClientConnector(log, tcpFactory, timers, soupMessages, soupDispatcher, host, port, username, password);
        soupDispatcher.subscribe(this);

        this.dispatcher = dispatcher;
    }

    public void subscribe(Object object) {
        dispatcher.subscribe(object);
    } 	

    public void open() throws IOException {
        this.soupConnector.open();
    }

    public void logOut(){
        soupConnector.sendLogoutMessage();
    }
    
    public void close() {
    	this.soupConnector.close();
    }



    @Override
    public void onSoupSequencedData(SoupSequencedDataEvent msg) {
        ByteBuffer bytes = msg.getMessage();
        if (bytes.remaining() < 1) {
            log.error(log.log().add("Received sequenced SOUP message with ").add(bytes.remaining()).add(" bytes"));
            return;
        }

        dispatcher.dispatch(bytes);
    }

    public void send(ByteBuffer buffer) {
        SoupUnsequencedDataCommand cmd = soupMessages.getSoupUnsequencedDataCommand();
        cmd.setMessage(buffer);
        soupConnector.send(cmd);
    }

    @Override
    public String toString() {
        return soupConnector.status().toString();
    }

    public void setLoginListener( SoupLoginEventListener listener )
    {
    	this.parentListener = listener;
    }

	@Override
	public void onSoupLoginAccepted(SoupLoginAcceptedEvent msg)
	{
		this.parentListener.onSoupLoginAccepted(msg);
	}
	
	@Override
	public void onSoupLoginRejected(SoupLoginRejectedEvent msg) {
		this.parentListener.onSoupLoginRejected(msg);
	}

    public void setLastSeqNum(int lastSeqNum) {
        soupConnector.setLastSeqNum(lastSeqNum);
    }
    
    public boolean canSendMessages() {
    	return soupConnector.isLoggedIn() && soupConnector.isConnected();
    }
}
