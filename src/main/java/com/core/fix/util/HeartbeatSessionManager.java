package com.core.fix.util;

import com.core.fix.connector.FIXConnectorListener;
import com.core.util.TimeUtils;
import com.core.util.time.TimerHandler;
import com.core.util.time.TimerService;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class HeartbeatSessionManager implements FIXConnectorListener {
    private final TimerService timerService;
    final HeartbeatSessionListener listener;

    private int heartbeatTimeoutSeconds;

    int recvHeartbeatTimer;
    int sendHeartbeatTimer;

    private final TimerHandler sendTimerHandler = new TimerHandler() {
        @Override
        public void onTimer(int internalTimerID, int referenceData) {
            listener.sendHeartbeat();

            sendHeartbeatTimer = TimerService.NULL_VALUE;
            onFIXMessageSent(null);
        }
    };

    private final TimerHandler recvTimerHandler = new TimerHandler() {
        @Override
        public void onTimer(int internalTimerID, int referenceData) {
            listener.sendTestRequest();

            recvHeartbeatTimer = TimerService.NULL_VALUE;
            onFIXMessageRecv(null);
        }
    };

    public HeartbeatSessionManager(TimerService timerService, HeartbeatSessionListener listener) {
        this.timerService = timerService;
        this.listener = listener;
    }

    @Override
    public void onFIXMessageSent(ByteBuffer buffer) {
        sendHeartbeatTimer = timerService.cancelTimer(sendHeartbeatTimer);

        if (heartbeatTimeoutSeconds > 0) {
            sendHeartbeatTimer = timerService.scheduleTimer(heartbeatTimeoutSeconds * TimeUtils.NANOS_PER_SECOND, sendTimerHandler);
        }
    }

    @Override
    public void onFIXMessageRecv(ByteBuffer msg) {
        recvHeartbeatTimer = timerService.cancelTimer(recvHeartbeatTimer);

        if (heartbeatTimeoutSeconds > 0) {
            recvHeartbeatTimer = timerService.scheduleTimer(2 * heartbeatTimeoutSeconds * TimeUtils.NANOS_PER_SECOND, recvTimerHandler);
        }
    }

    public void clear(int heartbeatTimeout) {
        this.heartbeatTimeoutSeconds = heartbeatTimeout;

        onFIXMessageSent(null);
        onFIXMessageRecv(null);
    }

    public int getHeartbeatTimeout() {
        return heartbeatTimeoutSeconds;
    }
}
