package com.core.match.itch.client;

import com.core.match.itch.msgs.ITCHOrderCancelEvent;
import com.core.match.itch.msgs.ITCHOrderEvent;
import com.core.match.itch.msgs.ITCHOrderExecutedEvent;

/**
 * Created by jgreco on 7/6/15.
 */
public interface ITCHClientOrderServiceListener {
    void onITCHOrder(ITCHClientOrder order, ITCHOrderEvent msg);
    void onITCHOrderCancel(ITCHClientOrder order, ITCHOrderCancelEvent msg);
    void onITCHOrderExecuted(ITCHClientOrder order, ITCHOrderExecutedEvent msg);
}
