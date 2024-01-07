package com.core.connector;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public interface CommandSender {
    String getName();

    short getContribID();
    void setContribID(short contributorID);
    String getDestinationAddress();

    String getSession();

    int getLastSeenContribSeqNum();
    int getContribSeqNum();
    int incContribSeqNum();

    boolean canSend();
    boolean isActive();
    boolean isCaughtUp();
    boolean canWrite();

    void setActive();
    void setPassive();

    void init();
    void add(ByteBuffer buffer);
    boolean send();

    void onMyMessage(int contributorSeq);

    void addAllCommandsClearedListener(AllCommandsClearedListener listener);
    void addContributorDefinedListener(ContributorDefinedListener listener);
}
