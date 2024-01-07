package com.core.app.heartbeats;

/**
 * Created by jgreco on 6/4/15.
 */
public interface HeartbeatSource {
    void onHeartbeatRegister(HeartbeatFieldRegister register);
    void onHeartbeatUpdate(HeartbeatFieldUpdater register);
}
