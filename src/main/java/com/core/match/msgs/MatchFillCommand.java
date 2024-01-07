package com.core.match.msgs;

public interface MatchFillCommand extends com.core.match.msgs.MatchCommonCommand {
    void copy(MatchFillEvent cmd);
    MatchFillEvent toEvent();

    void setOrderID(int val);

    void setQty(int val);

    void setPrice(long val);
    void setPrice(double val);

    void setMatchID(int val);

    void setLastFill(boolean val);

    void setPassive(boolean val);

    void setInBook(boolean val);
}
