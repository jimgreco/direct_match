package com.core.app.heartbeats;

import java.util.List;

/**
 * Created by jgreco on 5/30/15.
 */
public interface HeartbeatApp extends HeartbeatFieldRegister, HeartbeatFieldUpdater {
    String getName();
    List<HeartbeatField> getFields();
}
