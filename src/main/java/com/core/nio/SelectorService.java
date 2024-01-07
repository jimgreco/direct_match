package com.core.nio;

import com.core.util.file.File;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.tcp.TCPClientSocket;
import com.core.util.tcp.TCPServerSocket;
import com.core.util.tcp.TCPServerSocketAcceptListener;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerHandler;
import com.core.util.time.TimerService;
import com.core.util.time.TimerServiceImpl;
import com.core.util.udp.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * User: jgreco
 */
public class  SelectorService implements
        UDPSocketFactory,
        TCPSocketFactory,
        FileFactory,
        TimerService,
        SelectableChannelService {
    private final Log log;
    private final Selector selector;
    private final TimerServiceImpl timerService;

    public SelectorService(Log log, TimeSource timeSource) throws IOException {
        this(log, timeSource, Selector.open());
    }

    protected SelectorService(Log log, TimeSource timeSource, Selector selector) {
        this.log = log;
        this.selector = selector;
        this.timerService = new TimerServiceImpl(log, timeSource);

        log.info(log.log().add("Selector: ").add(selector.getClass().getName()));
    }

    @Override
    public int scheduleTimer(long nanos, TimerHandler handler) {
        return timerService.scheduleTimer(nanos, handler);
    }

    @Override
    public int scheduleTimer(long nanos, TimerHandler handler, int id) {
        return timerService.scheduleTimer(nanos, handler, id);
    }

    @Override
    public int cancelTimer(int timer) {
        return timerService.cancelTimer(timer);
    }

    @Override
    public ReadWriteUDPSocket createReadWriteUDPSocket(UDPSocketReadWriteListener listener) throws IOException {
        return new SelectorReadWriteUDPSocket(this, log, listener);
    }

    @Override
    public WritableUDPSocket createWritableUDPSocket(UDPSocketWriteListener listener) throws IOException {
        return new SelectorWriteUDPSocket(this, log, listener);
    }

    @Override
    public TCPServerSocket createTCPServerSocket(int port, TCPServerSocketAcceptListener listener) throws IOException {
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.bind(new InetSocketAddress(port));

        SelectorTCPServerSocket tcpServerSocket = new SelectorTCPServerSocket(this, log, channel, 1024 * 1024, listener);
        channel.register(selector, 0, tcpServerSocket);
        return tcpServerSocket;
    }

    @Override
    public Selector getSelector() {
        return selector;
    }

    @Override
    public TCPClientSocket createTCPClientSocket() throws IOException {
        SocketChannel clientChannel = SocketChannel.open();
        clientChannel.configureBlocking(false);

        SelectorTCPClientSocket clientSocket = new SelectorTCPClientSocket(this, log, clientChannel, 1024 * 1024, false);
        clientChannel.register(selector, 0, clientSocket);
        return clientSocket;
    }

    @Override
    public File createFile(String name, String mode) throws IOException {
        return new NIOFile(name, mode);
    }

    @Override
    public void free(SelectableChannel channel) {
        SelectionKey key = channel.keyFor(selector);

        if (key != null) {
            key.cancel();
        }
    }

    @Override
    public void enableAccept(SelectableChannel channel, boolean val) {
        SelectionKey key = channel.keyFor(selector);

        if (key != null) {
            changeOp(key, val, SelectionKey.OP_ACCEPT);
        }
    }

    @Override
    public void enableConnect(SelectableChannel channel, boolean val) {
        SelectionKey key = channel.keyFor(selector);

        if (key != null) {
            changeOp(key, val, SelectionKey.OP_CONNECT);
        }
    }

    @Override
    public void enableWrite(SelectableChannel channel, boolean val) {
        SelectionKey key = channel.keyFor(selector);

        if (key != null) {
            changeOp(key, val, SelectionKey.OP_WRITE);
        }
    }

    @Override
    public void enableRead(SelectableChannel channel, boolean val) {
        SelectionKey key = channel.keyFor(selector);

        if (key != null) {
            changeOp(key, val, SelectionKey.OP_READ);
            selector.wakeup();
        }
    }

    private static void changeOp(SelectionKey key, boolean val, int newOp) {
        boolean isNotEnabled = (key.interestOps() & newOp) == 0;
        if (val) {
            if (isNotEnabled) {
                key.interestOps(key.interestOps() | newOp);
            }
        }
        else {
            if (!isNotEnabled) {
                key.interestOps(key.interestOps() & ~newOp);
            }
        }
    }

    public void run() {
        while (true) {
            try {
                runOnce();
            }
            catch (Exception e) {
                log.error(log.log().add("ERROR IN APPLICATION. EXITING.").add(e));
                return;
            }
        }
    }

    public void runOnce() {
        timerService.triggerTimers();

        int select = 0;
        try {
            select = selector.selectNow();
        } catch (IOException e) {
            log.error(log.log().add("Error with selector ").add(e));
        }

        if (select > 0) {
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while(iterator.hasNext()) {
                SelectionKey key = iterator.next();
                try {
                    processKey(key);
                } catch(CancelledKeyException ignored) {
                }

                iterator.remove();
            }
        }

        // trigger timers again in case there is a zero timer
        timerService.triggerTimers();
    }

    private static void processKey(SelectionKey key) throws CancelledKeyException {
        // accepting a client socket
        if (key.isValid() && key.isAcceptable()) {
            SelectorHandler handler = (SelectorHandler) key.attachment();
            handler.onAccept();
        }

        // connecting to a server socket
        if (key.isValid() && key.isConnectable()) {
            SelectorHandler handler = (SelectorHandler) key.attachment();
            handler.onConnect();
        }

        // writing to a client socket
        if (key.isValid() && key.isWritable()) {
            SelectorHandler handler = (SelectorHandler) key.attachment();
            handler.onWrite();
        }

        // reading a client socket
        if (key.isValid() && key.isReadable()) {
            SelectorHandler handler = (SelectorHandler) key.attachment();
            handler.onRead();
        }
    }
}
