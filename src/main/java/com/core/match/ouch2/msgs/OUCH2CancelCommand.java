package com.core.match.ouch2.msgs;

public interface OUCH2CancelCommand extends OUCH2CommonCommand {
    void copy(OUCH2CancelEvent cmd);
    OUCH2CancelEvent toEvent();
}
