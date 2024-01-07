package com.core.match.services.trader;

import com.core.match.msgs.MatchTraderEvent;

/**
 * Created by johnlevidy on 5/26/15.
 */
public interface TraderServiceListener<T> {
    void onTrader(T trader, MatchTraderEvent msg, boolean isNew);
}
