package com.core.match.services.order;


public class DisplayedOrder extends AbstractOrder<DisplayedOrder> implements DisplayedOrderAttributes {
    private int externalOrderID;
    private boolean inBook;

    public DisplayedOrder() {
    }

    public DisplayedOrder copy() {
        DisplayedOrder displayedOrder = new DisplayedOrder();
        displayedOrder.id = id;
        displayedOrder.buy = buy;
        displayedOrder.qty = qty;
        displayedOrder.price = price;
        displayedOrder.securityID = securityID;
        displayedOrder.traderID = traderID;
        displayedOrder.inBook = inBook;
        displayedOrder.addCumQty(getCumQty());
        displayedOrder.externalOrderID = externalOrderID;
        return displayedOrder;
    }

    @Override
    public int getExternalOrderID() {
        return externalOrderID;
    }

    public void setExternalOrderID(int externalOrderID) {
        this.externalOrderID = externalOrderID;
    }

    @Override
    public void clear() {
        super.clear();
        externalOrderID = 0;
        inBook = false;
    }

    @Override
    public boolean isInBook() {
        return inBook;
    }

    @Override
    public void setInBook(boolean inBook) {
        this.inBook = inBook;
    }
}
