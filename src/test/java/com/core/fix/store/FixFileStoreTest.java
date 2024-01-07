package com.core.fix.store;

import com.core.fix.FixMessage;
import com.core.fix.FixParser;
import com.core.fix.FixWriter;
import com.core.fix.InlineFixParser;
import com.core.fix.InlineFixWriter;
import com.core.fix.connector.StubFIXConnector;
import com.core.fix.msgs.FixTags;
import com.core.fix.tags.FixTag;
import com.core.util.file.File;
import com.core.util.file.FileFactory;
import com.core.util.file.InMemoryFile;
import com.core.util.log.SystemOutLog;
import com.core.util.time.SimulatedTimeSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by jgreco on 1/4/15.
 */
public class FixFileStoreTest {
    private File fixFile;
    private File indexFile;
    private FixFileStore fixStore;
    private FixWriter writer;
    private StubFIXConnector connector;
    private FixParser parser;

    @Before
    public void before() throws IOException {
        SimulatedTimeSource timeSource = new SimulatedTimeSource();

        fixFile = new InMemoryFile();
        indexFile = new InMemoryFile();
        writer = new InlineFixWriter(timeSource, 2, "SENDER", "RECEIVER");
        connector = new StubFIXConnector();

        fixStore = new FixFileStore((name, mode) -> {
            if (name.contains(".idx")) {
                return indexFile;
            }
            return fixFile;
        }, new SystemOutLog("CORE03-1", "FIX", timeSource), "FIX");

        parser = new InlineFixParser();

        fixStore.init(writer, connector);
    }

    @Test
    public void testAbandonCreateMessage() {
        fixStore.createMessage('A');
        fixStore.createMessage('1');
        fixStore.finalizeAdminMessage();

        fixStore.createMessage('2');
        fixStore.createMessage('4');
        fixStore.finalizeAdminMessage();

        Assert.assertEquals('1', connector.get().getMsgType());
        Assert.assertEquals('4', connector.get().getMsgType());
        Assert.assertEquals(3, fixStore.getNextOutboundSeqNo());
    }

    @Test
    public void testResend() {
        FixTag priceTag = parser.createWriteOnlyFIXTag(FixTags.Price);

        fixStore.createMessage('A');
        fixStore.finalizeAdminMessage();

        FixWriter tempWriter = fixStore.createMessage('8');
        tempWriter.writeNumber(priceTag, 100);
        fixStore.finalizeBusinessMessage();

        fixStore.createMessage('0');
        fixStore.finalizeAdminMessage();

        connector.get();
        connector.get();
        connector.get();
        Assert.assertEquals(4, fixStore.getNextOutboundSeqNo());

        Assert.assertEquals(0, fixStore.resend(1, 0));

        FixMessage msg;
        msg = connector.get();
        Assert.assertEquals('4', msg.getMsgType());
        Assert.assertEquals(1, msg.getSeqNum());
        Assert.assertEquals(2, msg.getTag(FixTags.NewSeqNo).getValueAsInt());
        msg = connector.get();
        Assert.assertEquals('8', msg.getMsgType());
        Assert.assertEquals(2, msg.getSeqNum());
        Assert.assertEquals(100, msg.getTag(FixTags.Price).getValueAsInt());
        msg = connector.get();
        Assert.assertEquals('4', msg.getMsgType());
        Assert.assertEquals(3, msg.getSeqNum());
        Assert.assertEquals(4, msg.getTag(FixTags.NewSeqNo).getValueAsInt());

        Assert.assertEquals(4, fixStore.getNextOutboundSeqNo());
    }

