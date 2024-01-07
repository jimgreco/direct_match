package com.core.match.ouch.msgs;

public interface OUCHReplaceCommand extends OUCHCommonCommand {
    void copy(OUCHReplaceEvent cmd);
    OUCHReplaceEvent toEvent();

    void setNewClOrdID(long val);

    void setNewQty(int val);

    void setNewPrice(long val);
    void setNewPrice(double val);

    void setNewMaxDisplayedQty(int val);
}
