package com.core.match.sequencer;

/**
 * Created by jgreco on 7/19/15.
 */
interface SequencerBookServiceListener {
    void onMatch(int matchID,
                 int restingOrderID,
                 int aggressiveOrderID,
                 int qty,
                 long price,
                 int fillCount,
                 boolean lastFill,
                 boolean restingInBook,
                 boolean aggressiveInBook);
}
