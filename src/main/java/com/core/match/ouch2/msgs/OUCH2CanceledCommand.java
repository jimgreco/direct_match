package com.core.match.ouch2.msgs;

public interface OUCH2CanceledCommand extends OUCH2CommonCommand {
    void copy(OUCH2CanceledEvent cmd);
    OUCH2CanceledEvent toEvent();

    void setReason(char val);

    void setCanceledQty(int val);
}
