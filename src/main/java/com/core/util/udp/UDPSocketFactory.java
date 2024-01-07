package com.core.util.udp;

import java.io.IOException;

/**
 * User: jgreco
 */
public interface UDPSocketFactory {
    ReadWriteUDPSocket createReadWriteUDPSocket(UDPSocketReadWriteListener listener) throws IOException;
    WritableUDPSocket createWritableUDPSocket(UDPSocketWriteListener listener) throws IOException;


}
