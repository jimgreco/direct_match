package com.core.app.heartbeats;

/**
 * Created by jgreco on 8/5/15.
 */
public class NullHeartbeatRegister implements HeartbeatFieldRegister {
    @Override
    public HeartbeatStringField addStringField(String category, HeartBeatFieldIDEnum fieldName) {

        return new HeartbeatStringFieldImpl((byte)0, category, fieldName);
    }

    @Override
    public HeartbeatNumberField addNumberField(String category, HeartBeatFieldIDEnum fieldName) {
        return new HeartbeatNumberFieldImpl((byte)0, category, fieldName);
    }

    @Override
    public HeartbeatBooleanField addBoolField(String category, HeartBeatFieldIDEnum fieldName) {
        return null;
    }

    @Override
    public void addField(HeartbeatField field) {

    }
}
