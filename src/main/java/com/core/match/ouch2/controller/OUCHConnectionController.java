package com.core.match.ouch2.controller;

import com.core.connector.soup.SoupConnectionListener;
import com.core.match.msgs.MatchCancelCommand;
import com.core.match.ouch.OUCHOrder;
import com.core.util.log.Log;

import java.util.Iterator;

/**
 * Created by hli on 4/6/16.
 */
public class OUCHConnectionController implements SoupConnectionListener {
    private boolean connected;
    private final OUCH2Adaptor ouchAdapter;
    private final DisconnectCancelListener cancelListener;
    private final Log log;
    public OUCHConnectionController(OUCH2Adaptor ouchAdaptor, Log log, DisconnectCancelListener cancelListener){
        this.ouchAdapter=ouchAdaptor;
        this.cancelListener = cancelListener;
        this.log=log;
        ouchAdapter.addConnectionListener(this);

    }
    @Override
    public void onConnect() {
        connected=true;
    }


    @Override
    public void onDisconnect() {
        connected=false;

        log.error(log.log().add("Client disconnect.  Starting Cancel on Disconnect."));
        cancelOrdersIfDisconnected();
    }



    private void cancelOrdersIfDisconnected()
    {
        if (!ouchAdapter.isConnected() )
        {
            cancelListener.cancelAllLiveOrdersOnDisconnect();
        }
    }
}
