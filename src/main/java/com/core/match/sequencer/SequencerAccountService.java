package com.core.match.sequencer;

import com.core.match.msgs.MatchAccountEvent;
import com.core.match.msgs.MatchAccountListener;
import com.core.sequencer.CoreSequencerBaseService;
import com.core.util.log.Log;

/**
 * Created by jgreco on 1/2/15.
 */
class SequencerAccountService extends CoreSequencerBaseService implements MatchAccountListener {
    private final Log log;

    public SequencerAccountService(Log log) {
        super(log, "Accounts");

        this.log = log;
    }

    public short addAccount(String name) {
        return (short)super.add(name);
    }

    @Override
    public void onMatchAccount(MatchAccountEvent msg) {
        short id = msg.getAccountID();
        int index = getIndex(id);

        if (isValid(id)) {
            // nothing to update
        }
        else if (index != size()) {
            // index must be one greater than last
            log.error(log.log().add("Invalid AccountID. Expected=").add(size() + 1)
                    .add(", Recv=").add(msg.getAccountID()));
        }
        else {
            // adding a new account
            addAccount(msg.getNameAsString());
        }
    }
}
