package com.core.match.monitors.orders;

import com.core.match.util.MatchPriceUtils;

/**
 * Created by jgreco on 12/7/15.
 */
public class OrderHistoryMonitorOrderItem {
    int id;
    long price;
    int qty;
    int cumQty;
    String clOrdID;
    String account;
    String trader;
    String security;
    String contributor;
    boolean buy;
    boolean ioc;
    long notional;
    String created;
    String updated;
    String status;

    public void clear() {
        id = 0;
        price = 0;
        qty = 0;
        cumQty = 0;
        clOrdID = null;
        account =null;
        trader = null;
        security = null;
        contributor = null;
        buy = false;
        ioc = false;
        notional = 0;
        created = null;
        updated = null;
    }

    public String getStatus() {
        return status;
    }

    public String getCreated() {
        return created != null ? created : "";
    }

    public String getUpdated() {
        return updated != null ? updated : "";
    }

    public int getID() {
        return id;
    }

    public String getPrice32() {
        return MatchPriceUtils.to32ndPrice(price);
    }

    public double getPrice() {
        return MatchPriceUtils.toDouble(price);
    }

    public double getQty() {
        return MatchPriceUtils.toQtyRoundLot(qty);
    }

    public double getCumQty() {
        return MatchPriceUtils.toQtyRoundLot(cumQty);
    }

    public String getClOrdID() {
        return clOrdID != null ? clOrdID : "";
    }

    public String getSecurity() {
        return security != null ? security : "";
    }

    public String getTrader() {
        return trader != null ? trader : "";
    }

    public String getAccount() {
        return account != null ? account : "";
    }

    public String getContributor() {
        return contributor != null ? contributor : "";
    }

    public String getSide() {
        return buy ? "BUY" : "SELL";
    }

    public String getTif() {
        return ioc ? "IOC" : "DAY";
    }

    public String getAvgPx32() {
        if (cumQty == 0) {
            return "0";
        }
        return MatchPriceUtils.to32ndPrice(notional / cumQty);
    }

    public double getAvgPx() {
        if (cumQty == 0) {
            return 0;
        }
        return MatchPriceUtils.toDouble(notional / cumQty);
    }
}
