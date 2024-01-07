package com.core.match.fix.orders;

import com.core.app.AppConstructor;
import com.core.app.Param;
import com.core.connector.Connector;
import com.core.connector.Dispatcher;
import com.core.fix.*;
import com.core.fix.connector.FixConnector;
import com.core.fix.connector.FixServerTcpConnector;
import com.core.fix.msgs.FixDispatcher;
import com.core.fix.store.FixFileStore;
import com.core.fix.store.FixStore;
import com.core.match.MatchCommandSender;
import com.core.match.msgs.MatchMessages;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.contributor.Contributor;
import com.core.match.services.contributor.ContributorService;
import com.core.match.services.events.SystemEventService;
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
 * Created by jgreco on 2/16/16.
 */
public class GX2FixOrderEntry extends FixOrderEntry {
    @AppConstructor
    public GX2FixOrderEntry(Log log,
                            TCPSocketFactory socketFactory,
                            FileFactory fileFactory,
                            TimerService timeFactory,
                            TraderService<Trader> traderService,
                            AccountService<Account> accountService,
                            SecurityService<BaseSecurity> securityService,
                            SystemEventService systemEventService,
                            ContributorService<Contributor> contributorService,
                            TimeSource timeSource,
                            Dispatcher dispatcher,
                            MatchCommandSender sender,
                            Connector connector,
                            MatchMessages msgs,
                            @Param(name="Port") int port,
                            @Param(name="SenderCompId") String senderCompID,
                            @Param(name="TargetCompId") String targetCompID,
                            @Param(name="Account") String accountName) throws IOException {
        super(log,
                dispatcher,
                timeFactory,
                traderService,
                accountService,
                securityService,
                systemEventService,
                contributorService,
                sender,
                connector,
                msgs,
                new FixServerTcpConnector(log, socketFactory, sender, port),
                new InlineFixParser(),
                new InlineFixWriter(timeSource, 4, targetCompID, senderCompID),
                new FixFileStore(fileFactory, log, sender.getName()),
                new FixDispatcher(log),
                senderCompID,
                targetCompID,
                accountName,
                FIXQtyMode.RoundLot);

        this.info = new FIXPortInfo(port, 4, senderCompID, targetCompID);
    }
}
