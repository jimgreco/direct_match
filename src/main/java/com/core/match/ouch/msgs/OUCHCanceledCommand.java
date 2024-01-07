package com.core.match.ouch.msgs;

public interface OUCHCanceledCommand extends OUCHCommonCommand {
    void copy(OUCHCanceledEvent cmd);
    OUCHCanceledEvent toEvent();

    void setReason(char val);
}
