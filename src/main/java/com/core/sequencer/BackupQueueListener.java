package com.core.sequencer;

import java.nio.ByteBuffer;

/**
 * Created by hli on 5/11/16.
 */
public interface BackupQueueListener {
    void sendRemainingQueueMessages(ByteBuffer byteBuffer);
}
