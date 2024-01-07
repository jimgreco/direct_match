package com.core.match.services.order;

import com.core.util.datastructures.contracts.Linkable;
import com.core.util.pool.Poolable;

/**
 * User: jgreco
 */
public interface Order<T> extends Poolable<T> , Linkable<T> {
    // Order methods
    void addCumQty(int qty);
    int getCumQty();
    int getRemainingQty();
    boolean isFilled();

    // Order properties
    int getID();
    boolean isBuy();
    long getPrice();
    int getQty();
    short getSecurityID();
    short getTraderID();

    void setID(int id);
    void setBuy(boolean buy);
    void setPrice(long price);
    void setQty(int qty);
    void setSecurityID(short securityID);
    void setTraderID(short traderID);
}
