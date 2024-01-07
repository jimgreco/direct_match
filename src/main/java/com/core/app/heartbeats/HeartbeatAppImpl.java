package com.core.app.heartbeats;

import com.core.util.ByteStringBuffer;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.UnifiedMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by jgreco on 5/31/15.
 */
public class HeartbeatAppImpl implements HeartbeatApp {
    private final byte appID;
    private final byte[] appName;
    private final String appNameString;
    private final HeartbeatSource application;

    byte nextFieldID = 1;
    private final Map<HeartBeatFieldIDEnum, HeartbeatNumberField> numberFields = new UnifiedMap<>();
    private final Map<HeartBeatFieldIDEnum, HeartbeatStringField> stringFields = new UnifiedMap<>();
    private final Map<HeartBeatFieldIDEnum, HeartbeatBooleanField> booleanFields = new UnifiedMap<>();

    private final List<HeartbeatField> fields = new FastList<>();

    public HeartbeatAppImpl(byte appID, String appName, HeartbeatSource application) {
        this.appID = appID;
        this.appName = appName.getBytes();
        this.appNameString = appName;
        this.application = application;
    }

    public void updateFields() {
        application.onHeartbeatUpdate(this);
    }

    @Override
    public HeartbeatStringField addStringField(String category, HeartBeatFieldIDEnum fieldEnum) {
        HeartbeatStringField field = stringFields.get(fieldEnum);
        if (field == null) {
            field = new HeartbeatStringFieldImpl(nextFieldID++, category, fieldEnum);
            fields.add(field);
            stringFields.put(fieldEnum, field);
        }
        return field;
    }

    @Override
    public HeartbeatNumberField addNumberField(String category, HeartBeatFieldIDEnum fieldName) {
        HeartbeatNumberField field = numberFields.get(fieldName);
        if (field == null) {
            field = new HeartbeatNumberFieldImpl(nextFieldID++, category, fieldName);
            fields.add(field);
            numberFields.put(fieldName, field);
        }
        return field;
    }

    @Override
    public HeartbeatBooleanField addBoolField(String category, HeartBeatFieldIDEnum fieldName) {
        HeartbeatBooleanField field = booleanFields.get(fieldName);
        if (field == null) {
            field = new HeartbeatBooleanField(nextFieldID++, category, fieldName);
            fields.add(field);
            booleanFields.put(fieldName, field);
        }
        return field;
    }

    @Override
    public void addField(HeartbeatField field) {
        fields.add(field);

        switch (field.getType()) {
            case HeartbeatConstants.STRING_TYPE:
                stringFields.put(HeartBeatFieldIDEnum.valueOf(field.getName()), (HeartbeatStringField)field);
                break;
            case HeartbeatConstants.NUMBER_TYPE:
                numberFields.put(HeartBeatFieldIDEnum.valueOf(field.getName()), (HeartbeatNumberField)field);
                break;
            case HeartbeatConstants.BOOLEAN_TYPE:
                booleanFields.put(HeartBeatFieldIDEnum.valueOf(field.getName()), (HeartbeatBooleanField)field);
                break;
            default: 
            	break;
        }
    }

    @Override
    public void setField(String fieldName, String value) {
        HeartbeatStringField field = stringFields.get(fieldName);
        if (field != null) {
            field.set(value);
        }
    }

    @Override
    public void setField(String fieldName, long value) {
        HeartbeatNumberField field = numberFields.get(fieldName);
        if (field != null) {
            field.set(value);
        }
    }

    @Override
    public void setField(String fieldName, boolean value) {
        setField(fieldName, value ? 1 : 0);
    }

    @Override
    public void setField(String fieldName, double value) {
        setField(fieldName, Double.doubleToLongBits(value));
    }

    public void writeData(ByteStringBuffer buffer) {
        for (int i=0; i<fields.size(); i++) {
            HeartbeatFieldBase field = (HeartbeatFieldBase)fields.get(i);
            field.writeData(buffer);
            buffer.addComma();

        }
    }

    @Override
    public String getName() {
        return appNameString;
    }

    @Override
    public List<HeartbeatField> getFields() {
        return fields;
    }

    public void writeApp(ByteStringBuffer buffer) {
        buffer.add(HeartBeatFieldIDEnum.App.toString());
        buffer.addColon();
        buffer.add(appNameString);
        buffer.addComma();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeartbeatAppImpl that = (HeartbeatAppImpl) o;
        return Objects.equals(appName, that.appName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName);
    }


    public byte getID() {
        return appID;
    }
}
