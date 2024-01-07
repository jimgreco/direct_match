package com.core.app.heartbeats;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 6/2/15.
 */
public class HeartbeatFactory {



    public static String readString(ByteBuffer buffer) {
        short len = buffer.getShort();
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return new String(bytes);
    }

    public static String readAllString(ByteBuffer buffer) {
        int len = buffer.capacity();
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return new String(bytes);
    }
}
