package com.core.match.sequencer;

import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchOrderEvent;
import com.core.match.msgs.MatchReplaceEvent;
import com.core.sequencer.BackupQueueListener;

/**
 * Created by hli on 5/11/16.
 */
public interface BackupEventQueueController {
    void verifyOrderEvent(MatchOrderEvent receivedEvent);
    void verifyReplaceEvent(MatchReplaceEvent receivedEvent);
    void verifyCancelEvent(MatchCancelEvent receivedEvent);
    void verifyFillEvent(MatchFillEvent receivedEvent);
    void sendRemaining();
    public void addBackupQueueListener(BackupQueueListener listener);
}
