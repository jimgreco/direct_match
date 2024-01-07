package com.core.match.services.events;

import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchSystemEventCommand;
import com.core.match.msgs.MatchTestMessages;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by jgreco on 6/15/15.
 */
public class SystemEventServiceTest {
    private SystemEventService service;
    private SystemEventListener listener;
    private MatchTestMessages msgs;

    @Before
    public void before() {
        msgs = new MatchTestMessages();
        listener = Mockito.mock(SystemEventListener.class);
        service = new SystemEventService();
        service.addListener(listener);
    }

    @Test
    public void testOpen() {
        MatchSystemEventCommand cmd = msgs.getMatchSystemEventCommand();
        cmd.setTimestamp(500);
        cmd.setEventType(MatchConstants.SystemEvent.Open);
        service.onMatchSystemEvent(cmd.toEvent());

        Mockito.verify(listener).onOpen(500);
    }

    @Test
    public void testClose() {
        MatchSystemEventCommand cmd = msgs.getMatchSystemEventCommand();
        cmd.setTimestamp(100);
        cmd.setEventType(MatchConstants.SystemEvent.Open);
        service.onMatchSystemEvent(cmd.toEvent());
        Mockito.verify(listener).onOpen(100);

        cmd = msgs.getMatchSystemEventCommand();
        cmd.setTimestamp(105);
        cmd.setEventType(MatchConstants.SystemEvent.Close);
        service.onMatchSystemEvent(cmd.toEvent());
        Mockito.verify(listener).onClose(105);
    }

    @Test
    public void testDontDoCloseWithoutOpen() {
        MatchSystemEventCommand cmd = msgs.getMatchSystemEventCommand();
        cmd.setEventType(MatchConstants.SystemEvent.Close);
        service.onMatchSystemEvent(cmd.toEvent());
        Mockito.verifyZeroInteractions(listener);
    }

    @Test
    public void testOnlyDoASingleOpen() {
        MatchSystemEventCommand cmd = msgs.getMatchSystemEventCommand();
        cmd.setTimestamp(500);
        cmd.setEventType(MatchConstants.SystemEvent.Open);
        service.onMatchSystemEvent(cmd.toEvent());
        Mockito.verify(listener).onOpen(500);

        service.onMatchSystemEvent(cmd.toEvent());
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    public void testOnlyDoASingleClose() {
        MatchSystemEventCommand cmd = msgs.getMatchSystemEventCommand();
        cmd.setTimestamp(500);
        cmd.setEventType(MatchConstants.SystemEvent.Open);
        service.onMatchSystemEvent(cmd.toEvent());
        Mockito.verify(listener).onOpen(500);

        cmd = msgs.getMatchSystemEventCommand();
        cmd.setTimestamp(600);
        cmd.setEventType(MatchConstants.SystemEvent.Close);
        service.onMatchSystemEvent(cmd.toEvent());
        Mockito.verify(listener).onClose(600);

        service.onMatchSystemEvent(cmd.toEvent());
        Mockito.verifyNoMoreInteractions(listener);
    }
}
