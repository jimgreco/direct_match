package com.core.util.udp;

/**
 * Created by hli on 4/14/16.
 */
public interface UDPSocketWriteListener {
    void onWriteAvailable(WritableUDPSocket clientSocket);

    void onWriteUnavailable(WritableUDPSocket clientSocket);
}
