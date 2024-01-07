package com.core.fix;

import com.core.app.heartbeats.HeartBeatFieldIDEnum;
import com.core.app.heartbeats.HeartbeatFieldRegister;

/**
 * Created by jgreco on 1/8/15.
 */
public class FIXPortInfo {
    private final int port;
    private final String senderCompID;
    private final String targetCompID;
    private final String version;
    private final int intVersion;

    public FIXPortInfo(int port, int version, String senderCompID, String targetCompID) {
        this.intVersion = version;
        this.port = port;
        this.version = "FIX.4." + version;
        this.senderCompID = senderCompID;
        this.targetCompID = targetCompID;
    }

    public void addStatus(HeartbeatFieldRegister register) {
        register.addNumberField("FIX", HeartBeatFieldIDEnum.Port).set(port);
        register.addStringField("FIX", HeartBeatFieldIDEnum.Version).set(version);
        register.addStringField("FIX", HeartBeatFieldIDEnum.TargetCompID).set(targetCompID);
        register.addStringField("FIX", HeartBeatFieldIDEnum.SenderCompID).set(senderCompID);
    }

    public int getMinorVersion() {
        return intVersion;
    }

    public String getSenderCompID() {
        return senderCompID;
    }

    public String getTargetCompID() {
        return targetCompID;
    }
}
