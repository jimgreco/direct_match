package com.core.app;

import com.core.connector.AllCommandsClearedListener;
import com.core.connector.CommandSender;
import com.core.connector.ContributorDefinedListener;
import com.core.util.log.Log;
import com.core.util.time.TimeSource;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class ByteBufferCommandSender implements CommandSender {
    protected final TimeSource time;
    protected final CommandSender sender;
    protected final Log log;

    public ByteBufferCommandSender(Log log, TimeSource timeSource, CommandSender sender) {
        this.sender = sender;
        this.time = timeSource;
        this.log = log;
    }

    @Override
    public void onMyMessage(int contributorSeq) {
        sender.onMyMessage(contributorSeq);
    }

    @Override
    public void addContributorDefinedListener(ContributorDefinedListener listener) {
        sender.addContributorDefinedListener(listener);
    }

    @Override
    public boolean isActive() {
        return sender.isActive();
    }

    @Override
    public void setActive() {
        sender.setActive();
    }

    @Override
    public void setPassive() {
        sender.setPassive();
    }

    @Override
    public short getContribID() {
        return sender.getContribID();
    }

    @Override
    public void setContribID(short contributorID) {
        sender.setContribID(contributorID);
    }

    @Override
    public String getDestinationAddress() {
        return sender.getDestinationAddress();
    }

    @Override
    public String getSession() {
        return sender.getSession();
    }

    @Override
    public int getContribSeqNum() {
        return sender.getContribSeqNum();
    }

    @Override
    public int incContribSeqNum() {
        return sender.incContribSeqNum();
    }

    @Override
    public int getLastSeenContribSeqNum() {
        return sender.getLastSeenContribSeqNum();
    }

    @Override
    public boolean canSend() {
        return sender.canSend();
    }

    @Override
    public boolean isCaughtUp() {
        return sender.isCaughtUp();
    }

    @Override
    public boolean canWrite() {
        return sender.canWrite();
    }

    @Override
    public void addAllCommandsClearedListener(AllCommandsClearedListener listener) {
        sender.addAllCommandsClearedListener(listener);
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public void init() {
        sender.init();
    }

    @Override
    public boolean send() {
        if (!sender.canSend()) {
            log.error(log.log().add("NO TX. Cannot send message. CanWrite=").add(sender.canWrite())
                    .add(",IsActive=").add(sender.isActive())
                    .add(",IsCaughtUp=").add(sender.isCaughtUp()));

            return false;
        }
        if(!sender.isActive()){
            log.error(log.log().add("NO TX. Cannot send message because application is inactive"));
            return false;
        }

        if (log.isDebugEnabled()) {
            log.debug(log.log().add("TX"));
        }

        return sender.send();
    }

    @Override
    public void add(ByteBuffer buffer) {
        throw new CommandException("TODO: This is for the other implementation.");
    }
}
