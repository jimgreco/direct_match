package com.core.match;

import com.core.app.Application;
import com.core.app.UniversalApplication;
import com.core.match.msgs.MatchCommonCommand;
import com.core.util.log.Log;

/**
 * Created by jgreco on 12/22/14.
 */
public abstract class MatchApplication extends UniversalApplication implements Application {
    private final MatchCommandSender sender;

    public MatchApplication(Log log) {
        this(log, null);
    }

    public MatchApplication(Log log, MatchCommandSender sender) {
        super(log, sender);
        this.sender = sender;
    }
    
    protected void send(MatchCommonCommand cmd) {
        sender.send(cmd);
    }

    protected void add(MatchCommonCommand cmd) {
        sender.add(cmd);
    }

    protected MatchCommandSender getSender() {
        return sender;
    }
}
