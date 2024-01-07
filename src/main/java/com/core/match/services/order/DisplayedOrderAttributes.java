package com.core.match.services.order;

/**
 * Created by jgreco on 10/12/15.
 */
public interface DisplayedOrderAttributes {
    int getExternalOrderID();
    void setExternalOrderID(int displayedOrderID);
    boolean isInBook();
    void setInBook(boolean displayed);
}
