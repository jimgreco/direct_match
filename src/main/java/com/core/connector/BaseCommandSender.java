package com.core.connector;

import com.core.app.CommandException;
import com.core.util.log.Log;
import com.gs.collections.impl.list.mutable.FastList;

import java.util.List;

/**
 * Created by jgreco on 7/27/15.
 */
public abstract class BaseCommandSender implements
        CommandSender,
        SessionSourceListener {
    protected final Log log;

    private final String name;

    private String session;
    private short contribID;

    private boolean active;

    private int localContribSeqNum;
    private int eventStreamContribSeqNum;
    private int tempContribSeqNum;

    private final List<AllCommandsClearedListener> commandListeners = new FastList<>();
    private final List<ContributorDefinedListener> contributorListeners = new FastList<>();

    protected abstract void onCommandCleared();
    protected abstract void onActive();
    protected abstract void onPassive();
    protected abstract void onInit();
    protected abstract boolean onSend();

    public BaseCommandSender(Log log, String name) {
        this.log = log;
        this.name = name;
    }

    @Override
    public void init() {
        onInit();
        tempContribSeqNum = localContribSeqNum;
    }

    @Override
    public boolean send() {
        if (!onSend()) {
            return false;
        }
        localContribSeqNum = tempContribSeqNum;
        return true;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setActive() {
        if (contribID == 0) {
            throw new CommandException("Cannot set active when contributor has not been defined");
        }

        if (session == null) {
            throw new CommandException("Cannot set active when session has not been defined");
        }

        log.warn(log.log().add("tempSeqnum: ").add(tempContribSeqNum));

        // TODO: check for isCaughtUp()
        //if (!isCaughtUp()) {
        //    throw new CommandException("Not caught up to the end of the stream");
        //}
        onActive();

        init();
        active = true;
        if(!isCaughtUp()){
            log.info(log.log().add("packets in flight to be send"));
            onSend();
        }
    }

    @Override
    public void setPassive() {
        active = false;

        onPassive();
    }

    // TODO: This isn't really the definition of caught up.  We need to know if the entire connector is caught up.
    @Override
    public boolean isCaughtUp() {

        return eventStreamContribSeqNum == localContribSeqNum;
    }

    @Override
    public void setContribID(short contribID) {
        this.contribID = contribID;
        for (int i=0; i<contributorListeners.size(); i++) {
            contributorListeners.get(i).onContributorDefined(contribID, name);
        }
    }

    @Override
    public void onMyMessage(int contributorSeq) {
        eventStreamContribSeqNum = contributorSeq;

        onCommandCleared();

        // Make sure we're caught up on the messages we've sent vs. what we think we've sent
        if (eventStreamContribSeqNum >= localContribSeqNum) {
             localContribSeqNum = eventStreamContribSeqNum;

            if (canSend()) {
                // Okay, we're all caught up, notify the client apps send a new command if they want
                for (int i=0; i<commandListeners.size(); i++) {
                    commandListeners.get(i).onAllCommandsCleared();
                }
            }
        }
    }

    @Override
    public void addContributorDefinedListener(ContributorDefinedListener listener) {
        contributorListeners.add(listener);
    }

    @Override
    public void addAllCommandsClearedListener(AllCommandsClearedListener listener) {
        commandListeners.add(listener);
    }

    @Override
    public void onSessionDefinition(String session) {
        this.session = session;
    }

    @Override
    public boolean canSend() {
        return isActive() && isCaughtUp() && canWrite();
    }

    @Override
    public short getContribID() {
        return contribID;
    }

    @Override
    public int getLastSeenContribSeqNum() {
        return eventStreamContribSeqNum;
    }

    @Override
    public int getContribSeqNum() {
        return localContribSeqNum;
    }
    public int getCurrentMessageContribSeqNUm() {
        return tempContribSeqNum;
    }


    @Override
    public int incContribSeqNum() {
        return ++tempContribSeqNum;
    }

    @Override
	public String getSession() {
        return session;
    }
}
