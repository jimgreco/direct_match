package com.core.fix.util;

import com.core.nio.SimulatedSelectorService;
import com.core.util.log.SystemOutLog;
import com.core.util.time.SimulatedTimeSource;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by jgreco on 1/4/15.
 */
public class HeartbeatSessionManagerTest {
    SimulatedTimeSource timeSource = new SimulatedTimeSource();
    HeartbeatSessionListener listener = Mockito.mock(HeartbeatSessionListener.class);
    SimulatedSelectorService selectorService = new SimulatedSelectorService(new SystemOutLog("CORE03-1", "SELECT", timeSource));
    HeartbeatSessionManager sessionManager = new HeartbeatSessionManager(selectorService, listener);

    @Test
    public void testNoHeartbeat() {
        selectorService.runFor(45000);

        Mockito.verifyZeroInteractions(listener);

        sessionManager.clear(0);
        selectorService.runFor(45000);

        Mockito.verifyZeroInteractions(listener);
    }

    @Test
    public void testSendMessageClearsTimerSendHeartbeat() {
        sessionManager.clear(30);

        selectorService.runFor(29000);
        Mockito.verifyZeroInteractions(listener);

        sessionManager.onFIXMessageSent(null);
        selectorService.runFor(29000);
        Mockito.verifyZeroInteractions(listener);

        selectorService.runFor(1001);
        Mockito.verify(listener).sendHeartbeat();
    }

    @Test
    public void testRecvMessageClearsTimerSendHeartbeat() {
        sessionManager.clear(30);

        selectorService.runFor(29000);
        Mockito.verifyZeroInteractions(listener);

        sessionManager.onFIXMessageSent(null);
        selectorService.runFor(29000);
        Mockito.verifyZeroInteractions(listener);

        sessionManager.onFIXMessageSent(null);
        selectorService.runFor(29000);

        sessionManager.onFIXMessageSent(null);
        selectorService.runFor(29000);

        selectorService.runFor(5000);
        Mockito.verify(listener).sendTestRequest();
    }
}
