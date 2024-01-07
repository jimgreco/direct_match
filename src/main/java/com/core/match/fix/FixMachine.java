package com.core.match.fix;

import com.core.connector.ContributorDefinedListener;
import com.core.fix.msgs.*;
import com.core.fix.util.HeartbeatSessionListener;
import com.core.match.msgs.MatchInboundListener;
import com.core.match.msgs.MatchOutboundListener;

/**
 * Created by hli on 5/3/16.
 */
public interface FixMachine extends HeartbeatSessionListener,
        FixHeartbeatListener,
        FixLogoutListener,
        FixRejectListener,
        FixResendRequestListener,
        FixTestRequestListener,
        FixBusinessRejectListener,
        MatchInboundListener,
        MatchOutboundListener,
        ContributorDefinedListener {
    boolean isResending();
    void reset();
    FixStateMachine.FixMessageResult onFixMessage();
}
