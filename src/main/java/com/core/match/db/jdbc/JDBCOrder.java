package com.core.match.db.jdbc;


import com.core.match.services.order.AbstractOrder;

import java.nio.ByteBuffer;

/**
 * Created by hli on 9/30/15.
 */
public class JDBCOrder extends AbstractOrder<JDBCOrder> {
    private String clOrdId;
    private String origClOrdId;
    private int oldQty;
    private long oldPrice;

    public JDBCOrder(){
        super();
    }

    public JDBCOrder(int id,
                     boolean buy,
                     long price,
                     int qty,
                     int cumQty,
                     short securityID,
                     short traderID,
                     String clOrdId,
                     String origClOrdId,
                     int oldQty,
                     long oldPrice) {
        super(id, buy, price, qty, cumQty, securityID, traderID);

        this.clOrdId = clOrdId;
        this.origClOrdId = origClOrdId;
        this.oldQty = oldQty;
        this.oldPrice = oldPrice;
    }

    @Override
    public void clear() {
        super.clear();

        setClOrdId(null);
        setOrigClOrdId(null);
        setOldQty(0);
        setOldPrice(0);
    }

    public JDBCOrder copy() {
        JDBCOrder jdbcOrder = new JDBCOrder();
        jdbcOrder.setID(getID());
        jdbcOrder.setBuy(isBuy());
        jdbcOrder.setQty(getQty());
        jdbcOrder.setPrice(getPrice());
        jdbcOrder.setSecurityID(getSecurityID());
        jdbcOrder.setTraderID(getTraderID());
        jdbcOrder.addCumQty(getCumQty());
        jdbcOrder.clOrdId = getClOrdID();
        jdbcOrder.origClOrdId = getOrigClOrdId();
        jdbcOrder.oldQty = getOldQty();
        jdbcOrder.oldPrice = getOldPrice();
        return jdbcOrder;
    }

	public String getClOrdID() {
        return clOrdId;
    }

	public void setClOrdId(ByteBuffer clOrdId) {
	    this.clOrdId = SQLUtils.getClOrdIdAsString(clOrdId);
	}

	public String getOrigClOrdId() {
        return origClOrdId;
    }

    public void setOrigClOrdId(ByteBuffer origClOrdId) {
        this.origClOrdId = SQLUtils.getClOrdIdAsString(origClOrdId);
    }

	public int getOldQty() {
        return oldQty;
    }

	public void setOldQty(int oldQty) {
        this.oldQty = oldQty;
    }

	public long getOldPrice() {
        return oldPrice;
    }

	public void setOldPrice(long oldPrice) {
        this.oldPrice = oldPrice;
    }
}