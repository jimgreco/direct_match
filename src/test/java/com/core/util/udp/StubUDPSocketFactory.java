package com.core.util.udp;

import com.gs.collections.impl.list.mutable.FastList;

import java.io.IOException;
import java.util.List;

/**
 * Created by jgreco on 6/15/15.
 */
public class StubUDPSocketFactory implements UDPSocketFactory {
    private List<StubReadWriteUDPSocket> readWriteSocketList = new FastList<>();
    private List<StubWritableUDPSocket> writeSocketList = new FastList<>();


    public StubReadWriteUDPSocket popReadWriteSocket() {
        return readWriteSocketList.remove(0);
    }
    public StubWritableUDPSocket popWriteSocket() {
        return writeSocketList.remove(0);
    }

    @Override
    public ReadWriteUDPSocket createReadWriteUDPSocket(UDPSocketReadWriteListener listener) throws IOException {
        readWriteSocketList.add(new StubReadWriteUDPSocket());
        return readWriteSocketList.get(0);
    }

    @Override
    public WritableUDPSocket createWritableUDPSocket(UDPSocketWriteListener listener) throws IOException {
        writeSocketList.add(new StubWritableUDPSocket());
        return writeSocketList.get(0);    }
}
