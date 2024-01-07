package com.core.sequencer;

import com.core.app.CommandException;
import com.core.connector.CoreCommonCommand;
import com.core.connector.CoreCommonEvent;
import com.core.connector.mold.Mold64UDPEventSender;
import com.core.util.ByteStringBuffer;
import com.core.util.log.Log;
import com.core.util.time.TimeSource;

/**
 * Created by jgreco on 7/26/15.
 */
public abstract class BaseCommandHandler {
    protected static final short SEQUENCER_CONTRIBUTOR_ID = 1;

    protected final Mold64UDPEventSender sender;
    protected final CoreSequencerContributorService contributors;
    protected final Log log;
    private final TimeSource timeSource;
    private final ByteStringBuffer text = new ByteStringBuffer(256);

    protected BaseCommandHandler(Log log,
                                 TimeSource timeSource,
                                 Mold64UDPEventSender sender,
                                 CoreSequencerContributorService contributors) {
        this.sender = sender;
        this.log = log;
        this.contributors = contributors;
        this.timeSource = timeSource;
    }

    public boolean commonEventCheck(CoreCommonEvent msg)
    {
        short contributorID = msg.getContributorID();
        if (!contributors.isValid(contributorID)) {
            log.error(log.log().add("Invalid ContributorID: ").add(contributorID));
            return true;
        }

        int contributorSeqNum = contributors.getSeqNum(contributorID);
        int recvContribSeqNum = msg.getContributorSeq();
        if (recvContribSeqNum != contributorSeqNum + 1) {
            log.error(log.log().add("Invalid ContributorSeqNum. ")
                    .add("Name=").add(contributors.getName(contributorID))
                    .add(", Expected=").add(contributorSeqNum + 1)
                    .add(", Recv=").add(recvContribSeqNum));
            log.error(log.log().add(msg.toString()));
            return true;
        }

        contributors.incSeqNum(contributorID);
        return false;
    }

    protected void sendMsgFromSeq(CoreCommonCommand command, boolean flush) {
        command.setContributorID(SEQUENCER_CONTRIBUTOR_ID);
        command.setContributorSeq(contributors.incSeqNum(SEQUENCER_CONTRIBUTOR_ID));
        sendMessage(command, flush);
    }

    public void sendMessage(CoreCommonCommand command, boolean flush) {
        command.setTimestamp(timeSource.getTimestamp());
        sender.finalizeMessage(command.getLength());

        if (flush) {
            sender.flush();
        }
    }

    protected boolean enable(CoreSequencerBaseService service, String name, boolean enabled) {
        short id = service.getID(name);
        if(!service.isValid(id)) {
            return false;
        }

        if (enabled) {
            service.setEnabled(id);
        }
        else {
            service.setDisabled(id);
        }
        return true;
    }

    protected static void checkString(String name, int length) {
        if (name == null) {
            throw new CommandException("String is null");
        }

        if (name.length() == 0) {
            throw new CommandException("String is empty");
        }

        if (name.length() > length) {
            throw new CommandException(name + " is longer then max length: " + length);
        }
    }

    protected ByteStringBuffer getText() {
        return text.clear();
    }
}
