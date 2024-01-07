package com.core.match.ouch2.factories;

import com.core.match.ouch.controller.OUCHSoupAdapter;
import com.core.match.ouch2.controller.*;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimerService;

import java.io.IOException;

/**
 * Created by hli on 4/4/16.
 */
public class OUCHComponentFactory implements OUCHFactory {

    @Override
    public OUCH2Adaptor getOUCH2Adaptor(String name,
                                        Log log,
                                        FileFactory fileFactory,
                                        TCPSocketFactory tcpFactory,
                                        TimerService timers,
                                        int port,
                                        String username,
                                        String password) throws IOException {
        return new OUCH2SoupAdapter(name,
                log,
                fileFactory,
                tcpFactory,
                timers,
                port,
                username,
                password);
    }

    @Override
    public OUCHSoupAdapter getOUCHAdaptor(String name,
                                          Log log,
                                          FileFactory fileFactory,
                                          TCPSocketFactory tcpFactory,
                                          TimerService timers,
                                          int port,
                                          String username,
                                          String password) throws IOException {
        return new OUCHSoupAdapter(name,
                log,
                fileFactory,
                tcpFactory,
                timers,
                port,
                username,
                password);


    }

    public OUCHConnectionController getOUCHConnectionController(OUCH2Adaptor ouch2Adaptor, Log log, DisconnectCancelListener listener){
        return new OUCHConnectionController(ouch2Adaptor,log,listener);
    }
    public OUCHOrdersRepository getOUCHRepository(){
        return new OUCHOrdersRepository();
    }
}
