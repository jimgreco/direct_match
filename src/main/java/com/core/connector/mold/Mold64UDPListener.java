package com.core.connector.mold;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public interface Mold64UDPListener {
    boolean onMold64Packet(ByteBuffer session, long streamSeq);
    void onMold64Message(long seqNum, ByteBuffer message);
    void onMold64PacketComplete(ByteBuffer session, long nextSeqNum);
}
