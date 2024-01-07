package com.core.match.db.jdbc;

import com.core.match.msgs.MatchAccountEvent;
import com.core.match.msgs.MatchAccountListener;
import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchContributorEvent;
import com.core.match.msgs.MatchContributorListener;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchQuoteEvent;
import com.core.match.msgs.MatchQuoteListener;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.match.msgs.MatchSecurityEvent;
import com.core.match.msgs.MatchSecurityListener;
import com.core.match.msgs.MatchTraderEvent;
import com.core.match.msgs.MatchTraderListener;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.order.BaseOrder;
import com.core.match.services.order.OrderService;
import com.core.match.services.order.OrderServiceListener;
import com.core.match.services.order.ReplaceUpdates;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.SecurityService;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.gs.collections.impl.list.mutable.FastList;

import java.sql.Date;
import java.util.List;

/**
 * Created by jgreco on 12/27/14.
 */
public class JDBCFieldsService implements
        OrderServiceListener<JDBCOrder>,
        MatchContributorListener,
        MatchQuoteListener {
    private final OrderService<JDBCOrder> orders;
    private final List<CommonContributor> contributors = new FastList<>();
    private JDBCOrder lastOrder;
    private final TraderService<Trader> traders;
    private final SecurityService<BaseSecurity> securities;
    private final AccountService<Account>accounts;

    public JDBCFieldsService(OrderService<JDBCOrder> orders, TraderService<Trader> traders, SecurityService<BaseSecurity> securities, AccountService<Account>accounts) {
        this.orders = orders;
        this.orders.addListener(this);
        this.accounts=accounts;
        this.traders=traders;
        this.securities=securities;
    }

    @Override
    public void onMatchContributor(MatchContributorEvent msg) {
        CommonContributor contributor = new CommonContributor();
        contributor.name = msg.getNameAsString();

        if (msg.getSourceContributorID() == contributors.size() + 1) {
            contributors.add(contributor);
        }
    }



    @Override
    public boolean isInterested(MatchOrderEvent msg) {
        return true;
    }

    @Override
    public void onOrder(JDBCOrder order, MatchOrderEvent msg) {
        if (msg.hasClOrdID()) {
            order.setClOrdId(msg.getClOrdID());
        }
    }


    @Override
    public void onCancel(JDBCOrder order, MatchCancelEvent msg) {
        if (msg.hasClOrdID()) {
            order.setClOrdId(msg.getClOrdID());
        }

        if (msg.hasOrigClOrdID()) {
            order.setOrigClOrdId(msg.getOrigClOrdID());
        }

        lastOrder = new JDBCOrder(
                order.getID(),
                order.isBuy(),
                order.getPrice(),
                order.getQty(),
                order.getCumQty(),
                order.getSecurityID(),
                order.getTraderID(),
                order.getClOrdID(),
                order.getOrigClOrdId(),
                order.getOldQty(),
                order.getOldPrice());
    }

    @Override
    public void onReplace(JDBCOrder order, MatchReplaceEvent msg, ReplaceUpdates updates) {
        if (msg.hasClOrdID()) {
            order.setClOrdId(msg.getClOrdID());
        }

        if (msg.hasOrigClOrdID()) {
            order.setOrigClOrdId(msg.getOrigClOrdID());
        }

        order.setOldQty(updates.getOldQty());
        order.setOldPrice(updates.getOldPrice());
    }

    @Override
    public void onFill(JDBCOrder order, MatchFillEvent msg) {
        if (order.isFilled()) {
            lastOrder = new JDBCOrder(
                    order.getID(),
                    order.isBuy(),
                    order.getPrice(),
                    order.getQty(),
                    order.getCumQty(),
                    order.getSecurityID(),
                    order.getTraderID(),
                    order.getClOrdID(),
                    order.getOrigClOrdId(),
                    order.getOldQty(),
                    order.getOldPrice());
        }
    }

    @Override
    public void onMatchQuote(MatchQuoteEvent msg) {
        JDBCSecurity security = getSecurity(msg.getSecurityID());
        security.bidPrice = msg.getBidPrice();
        security.offerPrice = msg.getOfferPrice();
    }



    public static class JDBCSecurity {
        String name;
        Date maturityDate;
        long coupon;
        long bidPrice;
        long offerPrice;
        public String cusip;

        public JDBCSecurity(Bond security) {
            this.name=security.getName();
            this.coupon=security.getCoupon();
            this.cusip=security.getCUSIP();

        }

        public String getName() {
            return name;
        }

        public String getCusip() {
            return cusip;
        }


        public Date getMaturityDate() {
            return maturityDate;
        }

        public long getCoupon() {
            return coupon;
        }

        public long getBidPrice() {
            return bidPrice;
        }

        public long getOfferPrice() {
            return offerPrice;
        }
    }

    public static class CommonContributor {
        String name;

        public String getName() {
            return name;
        }
    }


    public JDBCSecurity getSecurity(int id) {
        Bond security= securities.getBond((short) id);
        if(security==null){
            return null;
        }
        return new JDBCSecurity(security);

    }

    public Trader getTrader(short id )
    {
       return traders.get(id);
    }
    public Account getAccount(int id) {
        return accounts.get(id);

    }

    public CommonContributor getContributor(int id) {
        return id > 0 && id <= contributors.size() ? contributors.get(id - 1) : null;
    }

    public JDBCOrder getOrder(int id) {
        JDBCOrder order = orders.get(id);

        if ( order == null && lastOrder != null && lastOrder.getID() == id) {
            order = lastOrder;
        }

        return order;
    }
}
