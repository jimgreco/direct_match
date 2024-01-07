package com.core.match.services.order;

import com.core.match.msgs.MatchCancelReplaceRejectEvent;
import com.core.match.msgs.MatchClientCancelReplaceRejectEvent;
import com.core.match.msgs.MatchClientOrderRejectEvent;
import com.core.match.msgs.MatchOrderRejectEvent;

/**
 * Created by jgreco on 1/3/15.
 */
public interface OrderServiceRejectListener<T extends Order<T>> {
    void onOrderReject(MatchOrderRejectEvent msg);
    void onClientOrderReject(MatchClientOrderRejectEvent msg);
    void onCancelReplaceReject(T order, MatchCancelReplaceRejectEvent msg);
    void onClientCancelReplaceReject(T order, MatchClientCancelReplaceRejectEvent msg);
}
