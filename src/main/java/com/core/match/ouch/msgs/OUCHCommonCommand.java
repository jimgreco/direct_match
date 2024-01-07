package com.core.match.ouch.msgs;

public interface OUCHCommonCommand extends com.core.connector.CommonCommand {

    void setMsgType(char val);

    void setClOrdID(long val);
}
