package com.core.match.itch;

import com.core.match.itch.msgs.ITCHCommonCommand;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 6/12/15.
 */
public interface ITCHServerListener {
    ByteBuffer getBuffer();
    void onMessage(ITCHCommonCommand msg);
}
