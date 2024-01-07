package com.core.match;

import com.core.connector.CommandSender;
import com.core.match.msgs.MatchCommonCommand;
import com.core.match.msgs.MatchContributorListener;

/**
 * Created by jgreco on 6/19/15.
 */
public interface MatchCommandSender extends CommandSender, MatchContributorListener {
    boolean send(MatchCommonCommand command);
    void add(MatchCommonCommand command);

}
