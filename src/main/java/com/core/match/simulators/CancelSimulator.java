package com.core.match.simulators;

import com.core.app.AppConstructor;
import com.core.app.Param;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.connector.Dispatcher;
import com.core.match.MatchApplication;
import com.core.match.MatchCommandSender;
import com.core.match.msgs.MatchCancelCommand;
import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchMessages;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.order.BaseOrder;
import com.core.match.services.order.OrderService;
import com.core.match.services.order.OrderServiceListener;
import com.core.match.services.order.ReplaceUpdates;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.util.log.Log;

import java.util.BitSet;


/**
 * Created by jgreco on 7/31/15.
 */
public class CancelSimulator extends MatchApplication implements OrderServiceListener<BaseOrder> {
    private final MatchMessages messages;
    private final BitSet accountIDs = new BitSet();
    private final TraderService<Trader> traders;

    @AppConstructor
    public CancelSimulator(Log log,
                           MatchCommandSender sender,
                           MatchMessages messages,
                           Dispatcher dispatcher,
                           AccountService<Account> accounts,
                           TraderService<Trader> traders,
                           @Param(name="Accounts") String accountsToCancel) {
        super(log, sender);

        this.traders = traders;

        this.messages = messages;
        String[] accountNames = accountsToCancel.split(",");

        accounts.addListener((account, msg, isNew) -> {
            for (String accountName : accountNames) {
                if (account.getName().equals(accountName)) {
                    accountIDs.set(account.getID(), true);
                }
            }
        });

        OrderService<BaseOrder> orders = OrderService.create(BaseOrder.class, log, dispatcher);
        orders.addListener(this);
    }

    @Override
    public void onAddHeartbeatFields(HeartbeatFieldRegister fieldRegister) {
    }

    @Override
    public void onUpdateHeartbeatFields(HeartbeatFieldUpdater fieldUpdater) {
    }

    @Override
    public void onOrder(BaseOrder order, MatchOrderEvent msg) {
        if (canSend()) {
            MatchCancelCommand cancel = messages.getMatchCancelCommand();
            cancel.setOrderID(msg.getOrderID());
            send(cancel);
        }
    }

    @Override
    public void onCancel(BaseOrder order, MatchCancelEvent msg) {

    }

    @Override
    public void onReplace(BaseOrder order, MatchReplaceEvent msg, ReplaceUpdates updates) {

    }

    @Override
    public void onFill(BaseOrder order, MatchFillEvent msg) {

    }

    @Override
    public boolean isInterested(MatchOrderEvent msg) {
        Trader trader = traders.get(msg.getTraderID());
        return accountIDs.get(trader.getAccountID());
    }
}
