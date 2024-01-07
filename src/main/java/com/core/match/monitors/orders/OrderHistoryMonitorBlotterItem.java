package com.core.match.monitors.orders;

/**
 * Created by jgreco on 12/14/16.
 */
public class OrderHistoryMonitorBlotterItem {
    private final int id;
    private final String clordid;
    private final String account;
    private final String trader;
    private final boolean buy;
    private final int qty;
    private final String security;
    private final double price;
    private final String price32;
    private final int matchID;
    private final String timestamp;
    private final boolean aggress;

    public OrderHistoryMonitorBlotterItem(int id, String clOrdID, String account, String trader, boolean buy, int qty, String security, double price, String price32, int matchID, String timestamp, boolean aggressor) {
        this.id = id;
        this.clordid = clOrdID;
        this.account = account;
        this.trader = trader;
        this.buy = buy;
        this.qty = qty;
        this.security = security;
        this.price = price;
        this.price32 = price32;
        this.matchID = matchID;
        this.timestamp = timestamp;
        this.aggress = aggressor;
    }

    public String getAccount() {
        return account;
    }

    public String getTrader() {
        return trader;
    }

    public boolean isBuy() {
        return buy;
    }

    public int getQty() {
        return qty;
    }

    public String getSecurity() {
        return security;
    }

    public double getPrice() {
        return price;
    }

    public String getPrice32() {
        return price32;
    }

    public int getMatchID() {
        return matchID;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getID() {
        return id;
    }

    public String getClOrdID() {
        return clordid;
    }

    public boolean isAggress() {
        return aggress;
    }
}