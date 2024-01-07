package com.core.connector;

import com.gs.collections.impl.list.mutable.FastList;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by jgreco on 7/12/15.
 */
public abstract class BaseConnector implements Connector {
    private final List<MessageGroupCompleteListener> messageGroupCompleteListeners = new FastList<>();
    private final List<SessionSourceListener> sessionSourceListeners = new FastList<>();
    private final List<BeforeMessageListener> beforeMessageListeners = new FastList<>();
    private final List<AfterMessageListener> afterMessageListeners = new FastList<>();
    private final ByteBufferDispatcher dispatcher;

    public BaseConnector(ByteBufferDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void addMessageGroupCompleteListener(MessageGroupCompleteListener listener) {
        messageGroupCompleteListeners.add(listener);
    }

    @Override
    public void addSessionSourceListener(SessionSourceListener listener) {
        sessionSourceListeners.add(listener);
    }

    @Override
    public void addBeforeListener(BeforeMessageListener listener) {
        beforeMessageListeners.add(listener);
    }

    @Override
    public void addAfterListener(AfterMessageListener listener) {
        afterMessageListeners.add(listener);
    }

    protected void dispatchMessage(ByteBuffer message) {
        dispatchBefore(message);
        dispatcher.dispatch(message);
        dispatchAfter(message);
    }

    protected void dispatchMessageGroupComplete(long nextSeqNum) {
        for (int i=0; i<messageGroupCompleteListeners.size(); i++) {
            messageGroupCompleteListeners.get(i).onMessageGroupComplete(nextSeqNum);
        }
    }

    protected void dispatchSession(String sessionStr) {
        for (int i=0; i< sessionSourceListeners.size(); i++) {
            sessionSourceListeners.get(i).onSessionDefinition(sessionStr);
        }
    }

    private void dispatchBefore(ByteBuffer message) {
        for (int i=0; i<beforeMessageListeners.size(); i++) {
            beforeMessageListeners.get(i).onBeforeMessage(message);
        }
    }

    private void dispatchAfter(ByteBuffer message) {
        for (int i=0; i<afterMessageListeners.size(); i++) {
            afterMessageListeners.get(i).onAfterMessage(message);
        }
    }
}
