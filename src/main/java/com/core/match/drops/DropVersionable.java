package com.core.match.drops;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 2/19/16.
 */
public interface DropVersionable {
    int getVer();
    void setVer(int version);
    void write(ByteBuffer buffer, String session, String contrib);
}
