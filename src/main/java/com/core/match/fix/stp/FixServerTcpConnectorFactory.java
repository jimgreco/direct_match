package com.core.match.fix.stp;

import com.core.connector.CommandSender;
import com.core.fix.connector.FixConnector;
import com.core.fix.connector.FixServerTcpConnector;
import com.core.util.log.Log;
import com.core.util.tcp.TCPSocketFactory;

/**
 * Created by hli on 1/17/16.
 */
public class FixServerTcpConnectorFactory {
    public FixConnector create( Log log,
                                        TCPSocketFactory socketFactory,
                                        CommandSender sender,
                                        int port){
       return  new FixServerTcpConnector(
               log,
               socketFactory,
               sender,
               port);
    }

}
