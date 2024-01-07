package com.core.match.ouch2.msgs;

public interface OUCH2FillCommand extends OUCH2CommonCommand {
    void copy(OUCH2FillEvent cmd);
    OUCH2FillEvent toEvent();

    void setExecutionQty(int val);

    void setExecutionPrice(long val);
    void setExecutionPrice(double val);

    void setMatchID(int val);
}
