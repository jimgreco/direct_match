package com.core.match.services.order;

import com.core.match.msgs.MatchOrderEvent;

/**
 * Created by jgreco on 1/3/15.
 */
public interface IsInterestedListener {
    boolean isInterested(MatchOrderEvent msg);
}
