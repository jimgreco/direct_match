package com.core.app.heartbeats.collector;

import com.core.app.heartbeats.HeartBeatFieldIDEnum;

import java.util.Map;

/**
 * Created by hli on 12/2/15.
 */
public class Login {
    private String core;
    private String app;
    private String trader;
    private String acct;
    private String port;

    public String getAcct() {
        return acct;
    }

    public String getSessState() {
        return sessState;
    }

    public String getPort() {
        return port;
    }

    private String sessState;


    private String loggedIn;
    private String connected;


    public Login(Map<String, String> result){
        onUpdate(result);

    }

    public String getCore() {
        return core;
    }
    public String getLoggedIn() {
        return loggedIn;
    }

    public String getApp() {
        return app;
    }


    public String getTrader() {
        return trader;
    }




    public void onUpdate(Map<String, String> result) {
        core=result.get(HeartBeatFieldIDEnum.CORE.name());
        app=result.get(HeartBeatFieldIDEnum.App.name());
        acct=result.get(HeartBeatFieldIDEnum.Acct.name());
        trader=result.get(HeartBeatFieldIDEnum.TRADER.name());
        connected=result.get(HeartBeatFieldIDEnum.Connected.name());
        loggedIn=result.get(HeartBeatFieldIDEnum.LoggedIn.name());



    }

    public String getConnected() {
        return connected;
    }

}
