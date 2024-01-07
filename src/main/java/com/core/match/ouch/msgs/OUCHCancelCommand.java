package com.core.match.ouch.msgs;

public interface OUCHCancelCommand extends OUCHCommonCommand {
    void copy(OUCHCancelEvent cmd);
    OUCHCancelEvent toEvent();
}
