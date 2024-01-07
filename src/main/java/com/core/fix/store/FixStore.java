package com.core.fix.store;

import com.core.fix.FixWriter;
import com.core.fix.connector.FixConnector;

/**
 * User: jgreco
 */
public interface FixStore {
    void init(FixWriter writer, FixConnector connector);
    int resend(int beginSeqNo, int endSeqNo);
    void reset(int seqNo);
    FixWriter createMessage(char msgType);
    FixWriter createMessage(String msgType);
    void finalizeBusinessMessage();
    void finalizeAdminMessage();
    void writeAdminMessage();
    int getNextOutboundSeqNo();
}
