package com.core.match.services.events;

import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchSystemEventEvent;
import com.core.match.msgs.MatchSystemEventListener;
import com.gs.collections.impl.list.mutable.FastList;

import java.util.List;

/**
 * Created by jgreco on 6/15/15.
 */
public class SystemEventService implements MatchSystemEventListener {
    private final List<SystemEventListener> listeners = new FastList<>();
    private char state = 0;

    public void addListener(SystemEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void onMatchSystemEvent(MatchSystemEventEvent msg) {
        long timestamp = msg.getTimestamp();
        char type = msg.getEventType();

        if (type == MatchConstants.SystemEvent.Open && state != MatchConstants.SystemEvent.Open) {
            for (int i=0; i<listeners.size(); i++) {
                listeners.get(i).onOpen(timestamp);
            }
            state = MatchConstants.SystemEvent.Open;
        }
        else if (type == MatchConstants.SystemEvent.Close && state == MatchConstants.SystemEvent.Open) {
            for (int i=0; i<listeners.size(); i++) {
                listeners.get(i).onClose(timestamp);
            }
            state = MatchConstants.SystemEvent.Close;
        }
    }

    public static SystemEventService create() {
        return new SystemEventService();
    }

    public boolean isOpen() {
        return state == MatchConstants.SystemEvent.Open;
    }

    public boolean isClosed() {
        return state == MatchConstants.SystemEvent.Close;
    }
}
