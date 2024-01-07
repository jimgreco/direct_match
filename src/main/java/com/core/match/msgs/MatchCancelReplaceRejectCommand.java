package com.core.match.msgs;

public interface MatchCancelReplaceRejectCommand extends com.core.match.msgs.MatchCommonCommand {
    void copy(MatchCancelReplaceRejectEvent cmd);
    MatchCancelReplaceRejectEvent toEvent();

    void setOrderID(int val);

    void setClOrdID(java.nio.ByteBuffer val);
    void setClOrdID(String val);

    void setOrigClOrdID(java.nio.ByteBuffer val);
    void setOrigClOrdID(String val);

    void setText(java.nio.ByteBuffer val);
    void setText(String val);

    void setIsReplace(boolean val);

    void setReason(char val);
}
