package com.core.fix.store;

import com.core.fix.FixWriter;
import com.core.fix.connector.FixConnector;
import com.core.fix.msgs.FixMsgTypes;
import com.core.fix.msgs.FixTags;
import com.core.fix.tags.FixTag;
import com.core.fix.tags.InlineFixTag;
import com.core.util.file.FileFactory;
import com.core.util.file.IndexedFile;
import com.core.util.log.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class FixFileStore implements FixStore {
    private final IndexedFile indexedFile;
    private final ByteBuffer resendMessageBodyBuffer = ByteBuffer.allocateDirect(1024 * 1024);
    private final ByteBuffer messageBuffer = ByteBuffer.allocateDirect(1024 * 1024);
    private final ByteBuffer emptyBuffer = ByteBuffer.allocateDirect(0);

    private final FixTag gapFillFlag;
    private final FixTag newSeqNo;

    private FixWriter writer;
    private FixConnector connector;

    public FixFileStore(FileFactory fileFactory, Log log, String name) throws IOException {
        this.indexedFile = new IndexedFile(fileFactory, log, name + ".FIX");
        this.gapFillFlag = new InlineFixTag(FixTags.GapFillFlag);
        this.newSeqNo = new InlineFixTag(FixTags.NewSeqNo);
    }

    @Override
    public void init(FixWriter initWriter, FixConnector initConnector) {
        this.writer = initWriter;
        this.connector = initConnector;
    }

    @Override
    public void reset(int seqNo) {
        indexedFile.reset(seqNo - 1);
    }

    @Override
    public int resend(int beginSeqNo, int endSeqNo) {
        if (endSeqNo == 0) {
            endSeqNo = getNextOutboundSeqNo() - 1;
        }

        int seqNo = beginSeqNo;
        while (seqNo <= endSeqNo) {
            int firstSeqNo = seqNo;
            seqNo = findNextMessage(seqNo, endSeqNo);

            messageBuffer.clear();
            if (seqNo != firstSeqNo) {
                // bunch of admin messages
                writer.initFix(FixMsgTypes.SequenceReset, firstSeqNo);
                writer.writeChar(gapFillFlag, 'Y');
                writer.writeNumber(newSeqNo, seqNo);

                ByteBuffer fixBody = writer.getFixBody();
                writer.buildFixMessage(messageBuffer, fixBody, true);
            }
            else {
                writer.buildFixMessage(messageBuffer, resendMessageBodyBuffer, true);
                seqNo++;
            }
            messageBuffer.flip();

            if (!connector.send(messageBuffer)) {
                break;
            }
        }

        return seqNo > endSeqNo ? 0 : seqNo;
    }

    private int findNextMessage(int seqNo, int endSeqNo) {
        while (seqNo <= endSeqNo) {
            resendMessageBodyBuffer.clear();
            indexedFile.read(seqNo - 1, resendMessageBodyBuffer);
            resendMessageBodyBuffer.flip();

            if (resendMessageBodyBuffer.hasRemaining()) {
                return seqNo;
            }
            seqNo++;
        }
        return seqNo;
    }

    @Override
    public FixWriter createMessage(char msgType) {
        writer.initFix(msgType, getNextOutboundSeqNo());
        return writer;
    }

    @Override
    public FixWriter createMessage(String msgType) {
        writer.initFix(msgType, getNextOutboundSeqNo());
        return writer;
    }

    @Override
    public void finalizeBusinessMessage() {
        ByteBuffer fixBody = writer.getFixBody();

        indexedFile.write(fixBody);

        messageBuffer.clear();
        writer.buildFixMessage(messageBuffer, fixBody, false);
        messageBuffer.flip();

        connector.send(messageBuffer);
    }

    @Override
    public void finalizeAdminMessage() {
        ByteBuffer fixBody = writer.getFixBody();

        writeAdminMessage();

        messageBuffer.clear();
        writer.buildFixMessage(messageBuffer, fixBody, false);
        messageBuffer.flip();

        connector.send(messageBuffer);
    }

    @Override
    public void writeAdminMessage() {
        // write nothing for the zero-index since this is fix
        indexedFile.write(emptyBuffer);
    }

    @Override
    public int getNextOutboundSeqNo() {
        return indexedFile.getNextIndex() + 1;
    }
}
