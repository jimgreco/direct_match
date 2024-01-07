package com.core.match.monitors.account;

import com.core.app.AppConstructor;
import com.core.app.Exposed;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.connector.Dispatcher;
import com.core.match.MatchApplication;
import com.core.match.msgs.*;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.account.AccountServiceListener;
import com.core.match.services.contributor.Contributor;
import com.core.match.services.contributor.ContributorService;
import com.core.match.services.contributor.ContributorServiceListener;
import com.core.match.services.order.OrderServiceListener;
import com.core.match.services.order.OrderServiceRejectListener;
import com.core.match.services.order.OrderServiceWithRejectsContribIDFiltered;
import com.core.match.services.order.ReplaceUpdates;
import com.core.match.services.risk.RiskAccount;
import com.core.match.services.risk.RiskService;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.match.services.trader.TraderServiceListener;
import com.core.util.log.Log;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.primitive.ShortObjectHashMap;

import java.util.List;

import static com.core.match.msgs.MatchConstants.MAX_LIVE_ORDERS;

/**
 * Created by hli on 10/21/15.
 */
public class AccountStatisticMonitor extends MatchApplication implements
        OrderServiceListener<AccountStatsOrder>,
        OrderServiceRejectListener<AccountStatsOrder>,
        TraderServiceListener<Trader>,
        AccountServiceListener<Account>,
        ContributorServiceListener<Contributor> {
    private final ShortObjectHashMap<AccountStats> acctMap =new ShortObjectHashMap<>();
    private final ShortObjectHashMap<TraderStats> traderMap =new ShortObjectHashMap<>();
    private final ShortObjectHashMap<ContributorStats> contribMap = new ShortObjectHashMap<>();

    private final TraderService<Trader> traders;
    private final AccountService<Account> accounts;

    private final RiskService<AccountStatsOrder> riskService;

    private final AccountStatsResults accountResults = new AccountStatsResults();
    private final TraderStatsResults traderResults = new TraderStatsResults();
    private final ContribStatsResults contribResults = new ContribStatsResults();

    @AppConstructor
    public AccountStatisticMonitor(Log log,
                                   Dispatcher dispatcher,
                                   AccountService<Account> accounts,
                                   TraderService<Trader> traders,
                                   SecurityService<BaseSecurity> securities,
                                   ContributorService<Contributor> contribs) {
        super(log);

        // TODO: Why aren't we just adding listeners inside riskservice?

        OrderServiceWithRejectsContribIDFiltered<AccountStatsOrder> orders =
            new OrderServiceWithRejectsContribIDFiltered<>(AccountStatsOrder.class, log, dispatcher, MAX_LIVE_ORDERS);
        this.riskService = new RiskService<>(accounts, traders, securities, log);
        this.accounts = accounts;
        this.traders = traders;

        orders.setIsInterestedListener(this);
        orders.addListener(riskService);
        orders.addListener(this);
        orders.addRejectListener(this);

        accounts.addListener(riskService);

        traders.addListener(this);
        accounts.addListener(this);
        contribs.addListener(this);
    }

    @Override
    public void onAddHeartbeatFields(HeartbeatFieldRegister fieldRegister) {
    }

    @Override
    public void onUpdateHeartbeatFields(HeartbeatFieldUpdater fieldUpdater) {
    }

    @Exposed(name="accounts")
    public AccountStatsResults  getAccounts(){
        return accountResults;
    }

    @Exposed(name="traders")
    public TraderStatsResults  getTraders(){
        return traderResults;
    }

    @Exposed(name="contribs")
    public ContribStatsResults  getContributors(){
        return contribResults;
    }

    @Override
    public void onOrder(AccountStatsOrder order, MatchOrderEvent msg) {
        order.setContribID(msg.getContributorID());

        TraderStats ts = traderMap.get(order.getTraderID());
        AccountStats as = acctMap.get(ts.acctId);
        ContributorStats cs = contribMap.get(msg.getContributorID());
        Trader trader = traders.get(order.getTraderID());
        RiskAccount account = riskService.getAccount(trader.getAccountID());
        int qty = msg.getQty();

        ts.incOrders();
        ts.addSendQty(qty);
        ts.addQtyOutstanding(qty);

        as.incOrders();
        as.addSendQty(qty);
        as.addQtyOutstanding(qty);

        cs.incOrders();
        cs.addSendQty(qty);
        cs.addQtyOutstanding(qty);

        as.setMaxDV01Exp(account.getMaxExposedDV01());
        as.setFilNetDV01(account.getNetDV01());
    }
    
    @Override
    public void onCancel(AccountStatsOrder order, MatchCancelEvent msg) {
        TraderStats ts = traderMap.get(order.getTraderID());
        AccountStats as = acctMap.get(ts.acctId);
        ContributorStats cs = contribMap.get(order.getContribID());
        Trader trader = traders.get(order.getTraderID());
        RiskAccount account = riskService.getAccount(trader.getAccountID());
        
        ts.incCancels();
        ts.addQtyOutstanding(-1 * order.getRemainingQty());

        as.incCancels();
        as.addQtyOutstanding(-1 * order.getRemainingQty());

        cs.incCancels();
        cs.addQtyOutstanding(-1 * order.getRemainingQty());

        as.setMaxDV01Exp(account.getMaxExposedDV01());
        as.setFilNetDV01(account.getNetDV01());
    }

    @Override
    public void onReplace(AccountStatsOrder order, MatchReplaceEvent msg, ReplaceUpdates updates) {
        TraderStats ts = traderMap.get(order.getTraderID());
        AccountStats as = acctMap.get(ts.acctId);
        ContributorStats cs = contribMap.get(order.getContribID());
        Trader trader = traders.get(order.getTraderID());
        RiskAccount account = riskService.getAccount(trader.getAccountID());
        int qty = msg.getQty();

        ts.incReplaces();
        as.incReplaces();
        cs.incReplaces();

        if(updates.isQtyUpdated()){
            ts.addQtyOutstanding(qty - updates.getOldQty());
            as.addQtyOutstanding(qty - updates.getOldQty());
            cs.addQtyOutstanding(qty - updates.getOldQty());
        }

        as.setMaxDV01Exp(account.getMaxExposedDV01());
        as.setFilNetDV01(account.getNetDV01());
    }

    @Override
    public void onFill(AccountStatsOrder order, MatchFillEvent msg) {
        TraderStats ts = traderMap.get(order.getTraderID());
        AccountStats as = acctMap.get(ts.acctId);
        ContributorStats cs = contribMap.get(order.getContribID());
        Trader trader = traders.get(order.getTraderID());
        RiskAccount account = riskService.getAccount(trader.getAccountID());
        int qty = msg.getQty();

        ts.incFills();
        ts.addFilledQty(qty);
        ts.addQtyOutstanding(-1 * qty);

        as.incFills();
        as.addFilledQty(qty);
        as.addQtyOutstanding(-1 * qty);

        cs.incFills();
        cs.addFilledQty(qty);
        cs.addQtyOutstanding(-1 * qty);

        as.setMaxDV01Exp(account.getMaxExposedDV01());
        as.setFilNetDV01(account.getNetDV01());

        if (as.isFICC()) {
            // reverse the DV01, venue is opposite of client
            double tradeDV01 = -1 * riskService.dv01(order, msg.getPrice(), qty);
            accountResults.addFICCDV01(tradeDV01);
        }
    }

    @Override
    public void onOrderReject(MatchOrderRejectEvent msg) {
        if (traderMap.contains(msg.getTraderID())) {
            TraderStats ts = traderMap.get(msg.getTraderID());
            ts.incRejectOrder();

            AccountStats as = acctMap.get(ts.getAcctId());
            as.incRejectOrder();
        }

        ContributorStats cs = contribMap.get(msg.getContributorID());
        cs.incRejectOrder();
    }

    @Override
    public void onClientCancelReplaceReject(AccountStatsOrder order, MatchClientCancelReplaceRejectEvent msg) {
        if (order != null && traderMap.contains(order.getTraderID())) {
            TraderStats ts = traderMap.get(order.getTraderID());
            ts.incRejectOrder();

            AccountStats as = acctMap.get(ts.getAcctId());
            as.incRejectOrder();
        }

        ContributorStats cs = contribMap.get(order != null ? order.getContribID() : msg.getContributorID());
        cs.incRejectOrder();
    }

    @Override
    public void onClientOrderReject(MatchClientOrderRejectEvent msg) {
        Trader trader = traders.get(msg.getTraderAsString());
        Account account = trader!=null ? accounts.get(trader.getAccountID()) : null;

        if (trader != null) {
            TraderStats ts = traderMap.get(trader.getID());
            ts.incRejectOrder();
        }

        if (account != null) {
            AccountStats as = acctMap.get(account.getID());
            as.incRejectOrder();
        }

        ContributorStats cs = contribMap.get(msg.getContributorID());
        cs.incRejectOrder();
    }

    @Override
    public void onCancelReplaceReject(AccountStatsOrder order, MatchCancelReplaceRejectEvent msg) {
        if (order != null) {
            Trader trader = traders.get(order.getTraderID());
            Account account = trader != null ? accounts.get(trader.getAccountID()) : null;

            if (trader != null) {
                TraderStats ts = traderMap.get(trader.getID());
                ts.incRejectOrder();
            }

            if (account != null) {
                AccountStats as = acctMap.get(account.getID());
                as.incRejectOrder();
            }
        }

        ContributorStats cs = contribMap.get(order != null ? order.getContribID() : msg.getContributorID());
        cs.incRejectOrder();
    }

    @Override
    public boolean isInterested(MatchOrderEvent msg) {
        return true;
    }

    @Override
    public void onTrader(Trader trader, MatchTraderEvent msg, boolean isNew) {
        if(isNew) {
            TraderStats ts = new TraderStats();
            ts.id = trader.getID();
            ts.trad = trader.getName();
            traderMap.put(trader.getID(), ts);
            traderResults.add(ts);
        }

        TraderStats ts = traderMap.get(trader.getID());
        ts.acctId = trader.getAccountID();
        ts.acct = accounts.get(trader.getAccountID()).getName();
        ts.ffQtyLmt = trader.getLimitsString();
    }

    @Override
    public void onAccount(Account account, MatchAccountEvent msg, boolean isNew) {
        if(isNew) {
            AccountStats as = new AccountStats();
            as.acct = account.getName();
            as.id = account.getID();
            acctMap.put(as.id, as);
            accountResults.add(as);
        }

        AccountStats as = acctMap.get(account.getID());
        as.netDv01Lmt = account.getNetDV01Limit();
        as.ficc = !msg.getNettingClearing();
    }

    @Override
    public void onContributor(Contributor contributor, MatchContributorEvent msg, boolean isNew) {
        if (isNew) {
            ContributorStats cs = new ContributorStats();
            cs.contrib = contributor.getName();
            cs.id = contributor.getID();
            contribMap.put(cs.id, cs);
            contribResults.add(cs);
        }
    }

    private class AccountStatsResults {
        private double FICCDV01;
        private List<AccountStats> accounts = new FastList<>();

        public double getFICCDV01() {
            return FICCDV01;
        }

        public List<AccountStats> getAccounts() {
            return accounts;
        }

        public void addFICCDV01(double dv01) {
            FICCDV01 += dv01;
        }

        public void add(AccountStats as) {
            accounts.add(as);
        }
    }

    private class TraderStatsResults {
        private List<TraderStats> traders = new FastList<>();

        public List<TraderStats> getTraders() {
            return traders;
        }

        public void add(TraderStats ts) {
            traders.add(ts);
        }
    }

    private class ContribStatsResults {
        private List<ContributorStats> contribs = new FastList<>();

        public List<ContributorStats> getContribs() {
            return contribs;
        }

        public void add(ContributorStats cs) {
            contribs.add(cs);
        }
    }
}