    @Test
    public void testSkipAdminMsgsOnResend() {
        FixTag priceTag = parser.createWriteOnlyFIXTag(FixTags.Price);

        fixStore.createMessage('A');
        fixStore.finalizeAdminMessage();

        FixWriter tempWriter = fixStore.createMessage('8');
        tempWriter.writeNumber(priceTag, 100);
        fixStore.finalizeBusinessMessage();

        tempWriter = fixStore.createMessage('8');
        tempWriter.writeNumber(priceTag, 101);
        fixStore.finalizeBusinessMessage();

        fixStore.createMessage('0');
        fixStore.finalizeAdminMessage();

        fixStore.createMessage('0');
        fixStore.finalizeAdminMessage();

        fixStore.createMessage('1');
        fixStore.finalizeAdminMessage();

        tempWriter = fixStore.createMessage('8');
        tempWriter.writeNumber(priceTag, 102);
        fixStore.finalizeBusinessMessage();

        fixStore.createMessage('0');
        fixStore.finalizeAdminMessage();

        fixStore.createMessage('1');
        fixStore.finalizeAdminMessage();

        while (connector.get() != null) {}

        Assert.assertEquals(10, fixStore.getNextOutboundSeqNo());

        Assert.assertEquals(0, fixStore.resend(1, 0));

        FixMessage msg;
        msg = connector.get();
        Assert.assertEquals('4', msg.getMsgType());
        Assert.assertEquals(1, msg.getSeqNum());
        Assert.assertEquals(2, msg.getTag(FixTags.NewSeqNo).getValueAsInt());
        msg = connector.get();
        Assert.assertEquals('8', msg.getMsgType());
        Assert.assertEquals(2, msg.getSeqNum());
        Assert.assertEquals(100, msg.getTag(FixTags.Price).getValueAsInt());
        msg = connector.get();
        Assert.assertEquals('8', msg.getMsgType());
        Assert.assertEquals(3, msg.getSeqNum());
        Assert.assertEquals(101, msg.getTag(FixTags.Price).getValueAsInt());

        msg = connector.get();
        Assert.assertEquals('4', msg.getMsgType());
        Assert.assertEquals(4, msg.getSeqNum());
        Assert.assertEquals(7, msg.getTag(FixTags.NewSeqNo).getValueAsInt());

        msg = connector.get();
        Assert.assertEquals('8', msg.getMsgType());
        Assert.assertEquals(7, msg.getSeqNum());
        Assert.assertEquals(102, msg.getTag(FixTags.Price).getValueAsInt());

        msg = connector.get();
        Assert.assertEquals('4', msg.getMsgType());
        Assert.assertEquals(8, msg.getSeqNum());
        Assert.assertEquals(10, msg.getTag(FixTags.NewSeqNo).getValueAsInt());


        Assert.assertEquals(0, fixStore.resend(6, 9));

        msg = connector.get();
        Assert.assertEquals('4', msg.getMsgType());
        Assert.assertEquals(6, msg.getSeqNum());
        Assert.assertEquals(7, msg.getTag(FixTags.NewSeqNo).getValueAsInt());

        msg = connector.get();
        Assert.assertEquals('8', msg.getMsgType());
        Assert.assertEquals(7, msg.getSeqNum());
        Assert.assertEquals(102, msg.getTag(FixTags.Price).getValueAsInt());

        msg = connector.get();
        Assert.assertEquals('4', msg.getMsgType());
        Assert.assertEquals(8, msg.getSeqNum());
        Assert.assertEquals(10, msg.getTag(FixTags.NewSeqNo).getValueAsInt());
    }

