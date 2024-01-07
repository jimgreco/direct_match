package com.core.nio;

import com.core.util.log.Log;
import com.core.util.tcp.TCPClientSocket;
import com.core.util.tcp.TCPClientSocketListener;
import com.core.util.tcp.TCPServerSocket;
import com.core.util.tcp.TCPServerSocketAcceptListener;
import com.gs.collections.impl.list.mutable.FastList;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * Created by jgreco on 7/9/15.
 */
class SelectorTCPServerSocket implements
        TCPServerSocket,
        SelectorHandler {
    private final List<TCPClientSocket> clients = new FastList<>();
    private final ServerSocketChannel channel;
    private final TCPServerSocketAcceptListener listener;
    private final SelectableChannelService select;
    private final Log log;
    private final int size;

    public SelectorTCPServerSocket(SelectableChannelService select,
                             Log log,
                             ServerSocketChannel channel,
                             int size,
                             TCPServerSocketAcceptListener listener) {
        this.log = log;
        this.select = select;
        this.channel = channel;
        this.listener = listener;
        this.size = size;
    }

    @Override
    public void onAccept() {
        try {
            log.debug(log.log().add("TCP client socket accepted"));

            SocketChannel serverClientChannel = channel.accept();
            serverClientChannel.configureBlocking(false);

            SelectorTCPClientSocket clientSocket = new SelectorTCPClientSocket(select, log, serverClientChannel, size, true);
            serverClientChannel.register(select.getSelector(), 0, clientSocket);
            TCPClientSocketListener clientSocketListener= listener.onAccept(clientSocket);
            clientSocket.setListener(clientSocketListener);
            clients.add(clientSocket);
        } catch (IOException e) {
            log.error(log.log().add("Error accepting client").add(e));
        }
    }

    @Override
    public void close() {
        //When we close the server aka channel here, we also close all the clients- deregisters
        closeClients();

        try {
            channel.close();
        } catch (IOException e) {
            log.error(log.log()
                    .add("Exception : Closing TCP socket: ").add(e));
        }
    }

    @Override
    public void enableAccept(boolean accept) {
        log.info(log.log().add("SelectorTCPServerSocket ")
                .add(accept ? "accepting" : "not accepting")
                .add(" TCP client sockets"));
        select.enableAccept(channel, accept);
    }

    @Override
    public void closeClients() {
        log.debug(log.log().add("Closing All TCP client connections"));

        // clear out all the clients
        for (int i=0; i<clients.size(); i++) {
            TCPClientSocket tcpServerClientSocket = clients.get(i);
            tcpServerClientSocket.close();
        }

        clients.clear();
    }

    @Override
    public void closeClient(TCPClientSocket clientSocket) {
        log.debug(log.log().add("Closing single TCP client connection"));
        clients.remove(clientSocket);
    }

    @Override
    public void onConnect() {
        throw new RuntimeException("TCP Client only");
    }

    @Override
    public void onRead() {
        throw new RuntimeException("TCP Client only");
    }

    @Override
    public void onWrite() {
        throw new RuntimeException("TCP Client only");
    }
}