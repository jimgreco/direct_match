package com.core.match.sequencer;

import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchReplaceEvent;

/**
 * Created by jgreco on 7/21/15.
 */
interface SequencerBookService {
    short addBook();
    void setListener(SequencerBookServiceListener listener);

    SequencerOrder buildOrder(MatchOrderEvent msg);
    boolean buildReplace(SequencerOrder order, MatchReplaceEvent msg);

    void addOrder(SequencerOrder order);
    void cancelOrder(SequencerOrder order);
    void replaceOrder(SequencerOrder order, boolean reinsert);

    SequencerOrder getOrder(int id);
    void deleteOrder(SequencerOrder order);
    long numOrders();
}
