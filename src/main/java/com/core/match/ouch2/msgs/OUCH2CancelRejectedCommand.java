package com.core.match.ouch2.msgs;

public interface OUCH2CancelRejectedCommand extends OUCH2CommonCommand {
    void copy(OUCH2CancelRejectedEvent cmd);
    OUCH2CancelRejectedEvent toEvent();

    void setReason(char val);
}
