package com.core.app.heartbeats;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by jgreco on 5/31/15.
 */
public interface HeartbeatVirtualMachine {
    List<HeartbeatApp> getApps();

    void addApp(HeartbeatApp app);
    HeartbeatApp addApp(String appName, HeartbeatSource application);
    void writeHeader(ByteBuffer buffer, boolean hasData);
    HeartbeatApp getApp(byte appID);
    HeartbeatApp getApp(String appName);
    String getVMNameString();
}
