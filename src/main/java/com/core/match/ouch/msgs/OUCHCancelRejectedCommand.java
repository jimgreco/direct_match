package com.core.match.ouch.msgs;

public interface OUCHCancelRejectedCommand extends OUCHCommonCommand {
    void copy(OUCHCancelRejectedEvent cmd);
    OUCHCancelRejectedEvent toEvent();

    void setReason(char val);
}
