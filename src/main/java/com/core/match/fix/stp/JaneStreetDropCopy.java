package com.core.match.fix.stp;

import com.core.app.AppConstructor;
import com.core.app.Param;
import com.core.connector.Connector;
import com.core.connector.Dispatcher;
import com.core.match.MatchCommandSender;
import com.core.match.fix.orders.FIXOrderOutput;
import com.core.match.msgs.MatchMessages;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.book.MatchBBOBookService;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerService;

import java.io.IOException;

/**
 * Created by jgreco on 2/27/16.
 */
public class JaneStreetDropCopy extends DropCopy {
    @AppConstructor
    public JaneStreetDropCopy(Log log,
                              TCPSocketFactory socketFactory,
                              FileFactory fileFactory,
                              TimerService timeFactory,
                              TimeSource timeSource,
                              TraderService<Trader> traders,
                              SecurityService<BaseSecurity> securityService,
                              AccountService<Account> accountService,
                              Dispatcher dispatcher,
                              MatchCommandSender sender,
                              MatchMessages messages,
                              FixServerTcpConnectorFactory fixServerTcpConnectorFactory,
                              MatchBBOBookService matchBBOBookService,
                              Connector connector,
                              @Param(name = "Port") int port,
                              @Param(name = "SenderCompId") String senderCompID,
                              @Param(name = "TargetCompId") String targetCompID,
                              @Param(name = "Accounts") String accounts) throws IOException {
        super(log,
                socketFactory,
                fileFactory,
                timeFactory,
                timeSource,
                traders,
                securityService,
                accountService,
                dispatcher,
                sender,
                messages,
                fixServerTcpConnectorFactory,
                connector,
                matchBBOBookService,
                port,
                senderCompID,
                targetCompID,
                accounts);
    }

    @Override
    protected FIXOrderOutput<FIXSTPOrder> getFIXOutput() {
        return new JaneStreetFIXOutput(parser, store, traders, accounts, securities);
    }
}
