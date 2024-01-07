package com.core.match.fairies.cancel;

import com.core.app.AppConstructor;
import com.core.app.CommandException;
import com.core.app.Exposed;
import com.core.app.Param;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.connector.AllCommandsClearedListener;
import com.core.connector.Dispatcher;
import com.core.match.MatchApplication;
import com.core.match.MatchCommandSender;
import com.core.match.msgs.MatchAccountEvent;
import com.core.match.msgs.MatchCancelCommand;
import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchContributorEvent;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchMessages;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.match.msgs.MatchSecurityEvent;
import com.core.match.msgs.MatchTraderEvent;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.account.AccountServiceListener;
import com.core.match.services.contributor.Contributor;
import com.core.match.services.contributor.ContributorService;
import com.core.match.services.contributor.ContributorServiceListener;
import com.core.match.services.events.SystemEventListener;
import com.core.match.services.events.SystemEventService;
import com.core.match.services.order.OrderService;
import com.core.match.services.order.OrderServiceListener;
import com.core.match.services.order.ReplaceUpdates;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.MultiLegSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.security.SecurityServiceListener;
import com.core.match.services.security.SecurityType;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.match.services.trader.TraderServiceListener;
import com.core.services.StaticsList;
import com.core.services.StaticsService;
import com.core.util.log.Log;
import com.gs.collections.api.iterator.IntIterator;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.set.mutable.primitive.IntHashSet;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by jgreco on 7/14/15.
 */
