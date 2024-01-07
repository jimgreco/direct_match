package com.core.match.ouch2.msgs;

public interface OUCH2ReplacedCommand extends OUCH2CommonCommand {
    void copy(OUCH2ReplacedEvent cmd);
    OUCH2ReplacedEvent toEvent();

    void setOldClOrdId(long val);

    void setQty(int val);

    void setPrice(long val);
    void setPrice(double val);

    void setReserved(int val);

    void setExternalOrderID(int val);
}
