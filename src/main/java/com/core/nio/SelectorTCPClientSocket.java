package com.core.nio;

import com.core.util.log.Log;
import com.core.util.tcp.TCPClientSocket;
import com.core.util.tcp.TCPClientSocketListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * User: jgreco
 */
class SelectorTCPClientSocket implements
        TCPClientSocket,
        SelectorHandler {
    protected final SocketChannel channel;
    protected TCPClientSocketListener listener;

    protected final SelectableChannelService selectService;
    protected final Log log;

    protected final ByteBuffer writeBuffer;

    protected ConnectionState connectionState = ConnectionState.Closed;
    protected ReadState readState = ReadState.NotReading;
    protected WriteState writeState = WriteState.NotWriting;

    public SelectorTCPClientSocket(SelectableChannelService selectService,
                                   Log log,
                                   SocketChannel clientChannel,
                                   int size,
                                   boolean connected) {
        this.log = log;
        this.selectService = selectService;
        this.channel = clientChannel;
        this.writeBuffer = ByteBuffer.allocateDirect(size);

        if (connected) {
            writeState = WriteState.Writing;
            connectionState = ConnectionState.Connected;
        }
    }

    @Override
    public void setListener(TCPClientSocketListener listener) {
        this.listener = listener;
    }

    @Override
    public void enableRead(boolean val) {
        if (val) {
            this.readState = ReadState.Reading;
        }
        else {
            this.readState = ReadState.NotReading;
        }

        selectService.enableRead(channel, val);
    }

    @Override
    public void enableWrite(boolean val) {
        selectService.enableWrite(channel, val);
    }

    @Override
    public void onRead() {
        // notify the listener so they can read at their leisure
        if (listener != null) {
            listener.onReadAvailable(this);
        }
    }

    @Override
    public void onWrite() {
        if (!isConnected()) {
            enableWrite(false);
            return;
        }

        if (writeState == WriteState.Blocking) {
            writeBuffer.flip();

            try {
                channel.write(writeBuffer);
            } catch (IOException e) {
                log.error(log.log().add("Error writing to TCP client channel. Closing."));
                e.printStackTrace();
                close();
                return;
            }

            if (writeBuffer.hasRemaining()) {
                writeBuffer.compact();
                log.info(log.log().add("Could not write everything to TCP channel - 2"));
                enableWrite(true);
            }
            else {
                writeBuffer.clear();
                enableWrite(false);

                if (writeState == WriteState.PendingClosing) {
                    close();
                }
                else {
                    writeState = WriteState.Writing;
                    if (listener != null) {
                        listener.onWriteAvailable(this);
                    }
                }
            }
        }
    }

    @Override
    public boolean read(ByteBuffer buffer) {
        try {
        	int read = readBytes(buffer);

            if (read == -1) {
                return false;
            }

            return true;
        } catch (IOException e) {
            log.error(log.log().add("Error reading from TCP client channel. Closing."));
            close();
            return false;
        }
    }
    
    @Override
    public int readBytes(ByteBuffer buffer) throws IOException {
    	int bytes = channel.read(buffer);
    	if(bytes == -1) {
    		log.info(log.log().add("EOF. Hanging up on TCP client connection"));
            close();
    	}
    	return bytes;
    }

    @Override
    public boolean write(ByteBuffer output) {
        if (writeState == WriteState.Blocking) {
            addToWriteBuffer(output);
            return false;
        }

        if (!canWrite()) {
            return false;
        }

        try {
            channel.write(output);
        }
        catch (Exception e) {
            log.error(log.log().add("Error writing to TCP client channel. Closing."));
            close();
            return false;
        }

        if (output.hasRemaining()) {
            log.info(log.log().add("Could not write everything to TCP channel: ").add(output.remaining()).add(" bytes remaining."));
            writeBuffer.clear();

            if (addToWriteBuffer(output)) {
                enableWrite(true);
                if (listener != null) {
                    listener.onWriteUnavailable(this);
                }
            }
            return false;
        }

        return true;
    }

    private boolean addToWriteBuffer(ByteBuffer output) {
        if (output.remaining() > writeBuffer.remaining()) {
            log.error(log.log().add("TCP Client Socket write buffer is not large enough. Disconnecting. OutputSize=")
                    .add(output.remaining()).add(". BufferSize=").add(writeBuffer.remaining()));
            close();
            return false;
        }

        writeState = WriteState.Blocking;
        writeBuffer.put(output);
        return true;
    }

    @Override
    public boolean canRead() {
        return isConnected() && readState == ReadState.Reading;
    }

    @Override
    public boolean canWrite() {
        return isConnected() && writeState == WriteState.Writing;
    }

    @Override
    public boolean isConnected() {
        return connectionState == ConnectionState.Connected;
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (IOException e) {
            log.debug(log.log().add("Error closing channel: ").add(e));
        }

        writeBuffer.clear();
        connectionState = ConnectionState.Closed;
        readState = ReadState.NotReading;
        writeState = WriteState.NotWriting;

        if (listener != null) {
            listener.onDisconnect(this);
        }
    }

    @Override
    public void closeClients() {
    	throw new RuntimeException("Not implemented"); // TODO: implement
    }

    @Override
    public void closeWhenFinishedWriting() {
        if (writeState == WriteState.Blocking) {
            writeState = WriteState.PendingClosing;
        }
        else {
            close();
        }
    }

    @Override
    public String toString() {
        SocketAddress remoteSocketAddress = channel.socket().getRemoteSocketAddress();

        if (remoteSocketAddress == null) {
            return "Not Connected";
        }

        return remoteSocketAddress.toString();
    }

    //
    // Client specific stuff
    //

    @Override
    public void onAccept() {
        throw new RuntimeException("TCP Client only");
    }

    @Override
    public void onConnect() {
        try {
            if (!channel.finishConnect()) {
                log.info(log.log().add("Could not SelectorTCPClientSocket"));
                close();
                return;
            }
        } catch (IOException e) {
            log.error(log.log().add("Error connecting SelectorTCPClientSocket: ").add(e.getMessage()));
            close();
            return;
        }

        log.info(log.log().add("Connected TCP client"));
        connectionState = ConnectionState.Connected;
        writeState = WriteState.Writing;

        if (listener != null) {
            listener.onConnect(this);
        }
    }

    @Override
    public void connect(String host, short port) throws IOException {
        if (connectionState  != ConnectionState.Closed) {
            throw new IOException("Attempting to connect to the server twice");
        }

        log.info(log.log().add("Initiating Client TCP connection to ").add(host).add(":").add(port));

        selectService.enableConnect(channel, true);
        channel.connect(new InetSocketAddress(host, port));

        connectionState = ConnectionState.AttemptingToConnect;
    }

    protected enum ReadState {
        Reading,
        NotReading
    }

    protected enum WriteState {
        Writing,
        Blocking,
        PendingClosing,
        NotWriting
    }

    protected enum ConnectionState {
        Closed,
        AttemptingToConnect,
        Connected
    }

	@Override
	public SocketAddress getRemoteAddress() throws IOException {
		return channel.getRemoteAddress();
	}

	@Override
	public SocketAddress getLocalAddress() throws IOException {
		return channel.getLocalAddress();
	}
}
