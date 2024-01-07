package com.core.match.ouch.client;

import com.core.match.ouch.msgs.OUCHAcceptedEvent;
import com.core.match.ouch.msgs.OUCHCancelRejectedEvent;
import com.core.match.ouch.msgs.OUCHCanceledEvent;
import com.core.match.ouch.msgs.OUCHFillEvent;
import com.core.match.ouch.msgs.OUCHRejectedEvent;
import com.core.match.ouch.msgs.OUCHReplacedEvent;
import com.core.match.ouch.msgs.OUCHTradeConfirmationEvent;

/**
 * Created by jgreco on 8/16/15.
 */
public interface OUCHClientOrderServiceListener {
    void onOUCHAccepted(OUCHAcceptedEvent msg, OUCHClientOrder order);
    void onOUCHReplaced(OUCHReplacedEvent msg, OUCHClientOrder order);
    void onOUCHCanceled(OUCHCanceledEvent msg, OUCHClientOrder order);
    void onOUCHCancelRejected(OUCHCancelRejectedEvent msg, OUCHClientOrder order);
    void onOUCHRejected(OUCHRejectedEvent msg);
    void onOUCHFill(OUCHFillEvent msg, OUCHClientOrder order);
    void onOUCHTradeConfirm(OUCHTradeConfirmationEvent msg);

}
