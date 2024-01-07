package com.core.match.ouch.msgs;

public interface OUCHFillCommand extends OUCHCommonCommand {
    void copy(OUCHFillEvent cmd);
    OUCHFillEvent toEvent();

    void setExecutionQty(int val);

    void setExecutionPrice(long val);
    void setExecutionPrice(double val);

    void setMatchID(int val);
}
