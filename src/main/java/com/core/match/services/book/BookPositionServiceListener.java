package com.core.match.services.book;

import com.core.match.services.security.BaseSecurity;

/**
 * Created by jgreco on 2/23/16.
 */
public interface BookPositionServiceListener<T> {
    void onBookDefined(BaseSecurity security, int levels);
    void onOrderChange(int position, T order, long timestamp);
    void onNoOrder(int qspot, boolean buy, short securityID, long timestamp);
}
