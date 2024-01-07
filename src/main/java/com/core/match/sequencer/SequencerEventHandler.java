package com.core.match.sequencer;

import com.core.match.msgs.MatchBaseDispatcher;
import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchCancelListener;
import com.core.match.msgs.MatchCommonEvent;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchFillListener;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchOrderListener;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.match.msgs.MatchReplaceListener;
import com.core.util.log.Log;
import com.core.util.store.IndexedStore;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 7/26/15.
 */
class SequencerEventHandler implements
        MatchBaseDispatcher.MatchBeforeListener,
        MatchBaseDispatcher.MatchAfterListener,
        MatchOrderListener,
        MatchReplaceListener,
        MatchCancelListener,
        MatchFillListener
{
    private final Log log;
    private final SequencerBookService books;
    private final SequencerContributorService contributors;
    private final IndexedStore store;
    private final OrderCommandHandler backupEventHandler;
    private final BackupEventQueueController eventQueueController;

    public SequencerEventHandler(Log log,
                                 IndexedStore store,
                                 SequencerBookService books,
                                 SequencerContributorService contributors, OrderCommandHandler backupEventHandler, BackupEventQueueController backupEventQueueController) {
        this.log = log;
        this.books = books;
        this.contributors = contributors;
        this.store = store;
        this.backupEventHandler =backupEventHandler;
        this.eventQueueController = backupEventQueueController;


    }

    @Override
    public void onMatchOrder(MatchOrderEvent msg) {
        backupEventHandler.onMatchOrder(msg);
        eventQueueController.verifyOrderEvent(msg);
    }

    @Override
    public void onMatchReplace(MatchReplaceEvent msg) {

        backupEventHandler.onMatchReplace(msg);
        eventQueueController.verifyReplaceEvent(msg);
    }

    @Override
    public void onMatchCancel(MatchCancelEvent msg) {
        backupEventHandler.onMatchCancel(msg);
        eventQueueController.verifyCancelEvent(msg);
    }

    @Override
    public void onMatchFill(MatchFillEvent msg) {
        eventQueueController.verifyFillEvent(msg);
    }

    // TODO: Should be before it's even a message
    @Override
    public void onMatchBeforeListener(MatchCommonEvent msg) {
        ByteBuffer buffer = msg.getRawBuffer();
        int position = buffer.position();
        int limit = position + buffer.remaining();

        // make sure we pickup the short length
        buffer.limit(limit);
        buffer.position(position);

        store.add(buffer);

        // reset to the normal buffer position
        buffer.position(position);
        buffer.limit(limit);
    }

    @Override
    public void onMatchAfterListener(MatchCommonEvent msg) {
        contributors.setSeqNum(msg.getContributorID(), msg.getContributorSeq());
    }
}
