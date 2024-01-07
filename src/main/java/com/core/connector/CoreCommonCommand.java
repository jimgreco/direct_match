package com.core.connector;

/**
 * Created by jgreco on 10/16/16.
 */
public interface CoreCommonCommand extends CommonCommand {
    java.nio.ByteBuffer getRawBuffer();

    int getLength();
    void setLength(int length);

    void setMsgType(char val);

    void setContributorID(short val);

    void setContributorSeq(int val);

    void setTimestamp(long val);
}
