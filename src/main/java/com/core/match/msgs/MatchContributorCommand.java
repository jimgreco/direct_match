package com.core.match.msgs;

public interface MatchContributorCommand extends com.core.match.msgs.MatchCommonCommand {
    void copy(MatchContributorEvent cmd);
    MatchContributorEvent toEvent();

    void setSourceContributorID(short val);

    void setName(java.nio.ByteBuffer val);
    void setName(String val);

    void setCancelOnDisconnect(boolean val);
}
