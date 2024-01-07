package com.core.match.ouch2.msgs;

public interface OUCH2CommonCommand extends com.core.connector.CommonCommand {

    void setMsgType(char val);

    void setTimestamp(long val);

    void setClOrdID(long val);
}
