package com.core.match.ouch.client;

public class OUCHClientFill {

    private final long matchId;
    private final long clOrdId;
    private final String security;
    private final int qty;
    private final long price;

    public OUCHClientFill(long matchId, long clOrdId, String security, int qty, long price) {
        this.matchId = matchId;
        this.clOrdId = clOrdId;
        this.security = security;
        this.qty = qty;
        this.price = price;
    }

    public long getMatchId() {
        return matchId;
    }

    public long getClOrdId() {
        return clOrdId;
    }

    public String getSecurity() {
        return security;
    }
    
    public int getQty() {
        return qty;
    }

    public long getPrice() {
        return price;
    }
}
