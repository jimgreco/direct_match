package com.core.connector;

/**
 * Created by jgreco on 10/16/16.
 */
public interface CoreCommonEvent extends CommonEvent {
    java.nio.ByteBuffer getRawBuffer();

    String getMsgName();

    char getMsgType();
    boolean hasMsgType();

    short getContributorID();
    boolean hasContributorID();

    int getContributorSeq();
    boolean hasContributorSeq();

    long getTimestamp();
    java.time.LocalDateTime getTimestampAsTime();
    boolean hasTimestamp();
}
