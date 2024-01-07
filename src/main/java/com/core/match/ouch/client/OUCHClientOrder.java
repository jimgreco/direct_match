package com.core.match.ouch.client;

import com.core.match.ouch.msgs.OUCHAcceptedEvent;
import com.core.match.ouch.msgs.OUCHConstants;

/**
 * Created by jgreco on 8/16/15.
 */
public class OUCHClientOrder {
    private final char side;
    private final String security;
    private final char tif;
    private final String trader;

    private long clOrdID;
    private int qty;
    private long price;
    private int cumQty;

    public OUCHClientOrder(long clOrdID, boolean buy, int qty, String security, long price) {
        this(clOrdID, buy ? OUCHConstants.Side.Buy : OUCHConstants.Side.Sell, qty, security, price, OUCHConstants.TimeInForce.DAY, "");
    }

    public OUCHClientOrder(OUCHAcceptedEvent msg) {
        this(msg.getClOrdID(), msg.getSide(), msg.getQty(), msg.getSecurityAsString(), msg.getPrice(), msg.getTimeInForce(), msg.getTraderAsString());
    }

    public OUCHClientOrder(long clOrdID, char side, int qty, String security, long price, char tif, String trader) {
        this.clOrdID = clOrdID;
        this.side = side;
        this.qty = qty;
        this.security = security;
        this.price = price;
        this.tif = tif;
        this.trader = trader;
    }

    public void replaced(long clOrdID, int qty, long price) {
        this.clOrdID = clOrdID;
        this.qty = qty;
        this.price = price;
    }

    public void fill(int qty) {
        this.cumQty += qty;
    }

    public boolean isBuy() {
        return OUCHConstants.Side.Buy == getSide();
    }

    public boolean isDayOrder() {
        return OUCHConstants.TimeInForce.DAY == getTIF();
    }

    public boolean isFilled() {
        return cumQty >= qty;
    }

    public char getSide() {
        return side;
    }

    public String getSecurity() {
        return security;
    }

    public char getTIF() {
        return tif;
    }

    public String getTrader() {
        return trader;
    }

    public int getRemainingQty() {
        return qty - cumQty;
    }

    public int getCumQty() {
        return cumQty;
    }

    public int getQty() {
        return qty;
    }

    public long getPrice() {
        return price;
    }

    public long getClOrdID() {
        return clOrdID;
    }
}
