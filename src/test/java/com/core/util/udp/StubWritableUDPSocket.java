package com.core.util.udp;

import com.gs.collections.impl.list.mutable.FastList;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by jgreco on 6/15/15.
 */
public class StubWritableUDPSocket implements SimpleWriteUDPSocket, WritableUDPSocket {
    private boolean channelOpened;


    private List<ByteBuffer> sent = new FastList<>();
    private boolean writeResult = true;
    private boolean noWrite;



    @Override
    public boolean write(ByteBuffer output) {
        ByteBuffer allocate = ByteBuffer.allocate(output.remaining());
        allocate.put(output);
        allocate.flip();
        sent.add(allocate);
        return writeResult;
    }

    @Override
    public boolean canWrite() {
        return channelOpened && !noWrite;
    }

    @Override
    public boolean write(ByteBuffer output, InetSocketAddress address) {
        write(output);
        return writeResult;    }

    @Override
    public void bind(String intf, String host, short port) throws IOException {
        channelOpened=true;

    }


    @Override
    public void open() throws IOException {

    }



    @Override
    public void close() {
        channelOpened = false;
    }



    public void setWriteResult(boolean result) {
        this.writeResult = result;
    }

    public ByteBuffer remove() {
        return sent.size() > 0 ? sent.remove(0) : null;
    }

    public ByteBuffer peek() {
        return sent.size() > 0 ? sent.get(0) : null;
    }

    public int getSentPackets() {
        return sent.size();
    }

    public void setNoWrite(boolean noWrite) {
        this.noWrite = noWrite;
    }
}
