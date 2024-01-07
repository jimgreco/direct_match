package com.core.app.heartbeats;

import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.UnifiedMap;

import java.util.List;
import java.util.Map;

/**
 * Created by jgreco on 6/5/15.
 */
public class StubHeartbeatApp implements HeartbeatApp {
    private byte nextID = 1;
    private final String name;
    private final List<HeartbeatField> fields = new FastList<>();
    private final Map<String, HeartbeatField> fieldMap = new UnifiedMap<>();

    public StubHeartbeatApp(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<HeartbeatField> getFields() {
        return fields;
    }

    @Override
    public HeartbeatStringField addStringField(String category, HeartBeatFieldIDEnum fieldName) {
        HeartbeatStringFieldImpl field = new HeartbeatStringFieldImpl(nextID++, category, fieldName);
        fields.add(field);
        fieldMap.put(field.getName(), field);
        return field;
    }

    @Override
    public HeartbeatNumberField addNumberField(String category, HeartBeatFieldIDEnum fieldName) {
        HeartbeatNumberFieldImpl field = new HeartbeatNumberFieldImpl(nextID++, category, fieldName);
        fields.add(field);
        fieldMap.put(field.getName(), field);
        return field;
    }

    @Override
    public HeartbeatBooleanField addBoolField(String category, HeartBeatFieldIDEnum fieldName) {
        return null;
    }

    @Override
    public void addField(HeartbeatField field) {
        fields.add(field);
        fieldMap.put(field.getName(), field);
    }

    @Override
    public void setField(String fieldName, String value) {

    }

    @Override
    public void setField(String fieldName, long value) {

    }

    @Override
    public void setField(String fieldName, boolean value) {

    }

    @Override
    public void setField(String fieldName, double value) {

    }

}
