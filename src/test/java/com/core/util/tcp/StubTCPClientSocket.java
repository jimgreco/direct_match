package com.core.util.tcp;

import com.core.util.BinaryUtils;
import com.gs.collections.impl.list.mutable.FastList;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by jgreco on 6/15/15.
 */
public class StubTCPClientSocket implements
        TCPClientSocket {
    private TCPClientSocketListener listener;

    private List<ByteBuffer> sent = new FastList<>();
    private List<ByteBuffer> recv = new FastList<>();
    private ByteBuffer readBuffer = ByteBuffer.allocate(1000);

    private boolean read;
    private boolean write = true;
    private boolean opened;

    private StubTCPClientSocket opposite;
    private StubTCPServerSocket server;

    private boolean dropSend;
    private boolean dropRecv;

    public void setServer(StubTCPServerSocket server) {
        this.server = server;
    }

    public void setOpposite(StubTCPClientSocket client) {
        this.opposite = client;
    }

    @Override
    public void enableRead(boolean val) {
        read = val;
    }

    @Override
    public void enableWrite(boolean val) {
        write = val;
    }

    @Override
    public boolean read(ByteBuffer buffer) {
        readBuffer.flip();
        recv.add(BinaryUtils.createCopy(readBuffer));
        buffer.put(BinaryUtils.createCopy(readBuffer));
        readBuffer.clear();
        return true;
    }
    
    @Override
    public int readBytes(ByteBuffer buffer) throws IOException {
    	int size = readBuffer.position();
    	read(buffer);
    	return size;
    }

    @Override
    public boolean write(ByteBuffer output) {
        sent.add(BinaryUtils.createCopy(output));

        if (!dropSend) {
            opposite.putInReadBuffer(BinaryUtils.createCopy(output));
        }

        output.position(output.limit());
        return true;
    }

    private void putInReadBuffer(ByteBuffer copy) {
        if (dropRecv) {
            return;
        }

        readBuffer.put(copy);
        listener.onReadAvailable(this);
    }

    @Override
    public boolean canRead() {
        return read && opened;
    }

    @Override
    public boolean canWrite() {
        return write && opened;
    }

    @Override
    public boolean isConnected() {
        return opened;
    }

    @Override
    public void setListener(TCPClientSocketListener listener) {
        this.listener = listener;
    }

    @Override
    public void connect(String host, short port) throws IOException {
        StubTCPClientSocket serverClient = server.connectTo();
        setOpposite(serverClient);

        if (opposite != null) {
            opposite.setOpposite(this);
            opened = true;
            listener.onConnect(this);
        }
    }

    public void open() {
        opened = true;
    }

    @Override
    public void close() {
        opened = false;
        listener.onDisconnect(this);
    }

    @Override
    public void closeClients() {
        opposite.close();
    }

    @Override
    public void closeWhenFinishedWriting() {
        close();
    }

    public void add(ByteBuffer buffer) {
        readBuffer.put(buffer);
        listener.onReadAvailable(this);
    }

    public ByteBuffer removeSent() {
        return sent.size() > 0 ? sent.remove(0) : null;
    }

    public ByteBuffer peekSent() {
        return sent.size() > 0 ? sent.get(0) : null;
    }

    public ByteBuffer removeRecv() {
        return recv.size() > 0 ? recv.remove(0) : null;
    }

    public ByteBuffer peekRecv() {
        return recv.size() > 0 ? recv.get(0) : null;
    }

    public int getSentPackets() {
        return sent.size();
    }

    public int getRecvPackets() {
        return recv.size();
    }

    public void dropSend(boolean dropSend) {
        this.dropSend = dropSend;
    }

    public void dropRecv(boolean dropRecv) {
        this.dropRecv = dropRecv;
    }

	@Override
	public SocketAddress getRemoteAddress() throws IOException {
		return new InetSocketAddress(123);
	}

	@Override
	public SocketAddress getLocalAddress() throws IOException {
		return new InetSocketAddress(123);
	}
}
