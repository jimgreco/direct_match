package com.core.connector;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 7/7/15.
 */
public interface ByteBufferDispatcher extends Dispatcher {
    boolean dispatch(ByteBuffer bytes);
}
