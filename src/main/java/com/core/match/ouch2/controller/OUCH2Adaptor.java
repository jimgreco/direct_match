package com.core.match.ouch2.controller;

import com.core.connector.Dispatcher;
import com.core.connector.soup.SoupConnectionListener;
import com.core.match.ouch2.msgs.*;

import java.io.IOException;

/**
 * Created by hli on 4/1/16.
 */
public interface OUCH2Adaptor {
    void open() throws IOException;
    void close() throws IOException;
    void setSession(String session);
    void send(OUCH2CommonCommand cmd);
    void enableRead( boolean enableRead);

    Dispatcher getOUCHDispatcher();
    OUCH2AcceptedCommand getOUCHAccepted();
    OUCH2RejectedCommand getOUCHRejected();
    OUCH2CanceledCommand getOUCHCanceled();
    OUCH2ReplacedCommand getOUCHReplaced();
    OUCH2FillCommand getOUCHFillCommand();
    OUCH2CancelRejectedCommand getOUCHCancelRejected();
    OUCH2TradeConfirmationCommand getOUCHTradeConfirmationCommand();

    void addConnectionListener(SoupConnectionListener listener);

    boolean isConnected();

    void sendSessionPassiveDebug(long clOrId);
    void sendDebugMessage(String message);

    void closeAllClients();

    Boolean isLoggedIn();
}
