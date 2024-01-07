package com.core.match.fix;

import com.core.fix.FIXPortInfo;
import com.core.fix.FixWriter;
import com.core.fix.InlineFixWriter;
import com.core.fix.connector.StubFIXConnector;
import com.core.fix.msgs.FixDispatcher;
import com.core.fix.store.FIXMemoryStore;
import com.core.fix.store.FixStore;
import com.core.fix.util.TestFixParser;
import com.core.match.GenericAppTest;
import com.core.match.services.order.Order;

/**
 * Created by jgreco on 6/15/15.
 */
public abstract class GenericFIXAppTest<T extends Order<T>> extends GenericAppTest<T> {
    protected final StubFIXConnector fixConnector;
    protected final TestFixParser fixParser;
    protected final FixWriter fixWriter;
    protected final FixStore fixStore;
    protected final FixDispatcher fixDispatcher;
    protected final FIXPortInfo fixInfo;

    public GenericFIXAppTest(Class<T> clsReal, int minorVersion, String senderCompID, String targetCompID) {
        super(clsReal);

        this.fixConnector = new StubFIXConnector();
        this.fixParser = new TestFixParser();
        this.fixWriter = new InlineFixWriter(timeSource, minorVersion, senderCompID, targetCompID);
        this.fixStore = new FIXMemoryStore(fixWriter, fixConnector);
        this.fixDispatcher = new FixDispatcher(this.log);
        this.fixInfo = new FIXPortInfo(1000, 4, "SENDER", "TARGET");
    }
}