    @Test
    public void testSkipAdminMsgsOnResend2() {
        FixTag priceTag = parser.createWriteOnlyFIXTag(FixTags.Price);

        fixStore.createMessage('A');
        fixStore.finalizeAdminMessage();

        FixWriter tempWriter = fixStore.createMessage('8');
        tempWriter.writeNumber(priceTag, 100);
        fixStore.finalizeBusinessMessage();

        fixStore.createMessage('0');
        fixStore.finalizeAdminMessage();

        fixStore.createMessage('0');
        fixStore.finalizeAdminMessage();

        fixStore.createMessage('1');
        fixStore.finalizeAdminMessage();

        tempWriter = fixStore.createMessage('8');
        tempWriter.writeNumber(priceTag, 102);
        fixStore.finalizeBusinessMessage();

        while (connector.get() != null) {}

        Assert.assertEquals(0, fixStore.resend(1, 0));

        FixMessage msg;
        msg = connector.get();
        Assert.assertEquals('4', msg.getMsgType());
        Assert.assertEquals(1, msg.getSeqNum());
        Assert.assertEquals(2, msg.getTag(FixTags.NewSeqNo).getValueAsInt());
        msg = connector.get();
        Assert.assertEquals('8', msg.getMsgType());
        Assert.assertEquals(2, msg.getSeqNum());
        Assert.assertEquals(100, msg.getTag(FixTags.Price).getValueAsInt());
        msg = connector.get();
        Assert.assertEquals('4', msg.getMsgType());
        Assert.assertEquals(3, msg.getSeqNum());
        Assert.assertEquals(6, msg.getTag(FixTags.NewSeqNo).getValueAsInt());
        msg = connector.get();
        Assert.assertEquals('8', msg.getMsgType());
        Assert.assertEquals(6, msg.getSeqNum());
        Assert.assertEquals(102, msg.getTag(FixTags.Price).getValueAsInt());
    }

    @Test
    public void testSkipAdminMsgsOnResend3() {
        fixStore.createMessage('A');
        fixStore.finalizeAdminMessage();

        fixStore.createMessage('0');
        fixStore.finalizeAdminMessage();

        fixStore.createMessage('0');
        fixStore.finalizeAdminMessage();

        fixStore.createMessage('1');
        fixStore.finalizeAdminMessage();

        while (connector.get() != null) {}

        Assert.assertEquals(0, fixStore.resend(2, 0));

        FixMessage msg;
        msg = connector.get();
        Assert.assertEquals('4', msg.getMsgType());
        Assert.assertEquals(2, msg.getSeqNum());
        Assert.assertEquals(5, msg.getTag(FixTags.NewSeqNo).getValueAsInt());

        Assert.assertEquals(0, fixStore.resend(1, 2));

        msg = connector.get();
        Assert.assertEquals('4', msg.getMsgType());
        Assert.assertEquals(1, msg.getSeqNum());
        Assert.assertEquals(3, msg.getTag(FixTags.NewSeqNo).getValueAsInt());
    }

    @Test
    public void testStopSendingOnNewMessageAdded() {
        FixTag priceTag = parser.createWriteOnlyFIXTag(FixTags.Price);

        fixStore.createMessage('A');
        fixStore.finalizeAdminMessage();

        FixWriter tempWriter = fixStore.createMessage('8');
        tempWriter.writeNumber(priceTag, 100);
        fixStore.finalizeBusinessMessage();

        fixStore.createMessage('0');
        fixStore.finalizeAdminMessage();

        tempWriter = fixStore.createMessage('8');
        tempWriter.writeNumber(priceTag, 101);
        fixStore.finalizeBusinessMessage();

        while (connector.get() != null) {}

        connector.setReturnValueFalseAtSeqNum(2);

        Assert.assertEquals(3, fixStore.resend(1, 0));

        FixMessage msg;
        msg = connector.get();
        Assert.assertEquals('4', msg.getMsgType());
        Assert.assertEquals(1, msg.getSeqNum());
        Assert.assertEquals(2, msg.getTag(FixTags.NewSeqNo).getValueAsInt());

        msg = connector.get();
        Assert.assertEquals('8', msg.getMsgType());
        Assert.assertEquals(2, msg.getSeqNum());
        Assert.assertEquals(100, msg.getTag(FixTags.Price).getValueAsInt());

        Assert.assertNull(connector.get());

        Assert.assertEquals(0, fixStore.resend(3, 0));

        msg = connector.get();
        Assert.assertEquals('4', msg.getMsgType());
        Assert.assertEquals(3, msg.getSeqNum());
        Assert.assertEquals(4, msg.getTag(FixTags.NewSeqNo).getValueAsInt());

        msg = connector.get();
        Assert.assertEquals('8', msg.getMsgType());
        Assert.assertEquals(4, msg.getSeqNum());
        Assert.assertEquals(101, msg.getTag(FixTags.Price).getValueAsInt());
    }
}
