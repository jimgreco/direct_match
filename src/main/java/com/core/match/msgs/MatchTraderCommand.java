package com.core.match.msgs;

public interface MatchTraderCommand extends com.core.match.msgs.MatchCommonCommand {
    void copy(MatchTraderEvent cmd);
    MatchTraderEvent toEvent();

    void setTraderID(short val);

    void setAccountID(short val);

    void setName(java.nio.ByteBuffer val);
    void setName(String val);

    void setFatFinger2YLimit(int val);

    void setFatFinger3YLimit(int val);

    void setFatFinger5YLimit(int val);

    void setFatFinger7YLimit(int val);

    void setFatFinger10YLimit(int val);

    void setFatFinger30YLimit(int val);
}
