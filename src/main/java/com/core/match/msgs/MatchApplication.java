package com.core.match.msgs;

import com.core.app.Application;
import com.core.app.UniversalApplication;
import com.core.util.log.Log;

/**
 * User: jgreco
 * THIS FILE IS AUTOGENERATED
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
