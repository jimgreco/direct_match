package com.core.match.monitors.md;

/**
 * Created by jgreco on 12/14/16.
 */
public class MarketDataRow {
    private final String field;
    private final int maturityDate;
    private final double coupon;
    String idcBid = "";
    String idcAsk = "";
    String dmBid = "";
    String dmAsk = "";
    double dmBidQty;
    double dmAskQty;

    public MarketDataRow(String field, double coupon, int maturityDate) {
        this.field = field;
        this.maturityDate = maturityDate;
        this.coupon = coupon;
    }

    public String getName() {
        return field;
    }

    public int getMaturityDate() {
        return maturityDate;
    }

    public double getCoupon() {
        return coupon;
    }

    public String getIdcBid() {
        return idcBid;
    }

    public String getIdcAsk() {
        return idcAsk;
    }

    public double getDmBidQty() {
        return dmBidQty;
    }

    public String getDmBid() {
        return dmBid;
    }

    public double getDmAskQty() {
        return dmAskQty;
    }

    public String getDmAsk() {
        return dmAsk;
    }
}