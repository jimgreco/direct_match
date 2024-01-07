package com.core.sequencer;

import java.nio.ByteBuffer;

/**
 * Created by hli on 5/12/16.
 */
public interface BackupQueue {
    void add(ByteBuffer inputToCopy);
}
