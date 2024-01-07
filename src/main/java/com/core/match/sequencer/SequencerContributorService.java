package com.core.match.sequencer;

import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchContributorEvent;
import com.core.match.msgs.MatchContributorListener;
import com.core.sequencer.CoreSequencerContributorService;
import com.core.util.log.Log;

/**
 * Created by jgreco on 1/2/15.
 */
class SequencerContributorService extends CoreSequencerContributorService implements MatchContributorListener {
    public SequencerContributorService(Log log) {
        super(log);
    }

    @Override
    public void onMatchContributor(MatchContributorEvent msg) {
        short id = msg.getSourceContributorID();
        int index = getIndex(id);
        if (isValid(id)) {
            // updating an old
        }
        else if (index != size()) {
            // can only add one to the end
            log.error(log.log().add("Invalid SourceContributorID. Expected=").add(size() + MatchConstants.STATICS_START_INDEX)
                    .add(", Recv=").add(id));
        }
        else {
            // add a new contributor
            addContributor(msg.getNameAsString());
        }
    }
}
