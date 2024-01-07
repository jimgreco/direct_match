package com.core.match.fix.stp;

import com.core.connector.Dispatcher;
import com.core.match.msgs.*;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.account.AccountServiceListener;
import com.core.match.services.order.*;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.match.services.trader.TraderServiceListener;
import com.core.match.util.MatchPriceUtils;
import com.core.util.log.Log;
import com.gs.collections.impl.set.mutable.UnifiedSet;
import com.gs.collections.impl.set.mutable.primitive.ShortHashSet;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jgreco on 2/15/16.
 */
public class FIXSTPOrderService implements
        OrderServiceListener<FIXSTPOrder>,
        MatchOrderRejectListener,
        MatchClientOrderRejectListener,
        MatchCancelReplaceRejectListener,
        MatchClientCancelReplaceRejectListener {
    private final ShortHashSet interestedInAccountIDs = new ShortHashSet();
    private final ShortHashSet interestedInTraderIDs = new ShortHashSet();
    private final UnifiedSet<ByteBuffer> interestedInTraderNames = new UnifiedSet<>();
    private final OrderService<FIXSTPOrder> orders;
    private final List<OrderServiceRejectListener<FIXSTPOrder>> listeners = new ArrayList<>();

    public FIXSTPOrderService(Log log,
                              Dispatcher dispatcher,
                              int size,
                              TraderService<Trader> traders,
                              AccountService<Account> accounts,
                              String[] accountNames) {
        orders = OrderService.create(FIXSTPOrder.class, log, dispatcher, size);
        orders.addListener(this);
        dispatcher.subscribe(this);

        accounts.addListener(new AccountServiceListener<Account>() {
            @Override
            public void onAccount(Account account, MatchAccountEvent msg, boolean isNew) {
                if (isNew) {
                    for (String accountName : accountNames) {
                        if (account.getName().equalsIgnoreCase(accountName) || accountName.equals("*")) {
                            log.info(log.log().add("Account: ").add(account.getName()).add("[").add(account.getID()).add("]"));
                            interestedInAccountIDs.add(account.getID());
                            return;
                        }
                    }
                }
            }
        });

        traders.addListener(new TraderServiceListener<Trader>() {
            @Override
            public void onTrader(Trader trader, MatchTraderEvent msg, boolean isNew) {
                if (isNew) {
                    if (interestedInAccountIDs.contains(trader.getAccountID())) {
                        log.info(log.log().add("Trader: ").add(trader.getName()).add("[").add(trader.getID()).add("]"));
                        interestedInTraderIDs.add(trader.getID());
                        interestedInTraderNames.add(ByteBuffer.wrap(trader.getName().getBytes()));
                    }
                }
            }
        });
    }

    @Override
    public void onOrder(FIXSTPOrder order, MatchOrderEvent msg) {
        if (msg.hasClOrdID()) {
            order.setClOrdID(msg.getClOrdID());
        }
        order.setIOC(msg.getIOC());
    }

    @Override
    public void onCancel(FIXSTPOrder order, MatchCancelEvent msg) {
        if (msg.hasClOrdID()) {
            order.setClOrdID(msg.getClOrdID());
        }
    }

    @Override
    public void onReplace(FIXSTPOrder order, MatchReplaceEvent msg, ReplaceUpdates updates) {
        if (msg.hasClOrdID()) {
            order.setClOrdID(msg.getClOrdID());
        }
        order.setReplaced(true);
    }

    @Override
    public void onFill(FIXSTPOrder order, MatchFillEvent msg) {
        order.addNotional(msg.getQty() * MatchPriceUtils.toDouble(msg.getPrice()));
    }

    @Override
    public boolean isInterested(MatchOrderEvent msg) {
        return interestedInTraderIDs.contains(msg.getTraderID());
    }

    @Override
    public void onMatchCancelReplaceReject(MatchCancelReplaceRejectEvent msg) {
        FIXSTPOrder order = orders.get(msg.getOrderID());
        if (order != null) {
            for (int i=0; i<listeners.size(); i++) {
                listeners.get(i).onCancelReplaceReject(order, msg);
            }
        }
    }

    @Override
    public void onMatchClientCancelReplaceReject(MatchClientCancelReplaceRejectEvent msg) {
        FIXSTPOrder order = orders.get(msg.getOrderID());
        if (order != null) {
            for (int i=0; i<listeners.size(); i++) {
                listeners.get(i).onClientCancelReplaceReject(order, msg);
            }
        }
    }

    @Override
    public void onMatchClientOrderReject(MatchClientOrderRejectEvent msg) {
        if (interestedInTraderNames.contains(msg.getTrader())) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).onClientOrderReject(msg);
            }
        }
    }

    @Override
    public void onMatchOrderReject(MatchOrderRejectEvent msg) {
        if (interestedInTraderIDs.contains(msg.getTraderID())) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).onOrderReject(msg);
            }
        }
    }

    public void addListener(OrderServiceListener<FIXSTPOrder> listener) {
        orders.addListener(listener);
    }

    public void addRejectListener(OrderServiceRejectListener<FIXSTPOrder> listener) {
        listeners.add(listener);
    }
}
