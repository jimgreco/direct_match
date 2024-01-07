package com.core.match.ouch2.msgs;

public interface OUCH2RejectedCommand extends OUCH2CommonCommand {
    void copy(OUCH2RejectedEvent cmd);
    OUCH2RejectedEvent toEvent();

    void setReason(char val);
}
