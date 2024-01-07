package com.core.app.heartbeats;

import com.core.util.ByteStringBuffer;

/**
 * Created by jgreco on 5/29/15.
 */
abstract class HeartbeatFieldBase implements HeartbeatField {
    private final String fieldNameString;
    private final String categoryString;

    protected abstract void writeAsBinary(ByteStringBuffer buffer);

    public HeartbeatFieldBase(byte fieldID, String category, HeartBeatFieldIDEnum fieldName) {
        if (category == null || category.equals("")) {
            category = "General";
        }

        this.fieldNameString = fieldName.name();
        this.categoryString = category;
    }

    @Override
    public final void writeData(ByteStringBuffer buffer) {
        buffer.add(fieldNameString);
        buffer.addColon();
        writeAsBinary(buffer);
    }


    @Override
    public String getName() {
        return fieldNameString;
    }

    @Override
    public String getCategory() {
        return categoryString;
    }
}