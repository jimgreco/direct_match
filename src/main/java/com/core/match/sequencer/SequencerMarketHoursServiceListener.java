package com.core.match.sequencer;

/**
 * Created by jgreco on 6/27/15.
 */
interface SequencerMarketHoursServiceListener {
    boolean onOpen();
    boolean onClose();
}
