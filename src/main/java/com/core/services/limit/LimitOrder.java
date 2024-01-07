package com.core.services.limit;

import com.core.util.list.IntrusiveDoublyLinkedListItem;

/**
 * Created by jgreco on 7/6/15.
 */
public interface LimitOrder<ORDER extends LimitOrder<ORDER>> extends IntrusiveDoublyLinkedListItem<ORDER> {
    boolean isBuy();
    long getPrice();
    int getRemainingQty();
}