public class CancelFairy extends MatchApplication implements
        SystemEventListener,
        OrderServiceListener<CancelFairyOrder>,
        AllCommandsClearedListener {
    private final OrderService<CancelFairyOrder> orders;
    private final TraderService<Trader> traders;
    private final AccountService<Account> accounts;
    private final SecurityService<BaseSecurity> securities;
    private final ContributorService<Contributor> contributors;

    final List<IntHashSet> traderOrders = new ArrayList<>();
    final List<IntHashSet> accountOrders = new ArrayList<>();
    final List<IntHashSet> securityOrders = new ArrayList<>();
    final List<IntHashSet> contributorOrders = new ArrayList<>();

    private final List<CancelTask> cancelTasks = new FastList<>();
    private final MatchMessages messages;

    @AppConstructor
    public CancelFairy(Log log,
                       MatchCommandSender sender,
                       MatchMessages messages,
                       Dispatcher dispatcher,
                       TraderService<Trader> traders,
                       AccountService<Account> accounts,
                       SecurityService<BaseSecurity> securities,
                       ContributorService<Contributor> contributors,
                       SystemEventService events) {
        super(log, sender);

        this.messages = messages;

        this.orders = OrderService.create(CancelFairyOrder.class, log, dispatcher);
        this.traders = traders;
        this.accounts = accounts;
        this.securities = securities;
        this.contributors = contributors;

        events.addListener(this);
        this.orders.addListener(this);

        this.traders.addListener(new TraderServiceListener<Trader>() {
            @Override
            public void onTrader(Trader trader, MatchTraderEvent msg, boolean isNew) {
                if (isNew) {
                    traderOrders.add(new IntHashSet());
                }
            }
        });

        this.accounts.addListener(new AccountServiceListener<Account>() {
            @Override
            public void onAccount(Account account, MatchAccountEvent msg, boolean isNew) {
                if (isNew) {
                    accountOrders.add(new IntHashSet());
                }
            }
        });

        this.securities.addListener(new SecurityServiceListener<BaseSecurity>() {
            @Override
            public void onBond(Bond security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
                if (isNew) {
                    securityOrders.add(new IntHashSet());
                }
            }

            @Override
            public void onMultiLegSecurityInstrument(MultiLegSecurity security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
                if (isNew) {
                    securityOrders.add(new IntHashSet());
                }
            }
        });

        this.contributors.addListener(new ContributorServiceListener<Contributor>() {
            @Override
            public void onContributor(Contributor contributor, MatchContributorEvent msg, boolean isNew) {
                if (isNew) {
                    contributorOrders.add(new IntHashSet());
                }
            }
        });

        addAllCommandsClearedListener(this);
    }

    @Override
    public void onAddHeartbeatFields(HeartbeatFieldRegister fieldRegister) {

    }

    @Override
    public void onUpdateHeartbeatFields(HeartbeatFieldUpdater fieldUpdater) {

    }

    @Exposed(name="cancelAccount")
    public void cancelAccount(@Param(name = "accountName") String accountName) {
        cancel(accounts, accountOrders, accountName);
    }

    @Exposed(name="cancelTrader")
    public void cancelTrader(@Param(name = "traderName") String traderName) {
        cancel(traders, traderOrders, traderName);
    }

    @Exposed(name="cancelSecurity")
    public void cancelSecurity(@Param(name = "securityName") String securityName) {
        cancel(securities, securityOrders, securityName);
    }

    @Exposed(name = "cancelContributor")
    public void cancelContributor(@Param(name = "contributorName") String contributorName) {
        cancel(contributors, contributorOrders, contributorName);
    }

    @Exposed(name = "cancelOrder")
    public void cancelOrder(@Param(name="orderID") int orderID) {
        if (!cancelOrderByID(orderID)) {
            throw new CommandException("Unknown OrderID");
        }
    }

    @Exposed(name = "cancelAll")
    public void cancelAll() {
        if (!canSend()) {
            throw new CommandException("Cannot send cancels now");
        }

        if (orders.size() > 0) {
            cancelTasks.add(new CancelTask(orders.getOrderIDs()));
            executeCancelTasks();
        }
        else {
            throw new CommandException("No orders to cancel");
        }
    }

    private <T extends StaticsList.StaticsObject> void cancel(StaticsService<T> service, List<IntHashSet> collection, String name) {
        if (!canSend()) {
            throw new CommandException("Cannot send cancels now");
        }

        T obj = service.get(name);
        if (obj == null) {
            throw new CommandException("Unknown: " + name);
        }

        IntHashSet orders = getOrderIDs(collection, obj.getID());
        if (orders.size() > 0) {
            log.info(log.log().add("Added ").add(orders.size()).add(" orders to cancel for ").add(name));
            cancelTasks.add(new CancelTask(orders));
            executeCancelTasks();
        }
        else {
            log.error(log.log().add("No orders to cancel for ").add(name));
        }
    }

    @Override
    public void onOrder(CancelFairyOrder order, MatchOrderEvent msg) {
        order.setContributorID(msg.getContributorID());
        add(order);
    }

    @Override
    public void onCancel(CancelFairyOrder order, MatchCancelEvent msg) {
        remove(order);
    }

    @Override
    public void onReplace(CancelFairyOrder order, MatchReplaceEvent msg, ReplaceUpdates updates) {
    }

    @Override
    public void onFill(CancelFairyOrder order, MatchFillEvent msg) {
        if (order.isFilled()) {
            remove(order);
        }
    }

    private void add(CancelFairyOrder order) {
        int orderID = order.getID();
        Trader trader = traders.get(order.getTraderID());

        add(accountOrders, trader.getAccountID(), orderID);
        add(traderOrders, order.getTraderID(), orderID);
        add(securityOrders, order.getSecurityID(), orderID);
        add(contributorOrders, order.getContributorID(), orderID);
    }

    private void remove(CancelFairyOrder order) {
        int orderID = order.getID();
        Trader trader = traders.get(order.getTraderID());

        remove(accountOrders, trader.getAccountID(), orderID);
        remove(traderOrders, order.getTraderID(), orderID);
        remove(securityOrders, order.getSecurityID(), orderID);
        remove(contributorOrders, order.getContributorID(), orderID);
    }

    @Override
    public boolean isInterested(MatchOrderEvent msg) {
        return true;
    }

    @Override
    public void onAllCommandsCleared() {
        executeCancelTasks();
    }

    @Override
    protected void onActive() {
        cancelTasks.clear();
    }

    private void executeCancelTasks() {
        if (!canSend()) {
            return;
        }

        while(!cancelTasks.isEmpty()) {
            CancelTask cancelTask = cancelTasks.get(0);
            while (cancelTask.hasNext()) {
                int orderID = cancelTask.next();
                if (!cancelOrderByID(orderID)) {
                    continue;
                }
                return;
            }
            cancelTasks.remove(0);
        }
    }

    private boolean cancelOrderByID(int orderID) {
        CancelFairyOrder order = orders.get(orderID);
        if (order == null) {
            return false;
        }

        log.info(log.log().add("Sent cancel for Order. ID=").add(orderID)
                .add(", Trader=").add(traders.get(order.getTraderID()).getName())
                .add(", Security=").add(securities.get(order.getSecurityID()).getName())
                .add(", Contributor=").add(contributors.get(order.getContributorID()).getName()));

        MatchCancelCommand cancelCmd = messages.getMatchCancelCommand();
        cancelCmd.setOrderID(orderID);
        send(cancelCmd);
        return true;
    }

    private static void add(List<IntHashSet> staticsObjectList, short staticsObjectID, int orderID) {
        getOrderIDs(staticsObjectList, staticsObjectID).add(orderID);
    }

    private static void remove(List<IntHashSet> staticsObjectList, short staticsObjectID, int orderID) {
        getOrderIDs(staticsObjectList, staticsObjectID).remove(orderID);
    }

    private static IntHashSet getOrderIDs(List<IntHashSet> list, short id) {
        return list.get(id - MatchConstants.STATICS_START_INDEX);
    }

    @Override
    public void onOpen(long timestamp) {
    }

    @Override
    public void onClose(long timestamp) {
        if (orders.size() > 0) {
            cancelTasks.add(new CancelTask(orders.getOrderIDs()));
            executeCancelTasks();
        }
    }

    private static class CancelTask implements IntIterator {
        int[] ordersToCancel;
        int position;

        public CancelTask(IntHashSet orders) {
            this(orders.toArray());
        }

        public CancelTask(int[] orderIDs) {
            ordersToCancel = orderIDs;
        }

        @Override
		public boolean hasNext() {
            return position < ordersToCancel.length;
        }

        @Override
		public int next() {
            return ordersToCancel[position++];
        }
    }
}
