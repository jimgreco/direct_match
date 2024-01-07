package com.core.util.tcp;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 7/9/15.
 */
public interface OneShotTCPServerTCPServerSocketListener {
    void onRead(TCPClientSocket socket, ByteBuffer buffer);
}
