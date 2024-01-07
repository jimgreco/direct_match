package com.core.app.heartbeats;

import com.core.util.ByteStringBuffer;

/**
 * Created by hli on 12/2/15.
 */
public class HeartbeatBooleanField extends HeartbeatFieldBase  implements HeartbeatField  {
    private boolean value;

    public HeartbeatBooleanField(byte fieldID, String category, HeartBeatFieldIDEnum fieldName) {
        super(fieldID, category, fieldName);
    }

    @Override
    public String getCategory() {
        return super.getCategory();
    }

    @Override
    protected void writeAsBinary(ByteStringBuffer buffer) {
        buffer.add(getValue());

    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public char getType() {
        return 'B';
    }

    public void set(Boolean value){
        this.value=value;
    }

    @Override
    public String getValue() {
        return value? "True":"False";
    }

    public String get() {
        return getValue();
    }


}
