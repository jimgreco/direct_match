package com.core.match.ouch.msgs;

public interface OUCHReplacedCommand extends OUCHCommonCommand {
    void copy(OUCHReplacedEvent cmd);
    OUCHReplacedEvent toEvent();

    void setOldClOrdId(long val);

    void setQty(int val);

    void setPrice(long val);
    void setPrice(double val);

    void setMaxDisplayedQty(int val);
}
