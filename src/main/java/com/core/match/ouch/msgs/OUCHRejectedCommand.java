package com.core.match.ouch.msgs;

public interface OUCHRejectedCommand extends OUCHCommonCommand {
    void copy(OUCHRejectedEvent cmd);
    OUCHRejectedEvent toEvent();

    void setReason(char val);
}
