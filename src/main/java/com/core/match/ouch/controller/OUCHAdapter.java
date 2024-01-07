package com.core.match.ouch.controller;

import com.core.connector.Dispatcher;
import com.core.connector.soup.SoupConnectionListener;
import com.core.match.ouch.msgs.OUCHAcceptedCommand;
import com.core.match.ouch.msgs.OUCHCancelRejectedCommand;
import com.core.match.ouch.msgs.OUCHCanceledCommand;
import com.core.match.ouch.msgs.OUCHCommonCommand;
import com.core.match.ouch.msgs.OUCHFillCommand;
import com.core.match.ouch.msgs.OUCHRejectedCommand;
import com.core.match.ouch.msgs.OUCHReplacedCommand;
import com.core.match.ouch.msgs.OUCHTradeConfirmationCommand;

import java.io.IOException;

/**
 * Created by jgreco on 6/30/15.
 */
public interface OUCHAdapter {
	void open() throws IOException;
	void close() throws IOException;
	void setSession(String session);
	void send(OUCHCommonCommand cmd);
	void enableRead( boolean enableRead);
	
    Dispatcher getOUCHDispatcher();
	OUCHAcceptedCommand getOUCHAccepted();
	OUCHRejectedCommand getOUCHRejected();
	OUCHCanceledCommand getOUCHCanceled();
	OUCHReplacedCommand getOUCHReplaced();
	OUCHFillCommand getOUCHFillCommand();
	OUCHCancelRejectedCommand getOUCHCancelRejected();
	OUCHTradeConfirmationCommand getOUCHTradeConfirmationCommand();

	void addConnectionListener(SoupConnectionListener listener);

	boolean isConnected();

	void sendSessionPassiveDebug(long clOrId);

	void closeAllClients();

	Boolean isLoggedIn();
}
