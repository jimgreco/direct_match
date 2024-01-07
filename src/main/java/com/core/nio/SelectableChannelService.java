package com.core.nio;

import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;

/**
 * Created by jgreco on 1/18/15.
 */
interface SelectableChannelService {
    void enableAccept(SelectableChannel channel, boolean val);
    void enableConnect(SelectableChannel channel, boolean val);
    void enableWrite(SelectableChannel channel, boolean val);
    void enableRead(SelectableChannel channel, boolean val);
    void free(SelectableChannel channel);
    Selector getSelector();
}
