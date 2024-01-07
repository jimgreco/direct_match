package com.core.match.services.order;

import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchReplaceEvent;

/**
 * User: jgreco
 */
public interface OrderServiceListener<T extends Order<T>> extends IsInterestedListener {
    void onOrder(T order, MatchOrderEvent msg);
    void onCancel(T order, MatchCancelEvent msg);
    void onReplace(T order, MatchReplaceEvent msg, ReplaceUpdates updates);
    void onFill(T order, MatchFillEvent msg);
}
