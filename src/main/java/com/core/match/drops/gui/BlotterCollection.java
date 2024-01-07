package com.core.match.drops.gui;

import com.core.match.drops.DropCollection;
import com.core.match.drops.LinearCounter;
import com.core.match.drops.gui.msgs.GUIBlotter;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.SecurityService;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.match.util.MatchPriceUtils;
import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;

/**
 * Created by jgreco on 2/21/16.
 */
class BlotterCollection extends DropCollection {
    private final IntObjectHashMap<GUIBlotter> orderIDBlotterMap;
    private final SecurityService<BaseSecurity> securities;
    private final TraderService<Trader> traders;
    private final AccountService<Account> accounts;
    private int rejectCount;

    public BlotterCollection(LinearCounter versionCounter,
                             LinearCounter itemCounter,
                             SecurityService<BaseSecurity> securities,
                             TraderService<Trader> traders,
                             AccountService<Account> accounts) {
        super(versionCounter, itemCounter);

        this.orderIDBlotterMap = new IntObjectHashMap<>();
        this.securities = securities;
        this.traders = traders;
        this.accounts = accounts;
    }

    public void order(BlotterDropOrder order) {
        BaseSecurity security = securities.get(order.getSecurityID());
        Trader trader = traders.get(order.getTraderID());
        Account account = trader != null ? accounts.get(trader.getAccountID()) : null;

        GUIBlotter bm = new GUIBlotter(
                order.getID(),
                account != null ? account.getName() : "",
                trader != null ? trader.getName() : "",
                security != null ? security.getName() : "",
                order.isBuy(),
                "",
                order.getCreated());

        bm.setClOrdID(order.getClOrdID());
        bm.setPrice(order.getPrice());
        bm.setPrice32(getPrice32(security, order.getPrice(), order.isBuy()));
        bm.setCumQty(order.getCumQty());
        bm.setNotional(order.getNotional());
        bm.setQty(order.getQty());
        bm.setUpdateType(GUIOrderStatus.New.name());

        orderIDBlotterMap.put(order.getID(), bm);
        addVersion(bm);
    }

    public void reject(String clOrdID,
                                String security,
                                boolean buy,
                                String account,
                                String trader,
                                long created,
                                String rejectText) {
        GUIBlotter bm = new GUIBlotter(
                --rejectCount,
                account,
                trader,
                security,
                buy,
                rejectText,
                created);

        bm.setClOrdID(clOrdID);
        bm.setUpdateType(GUIOrderStatus.Rejected.name());

        addVersion(bm);
    }

    public void cancel(BlotterDropOrder order) {
        GUIBlotter bm = orderIDBlotterMap.get(order.getID());
        if (bm == null) {
            return;
        }

        bm.setUpdateType(GUIOrderStatus.Canceled.name());
        bm.setUpdated(order.getUpdated());
        bm.setClOrdID(order.getClOrdID());

        updateVersion(bm);
    }

    public void replace(BlotterDropOrder order) {
        GUIBlotter bm = orderIDBlotterMap.get(order.getID());
        if (bm == null) {
            return;
        }
        BaseSecurity security = securities.get(order.getSecurityID());

        bm.setUpdateType(GUIOrderStatus.Replaced.name());
        bm.setUpdated(order.getUpdated());
        bm.setClOrdID(order.getClOrdID());
        bm.setPrice(order.getPrice());
        bm.setPrice32(getPrice32(security, order.getPrice(), order.isBuy()));
        bm.setQty(order.getQty());

        updateVersion(bm);
    }

    public void fill(BlotterDropOrder order, int qty, long price) {
        GUIBlotter bm = orderIDBlotterMap.get(order.getID());
        if (bm == null) {
            return;
        }

        bm.setUpdateType(order.getRemainingQty() > 0 ? GUIOrderStatus.PartiallyFilled.name() : GUIOrderStatus.Filled.name());
        bm.setUpdated(order.getUpdated());
        bm.setCumQty(order.getCumQty());
        bm.setNotional(order.getNotional());
        bm.setLastQty(qty);
        bm.setLastPrice(price);

        updateVersion(bm);
    }

    private long getPrice32(BaseSecurity security, long price, boolean buy) {
        if (security.isBond()) {
            if (((Bond) security).getTerm() < 6) {
                return MatchPriceUtils.roundQuarter(price, buy);
            }
            else {
                return MatchPriceUtils.roundHalf(price, buy);
            }
        }
        return price;
    }
}
