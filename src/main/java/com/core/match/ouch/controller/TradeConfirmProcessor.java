package com.core.match.ouch.controller;

import com.core.match.STPHolder;
import com.core.match.ouch.OUCHOrder;
import com.core.match.services.security.BaseSecurity;

public interface TradeConfirmProcessor {
    void sendTradeConfirmation(long timestamp, int matchID, STPHolder<OUCHOrder> holder, BaseSecurity sec);
}
