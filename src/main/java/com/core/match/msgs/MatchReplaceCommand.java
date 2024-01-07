package com.core.match.msgs;

public interface MatchReplaceCommand extends com.core.match.msgs.MatchCommonCommand {
    void copy(MatchReplaceEvent cmd);
    MatchReplaceEvent toEvent();

    void setOrderID(int val);

    void setQty(int val);

    void setPrice(long val);
    void setPrice(double val);

    void setClOrdID(java.nio.ByteBuffer val);
    void setClOrdID(String val);

    void setOrigClOrdID(java.nio.ByteBuffer val);
    void setOrigClOrdID(String val);

    void setExternalOrderID(int val);

    void setInBook(boolean val);
}
