package com.core.match.services.risk;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.core.match.msgs.MatchAccountEvent;
import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.account.AccountServiceListener;
import com.core.match.services.order.Order;
import com.core.match.services.order.OrderService;
import com.core.match.services.order.OrderServiceListener;
import com.core.match.services.order.ReplaceUpdates;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.SecurityService;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.match.util.MatchBondMath;
import com.core.util.log.Log;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;

/**
 * Created by jgreco on 5/19/15.
 */
public class RiskService<T extends Order<T> & RiskOrderAttributes> implements
        OrderServiceListener<T>,
        AccountServiceListener<Account> {
    private final AccountService<Account> accounts;
    private final TraderService<Trader> traders;
    private final SecurityService<BaseSecurity> securities;
    private final Set<String> accountNames = new HashSet<>();
    private final BitSet interestedAccountIDs = new BitSet();
    private final Log log;
    private  final IntObjectHashMap<RiskAccount> accountMap=new IntObjectHashMap<>();
    private final List<RiskServiceListener> listeners = new FastList<>();


    public RiskService(OrderService<T> orders,
                       AccountService<Account> accounts,
                       TraderService<Trader> traders,
                       SecurityService<BaseSecurity> securities,
                       Log log) {
        this(accounts, traders, securities, log);

        orders.addListener(this);
    }

    public RiskService(AccountService<Account> accounts,
                       TraderService<Trader> traders,
                       SecurityService<BaseSecurity> securities,
                       Log log) {
        this.accounts = accounts;
        this.traders = traders;
        this.securities = securities;
        this.log=log;

        accounts.addListener(this);
    }

    public void isInterestedInAccount(String name) {
        accountNames.add(name);
    }

    //Static because this is non-dependent to the state of the class
    public static boolean violatesFatFingerQuantityLimit(Trader trader, int newQty, Bond security) {
        int traderSecurityLimit=getLimitForTraderAndSecurity(trader, security);
        return Math.abs(newQty) > traderSecurityLimit;
    }

    //Static because this is non-dependent to the state of the class
    public static int getLimitForTraderAndSecurity(Trader trader, Bond security){
        int term = security.getTerm();
        if (term <= 2) {
            return trader.getFatFinger2YQtyLimit();
        }
        else if (term <= 3) {
            return trader.getFatFinger3YQtyLimit();
        }
        else if (term <= 5) {
            return trader.getFatFinger5YQtyLimit();
        }
        else if (term <= 7) {
            return trader.getFatFinger7YQtyLimit();
        }
        else if (term <= 10) {
            return trader.getFatFinger10YQtyLimit();
        }
        else {
            return trader.getFatFinger30YQtyLimit();
        }
    }

    public  boolean violatesDV01Limit(Account account, double signedNewDV01) {
        RiskAccount riskAccount= accountMap.get(account.getID());
        return Math.abs(riskAccount.getMaxExposedDV01() + signedNewDV01) > account.getNetDV01Limit();
    }

    public double dv01( T order, long price, int qty ) {
        BaseSecurity sec = this.securities.get( order.getSecurityID() );

        if(sec!=null && sec.isBond() ){
            return ( order.isBuy() ? 1 : -1 ) * MatchBondMath.getDV01((Bond)sec, price, qty);
        } else {
            return 0;
        }
    }

    @Override
    public void onOrder(T order, MatchOrderEvent msg) {
        //New order created. Only contain unfilled qty
        Trader trader = traders.get(order.getTraderID());
        RiskAccount account = getAccount(trader.getAccountID());
        double newOrderOpenDV01 = dv01(order, order.getPrice(), order.getRemainingQty());

        order.setOpenDV01Contribution(newOrderOpenDV01);

        if(order.isBuy()){
            account.addOpenBuyDV01(newOrderOpenDV01);
        } else {
            account.addOpenSellDV01(newOrderOpenDV01);
        }

        notifyListeners(account);
    }

    @Override
    public void onCancel(T order, MatchCancelEvent msg) {
        double openDV01onOrder=order.getOpenDV01Contribution();
        Trader trader = traders.get(order.getTraderID());
        RiskAccount account = accountMap.get(trader.getAccountID());

        order.setOpenDV01Contribution(0);

        if(order.isBuy()){
            account.removeOpenBuyDV01(openDV01onOrder);
        } else {
            account.removeOpenSellDV01(openDV01onOrder);
        }
        
        notifyListeners(account);
    }

    @Override
    public void onReplace(T order, MatchReplaceEvent msg, ReplaceUpdates updates) {
        Trader trader = traders.get(order.getTraderID());
        RiskAccount account = accountMap.get(trader.getAccountID());
        double newOpenDv01 =  dv01(order, order.getPrice(), order.getRemainingQty());
        double oldOpenDV01 = order.getOpenDV01Contribution();

        order.setOpenDV01Contribution(newOpenDv01);

        if(order.isBuy()){
            account.removeOpenBuyDV01(oldOpenDV01);
            account.addOpenBuyDV01(newOpenDv01);
        } else {
            account.removeOpenSellDV01(oldOpenDV01);
            account.addOpenSellDV01(newOpenDv01);
        }
        
        notifyListeners(account);
    }

    @Override
    public void onFill(T order, MatchFillEvent msg) {
        Trader trader = traders.get(order.getTraderID());
        RiskAccount account = accountMap.get(trader.getAccountID());
        double filledDV01 = dv01(order, msg.getPrice(), msg.getQty());
        double openDV01Change = dv01(order, order.getPrice(), msg.getQty());

        // recompute the dv01 contribution from the order
        order.setFilledNetDV01Contribution(order.getFilledNetDV01Contribution() + filledDV01);
        order.setOpenDV01Contribution(order.getOpenDV01Contribution() - openDV01Change);

        // reduce the accounts net dv01 by the fill's contribution to the dv01 of the order
        account.addNetDV01(filledDV01);

        if(order.isBuy()){
            account.removeOpenBuyDV01(openDV01Change);
        } else {
            account.removeOpenSellDV01(openDV01Change);
        }
        
        notifyListeners(account);
    }

    @Override
    public boolean isInterested(MatchOrderEvent msg) {
        Trader trader = traders.get(msg.getTraderID());
        return interestedAccountIDs.get(trader.getAccountID());
    }

    @Override
    public void onAccount(Account account, MatchAccountEvent msg, boolean isNew) {
        if(isNew) {
            RiskAccount riskAccount=new RiskAccount();
            riskAccount.setID(account.getID());
            accountMap.put(account.getID(),riskAccount);

        }
        if (accountNames.contains(account.getName())) {
            interestedAccountIDs.set(msg.getAccountID());
        }
    }

    public RiskAccount getAccount(short id){
        return accountMap.get(id);
    }
    
    public void addListener(RiskServiceListener listener) {
        listeners.add(listener);
    }
    
    private void notifyListeners(RiskAccount account) {
    	for (int i = 0; i < listeners.size(); i++) {
    		RiskServiceListener listener = listeners.get(i);
    		listener.onAccountRiskUpdate(account);
		}
    }
}
