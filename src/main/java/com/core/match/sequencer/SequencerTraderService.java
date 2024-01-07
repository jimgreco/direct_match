package com.core.match.sequencer;

import com.core.match.msgs.MatchTraderEvent;
import com.core.match.msgs.MatchTraderListener;
import com.core.sequencer.CoreSequencerBaseService;
import com.core.util.log.Log;
import com.gs.collections.impl.list.mutable.primitive.ShortArrayList;

/**
 * Created by jgreco on 1/2/15.
 */
class SequencerTraderService extends CoreSequencerBaseService implements MatchTraderListener {
    private ShortArrayList accountIDs = new ShortArrayList();

    public SequencerTraderService(Log log) {
        super(log, "Traders");
    }

    public short addTrader(String name, short accountID) {
        accountIDs.add(accountID);
        return (short)add(name);
    }

    public short getAccountID(int traderID) {
        if (!isValid(traderID)) {
            return 0;
        }
        return accountIDs.get(getIndex(traderID));
    }

    @Override
    public void onMatchTrader(MatchTraderEvent msg) {
        short id = msg.getTraderID();
        short accountID = msg.getAccountID();
        int index = getIndex(id);

        if (isValid(id)) {
            accountIDs.set(index, accountID);
        }
        else if (index != size()) {
            log.error(log.log().add("Invalid TraderID. Expected=").add(size() + 1)
                    .add(", Recv=").add(msg.getAccountID()));
        }
        else {
            addTrader(msg.getNameAsString(), accountID);
        }
    }
}
