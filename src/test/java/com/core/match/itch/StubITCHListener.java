package com.core.match.itch;

import com.core.match.itch.msgs.ITCHCommonCommand;
import com.gs.collections.impl.list.mutable.FastList;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by jgreco on 6/15/15.
 */
public class StubITCHListener implements ITCHServerListener {
    private List<ITCHCommonCommand> received = new FastList<>();

    @Override
    public ByteBuffer getBuffer() {
        return ByteBuffer.allocate(1500);
    }

    @Override
    public void onMessage(ITCHCommonCommand msg) {
        received.add(msg);
    }

    @SuppressWarnings("unchecked")
	public <T extends ITCHCommonCommand> T pop(@SuppressWarnings("unused") Class<T> cls) {
        return received.isEmpty() ? null : (T)received.remove(0);
    }

    public int size() {
        return received.size();
    }
}
