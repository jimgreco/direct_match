package com.core.util.udp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public interface UDPSocketReadWriteListener extends UDPSocketWriteListener {
    void onDatagram(ReadWriteUDPSocket clientSocket, ByteBuffer datagram, InetSocketAddress addr);
}
