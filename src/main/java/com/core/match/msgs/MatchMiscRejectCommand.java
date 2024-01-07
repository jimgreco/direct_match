package com.core.match.msgs;

public interface MatchMiscRejectCommand extends com.core.match.msgs.MatchCommonCommand {
    void copy(MatchMiscRejectEvent cmd);
    MatchMiscRejectEvent toEvent();

    void setRejectedMsgType(char val);

    void setRejectReason(char val);
}
