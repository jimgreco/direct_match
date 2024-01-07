package com.core.match.itch.msgs;

public interface ITCHCommonCommand extends com.core.connector.CommonCommand {

    void setMsgType(char val);

    void setSecurityID(short val);

    void setTimestamp(long val);
}
