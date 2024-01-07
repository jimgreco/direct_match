package com.core.match.drops.gui;

import com.core.app.AppConstructor;
import com.core.app.Param;
import com.core.app.heartbeats.HeartBeatFieldIDEnum;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.app.heartbeats.HeartbeatNumberField;
import com.core.connector.Connector;
import com.core.connector.Dispatcher;
import com.core.match.drops.LinearCounter;
import com.core.match.drops.VersionedDropBase;
import com.core.match.msgs.*;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.contributor.Contributor;
import com.core.match.services.contributor.ContributorService;
import com.core.match.services.order.*;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.util.log.Log;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerService;

/**
 * Created by jgreco on 2/21/16.
 */
public class BlotterDrop extends VersionedDropBase implements
        OrderServiceListener<BlotterDropOrder>,
        OrderServiceRejectListener<BlotterDropOrder>
{
    private final SecurityService<BaseSecurity> securities;
    private final AccountService<Account> accounts;
    private final TraderService<Trader> traders;

    private final BlotterCollection blotterCollection;
    private final OrderServiceWithRejectsContribIDFiltered<BlotterDropOrder> rejectService;

    public short GUIContribID;

    private HeartbeatNumberField liveOrdersHeartbeat;

    @AppConstructor
    public BlotterDrop(Log log,
                       Dispatcher dispatcher,
                       TimeSource time,
                       TimerService timers,
                       TCPSocketFactory factory,
                       Connector connector,
                       SecurityService<BaseSecurity> securities,
                       TraderService<Trader> traders,
                       AccountService<Account> accounts,
                       ContributorService<Contributor> contributors,
                       @Param(name = "Name") String name,
                       @Param(name = "Port") int port,
                       @Param(name = "TimeoutMS") int timeOutMS,
                       @Param(name = "GUIContributor") String GUIContributorName) {
        super(log, time, timers, factory, connector, name, timeOutMS, port);

        this.securities = securities;
        this.traders = traders;
        this.accounts = accounts;

        rejectService = new OrderServiceWithRejectsContribIDFiltered<>(BlotterDropOrder.class, log, dispatcher, MatchConstants.MAX_LIVE_ORDERS);
        rejectService.addListener(this);
        rejectService.addRejectListener(this);

        contributors.addListener((contributor, msg, isNew) -> {
            if (isNew && contributor.getName().equals(GUIContributorName)) {
                GUIContribID = msg.getSourceContributorID();
                log.info(log.log().add("Contributor defined: " + GUIContribID));
                rejectService.onContributorDefined(GUIContribID, GUIContributorName);
            }
        });

        blotterCollection = new BlotterCollection(versionCounter, new LinearCounter(), securities, traders, accounts);
        addCollection(blotterCollection);
    }

    @Override
    public void onAddHeartbeatFields(HeartbeatFieldRegister fieldRegister) {
        super.onAddHeartbeatFields(fieldRegister);

        liveOrdersHeartbeat = fieldRegister.addNumberField("DROP", HeartBeatFieldIDEnum.LiveOrders);
    }

    @Override
    public void onUpdateHeartbeatFields(HeartbeatFieldUpdater fieldUpdater) {
        super.onUpdateHeartbeatFields(fieldUpdater);

        liveOrdersHeartbeat.set(rejectService.size());
    }

    @Override
    public boolean isInterested(MatchOrderEvent msg) {
        return msg.getContributorID() == GUIContribID;
    }

    @Override
    public void onOrder(BlotterDropOrder order, MatchOrderEvent msg) {
        if (msg.hasClOrdID()) {
            order.clordid = msg.getClOrdIDAsString();
        }
        order.created = msg.getTimestamp();
        order.updated = msg.getTimestamp();
        order.contributorID = msg.getContributorID();

        blotterCollection.order(order);
    }

    @Override
    public void onCancel(BlotterDropOrder order, MatchCancelEvent msg) {
        if (msg.hasClOrdID()) {
            order.clordid = msg.getClOrdIDAsString();
        }
        order.updated = msg.getTimestamp();

        blotterCollection.cancel(order);
    }

    @Override
    public void onReplace(BlotterDropOrder order, MatchReplaceEvent msg, ReplaceUpdates updates) {
        if (msg.hasClOrdID()) {
            order.clordid = msg.getClOrdIDAsString();
        }
        order.updated = msg.getTimestamp();

        blotterCollection.replace(order);
    }

    @Override
    public void onFill(BlotterDropOrder order, MatchFillEvent msg) {
        order.updated = msg.getTimestamp();
        order.notional += msg.getQty() * msg.getPrice();

        blotterCollection.fill(order, msg.getQty(), msg.getPrice());
    }

    @Override
    public void onOrderReject(MatchOrderRejectEvent msg) {
        if(!validateContributor(msg.getContributorID())){
            return ;
        }

        BaseSecurity security = securities.get(msg.getSecurityID());
        Trader trader = traders.get(msg.getTraderID());
        Account account = trader != null ? accounts.get(trader.getAccountID()) : null;

        blotterCollection.reject(
                msg.hasClOrdID() ? msg.getClOrdIDAsString() : "",
                security != null ? security.getName() : "",
                msg.getBuy(),
                account != null ? account.getName() : "",
                trader != null ? trader.getName() : "",
                msg.getTimestamp(),
                msg.hasText() ? msg.getTextAsString() : "");
    }

    @Override
    public void onClientOrderReject(MatchClientOrderRejectEvent msg) {
        if(!validateContributor(msg.getContributorID())){
            return ;
        }

        blotterCollection.reject(
                msg.hasClOrdID() ? msg.getClOrdIDAsString() : "",
                msg.hasSecurity() ? msg.getSecurityAsString() : "",
                msg.getBuy(),
                "",
                msg.hasTrader() ? msg.getTraderAsString() : "",
                msg.getTimestamp(),
                msg.hasText() ? msg.getTextAsString() : "");
    }

    @Override
    public void onCancelReplaceReject(BlotterDropOrder order, MatchCancelReplaceRejectEvent msg) {
        // no GUI replaces yet
    }

    @Override
    public void onClientCancelReplaceReject(BlotterDropOrder order, MatchClientCancelReplaceRejectEvent msg) {
        // no GUI replaces yet
    }

    private boolean validateContributor(short contribId){
        return contribId == GUIContribID;
    }
}
