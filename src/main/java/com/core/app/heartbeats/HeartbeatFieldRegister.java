package com.core.app.heartbeats;


/**
 * Created by jgreco on 5/31/15.
 */
public interface HeartbeatFieldRegister {
    HeartbeatStringField addStringField(String category, HeartBeatFieldIDEnum fieldName);
    HeartbeatNumberField addNumberField(String category, HeartBeatFieldIDEnum fieldName);
    HeartbeatBooleanField addBoolField(String category, HeartBeatFieldIDEnum fieldName);

    void addField(HeartbeatField field);
}
