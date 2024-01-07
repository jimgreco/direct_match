package com.core.match;

import com.core.app.ByteBufferCommandSender;
import com.core.connector.CommandSender;
import com.core.match.msgs.MatchBaseDispatcher;
import com.core.match.msgs.MatchCommonCommand;
import com.core.match.msgs.MatchCommonEvent;
import com.core.match.msgs.MatchContributorEvent;
import com.core.match.msgs.MatchContributorListener;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;
import com.core.util.time.TimeSource;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class MatchByteBufferCommandSender extends ByteBufferCommandSender implements
        MatchCommandSender,
        MatchContributorListener,
        MatchBaseDispatcher.MatchAfterListener {
    public MatchByteBufferCommandSender(Log log, TimeSource timeSource, CommandSender sender) {
        super(log, timeSource, sender);
    }

    @Override
    public void onMatchContributor(MatchContributorEvent msg) {
        if (sender.getContribID() > 0) {
            return;
        }

        // All messages are attributed to a single contributor
        // We need to find our contributor before we can start sending
        if (BinaryUtils.compare(msg.getName(), sender.getName())) {
            sender.setContribID(msg.getSourceContributorID());
        }
    }

    @Override
    public void onMatchAfterListener(MatchCommonEvent msg) {
        if (msg.getContributorID() == sender.getContribID()) {
            sender.onMyMessage(msg.getContributorSeq());
        }
    }

    @Override
    public boolean send(MatchCommonCommand command) {
       // init();
        add(command);
        return send();
    }

    @Override
    public void add(MatchCommonCommand command) {
        ByteBuffer msgBuffer = prepCommand(command);
        sender.add(msgBuffer);
    }

    private ByteBuffer prepCommand(MatchCommonCommand command) {
        // set the common fields
        // MsgType is set in the command itself
        command.setContributorID(sender.getContribID());
        command.setContributorSeq(sender.incContribSeqNum());
        command.setTimestamp(time.getTimestamp());

        if (log.isDebugEnabled()) {
            log.debug(log.log().add("PREPARE ").add(command.toString()));
        }

        ByteBuffer msgBuffer = command.getRawBuffer();
        int length = command.getLength();
        msgBuffer.position(0);
        msgBuffer.limit(length);
        return msgBuffer;
    }
}
