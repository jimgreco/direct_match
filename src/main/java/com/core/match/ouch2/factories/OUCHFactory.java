package com.core.match.ouch2.factories;

import com.core.match.ouch.controller.OUCHAdapter;
import com.core.match.ouch.controller.OUCHSoupAdapter;
import com.core.match.ouch2.controller.DisconnectCancelListener;
import com.core.match.ouch2.controller.OUCH2Adaptor;
import com.core.match.ouch2.controller.OUCHConnectionController;
import com.core.match.ouch2.controller.OUCHOrdersRepository;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimerService;

import java.io.IOException;

/**
 * Created by hli on 4/6/16.
 */
public interface OUCHFactory {
    OUCH2Adaptor getOUCH2Adaptor(String name,
                                 Log log,
                                 FileFactory fileFactory,
                                 TCPSocketFactory tcpFactory,
                                 TimerService timers,
                                 int port,
                                 String username,
                                 String password) throws IOException;

    OUCHAdapter getOUCHAdaptor(String name,
                               Log log,
                               FileFactory fileFactory,
                               TCPSocketFactory tcpFactory,
                               TimerService timers,
                               int port,
                               String username,
                               String password) throws IOException;

     OUCHConnectionController getOUCHConnectionController(OUCH2Adaptor ouch2Adaptor, Log log, DisconnectCancelListener listener);
     OUCHOrdersRepository getOUCHRepository();
}
