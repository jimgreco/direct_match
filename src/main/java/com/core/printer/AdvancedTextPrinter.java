package com.core.printer;

import com.core.app.AppConstructor;
import com.core.app.CommandException;
import com.core.app.Exposed;
import com.core.app.Param;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.connector.Connector;
import com.core.connector.Dispatcher;
import com.core.match.MatchApplication;
import com.core.match.msgs.MatchPrinter;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.contributor.Contributor;
import com.core.match.services.contributor.ContributorService;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.tcp.TCPDrop;
import com.core.util.tcp.TCPSocketFactory;

import java.io.IOException;

/**
 * Created by jgreco on 8/10/15.
 */
public class AdvancedTextPrinter extends MatchApplication {
    private final TCPDrop drop;
    private boolean noLog;

    @AppConstructor
    public AdvancedTextPrinter(Log log,
                               FileFactory fileFactory,
                               TCPSocketFactory tcpFactory,
                               Dispatcher dispatcher,
                               Connector connector,
                               ContributorService<Contributor> contributors,
                               SecurityService<BaseSecurity> securities,
                               TraderService<Trader> traders,
                               AccountService<Account> accounts,
                               @Param(name="Name") String name,
                               @Param(name="Type") String type,
                               @Param(name="Style") String style,
                               @Param(name="Port") int port) throws IOException {
        super(log);

        this.drop = new TCPDrop(log, tcpFactory, fileFactory, name, port);

        if (type.equalsIgnoreCase("match")) {
            MatchPrinter matchPrinter = new MatchPrinter(
                    connector,
                    contributors,
                    securities,
                    traders,
                    accounts,
                    str -> {
                        drop.add(str);

                        if (!noLog) {
                            log.info(log.log().add(str));
                        }
                    },
                    style);
            dispatcher.subscribe(matchPrinter);
        }
    }

    @Exposed(name="log")
    public void out(@Param(name="output") boolean output) {
        noLog = !output;
    }

    @Override
    protected void onActive() {
        try {
            drop.open();
        } catch (Exception e) {
            throw new CommandException(e);
        }
    }

    @Override
    public void onAddHeartbeatFields(HeartbeatFieldRegister fieldRegister) {
    }

    @Override
    public void onUpdateHeartbeatFields(HeartbeatFieldUpdater fieldUpdater) {
    }
}
