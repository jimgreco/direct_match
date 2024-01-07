package com.core.match.drops.gui;

import com.core.app.AppConstructor;
import com.core.app.Param;
import com.core.connector.Connector;
import com.core.connector.Dispatcher;
import com.core.match.drops.LinearCounter;
import com.core.match.drops.VersionedDropBase;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.order.IsInterestedListener;
import com.core.match.services.order.OrderService;
import com.core.match.services.risk.RiskService;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.util.log.Log;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerService;

/**
 * Created by jgreco on 11/10/16.
 */
public class RiskDrop extends VersionedDropBase implements IsInterestedListener {

	@AppConstructor
    public RiskDrop(Log log,
                    TimeSource time,
                    TimerService timers,
                    TCPSocketFactory factory,
                    Connector connector,
                    Dispatcher dispatcher,
                    AccountService<Account> accounts,
                    TraderService<Trader> traders,
                    SecurityService<BaseSecurity> securities,
                    @Param(name = "Name") String name,
                    @Param(name = "Port") int port,
                    @Param(name = "TimeoutMS") int timeOutMS) {
        super(log, time, timers, factory, connector, name, timeOutMS, port);

        OrderService<RiskDropOrder> orderService = OrderService.create(RiskDropOrder.class, log, dispatcher);
        orderService.setIsInterestedListener(this);
        
        RiskService<RiskDropOrder> riskService = new RiskService<>(accounts, traders, securities, log);
        orderService.addListener(riskService);

        RiskDropCollection dropCollection = new RiskDropCollection(versionCounter, new LinearCounter(), accounts, riskService);
        addCollection(dropCollection);
    }

	@Override
	public boolean isInterested(MatchOrderEvent msg) {
		return true;
	}
}
