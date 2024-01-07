package com.core.match.ouch2.msgs;

public interface OUCH2ReplaceCommand extends OUCH2CommonCommand {
    void copy(OUCH2ReplaceEvent cmd);
    OUCH2ReplaceEvent toEvent();

    void setNewClOrdID(long val);

    void setNewQty(int val);

    void setNewPrice(long val);
    void setNewPrice(double val);

    void setReserved(int val);
}
