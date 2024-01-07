package com.core.match.msgs;

public interface MatchAccountCommand extends com.core.match.msgs.MatchCommonCommand {
    void copy(MatchAccountEvent cmd);
    MatchAccountEvent toEvent();

    void setAccountID(short val);

    void setName(java.nio.ByteBuffer val);
    void setName(String val);

    void setNetDV01Limit(int val);

    void setCommission(long val);
    void setCommission(double val);

    void setSSGMID(java.nio.ByteBuffer val);
    void setSSGMID(String val);

    void setNettingClearing(boolean val);
}
