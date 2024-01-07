package com.core.connector.soup.msgs;

public interface SoupCommonCommand extends com.core.connector.CommonCommand {

    void setMsgLength(short val);

    void setMsgType(char val);
}
