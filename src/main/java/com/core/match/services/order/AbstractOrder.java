package com.core.match.services.order;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by jgreco on 8/11/15.
 */
public class AbstractOrder<T> implements Order<T> {
    int id;
    boolean buy;
    long price;
    int qty;
    short securityID;
    short traderID;

    private int cumQty;

    protected T next;

    public AbstractOrder() {
    }

    public AbstractOrder(int id, boolean buy, long price, int qty, int cumQty, short securityID, short traderID) {
        this.id = id;
        this.buy = buy;
        this.price = price;
        this.qty = qty;
        this.cumQty = cumQty;
        this.securityID = securityID;
        this.traderID = traderID;
    }

    @Override
	public void addCumQty(int qty) {
        cumQty += qty;
    }

    @Override
	@JsonIgnore
    public int getRemainingQty() {
        return getQty() - getCumQty();
    }

    @Override
	@JsonIgnore
    public boolean isFilled() {
        return getCumQty() >= getQty();
    }

    @Override
    public void setNext(T next) {
        this.next = next;
    }

    @Override
    public T next() {
        return next;
    }

    @Override
    public void clear() {
        setNext(null);
        id = 0;
        buy = false;
        price = 0;
        qty = 0;
        cumQty = 0;
        securityID = 0;
        traderID = 0;
    }

    @Override
	public int getID() {
        return id;
    }

    @Override
	public boolean isBuy() {
        return buy;
    }

    @Override
	public long getPrice() {
        return price;
    }

    @Override
	public int getQty() {
        return qty;
    }

    @Override
	public int getCumQty() {
        return cumQty;
    }

    @Override
	public short getSecurityID() {
        return securityID;
    }

    @Override
	public short getTraderID() {
        return traderID;
    }

    @Override
    public void setID(int id) {
        this.id = id;
    }

    @Override
    public void setBuy(boolean buy) {
        this.buy = buy;
    }

    @Override
    public void setPrice(long price) {
        this.price = price;
    }

    @Override
    public void setQty(int qty) {
        this.qty = qty;
    }

    @Override
    public void setSecurityID(short securityID) {
        this.securityID = securityID;
    }

    @Override
    public void setTraderID(short traderID) {
        this.traderID = traderID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getID());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        return ((Order<?>)obj).getID() == getID();
    }


}
