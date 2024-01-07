package com.core.util.tcp;

import com.core.util.BinaryUtils;
import com.core.util.file.FileFactory;
import com.core.util.file.IndexedFile;
import com.core.util.log.Log;
import com.gs.collections.impl.map.mutable.UnifiedMap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

/**
 * Created by jgreco on 8/4/15.
 */
public class TCPDrop {
    final Map<TCPClientSocket, Connection> allConnections = new UnifiedMap<>();
    private final ByteBuffer temp = ByteBuffer.allocate(4096);
    private final IndexedFile file;

    public TCPDrop(Log log, TCPSocketFactory tcpFactory, FileFactory fileFactory, String name, int port) throws IOException {
        this.file = new IndexedFile(fileFactory, log, name);

        tcpFactory.createTCPServerSocket(port, acceptedSocket -> {
            acceptedSocket.enableRead(true);

            TCPClientSocketListener listener = new TCPClientSocketListener() {
                @Override
                public void onConnect(TCPClientSocket clientSocket) {
                }

                @Override
                public void onDisconnect(TCPClientSocket clientSocket) {
                    Connection conn = allConnections.remove(clientSocket);
                    if (conn != null) {
                        conn.close();
                    }
                }

                @Override
                public void onReadAvailable(TCPClientSocket clientSocket) {
                }

                @Override
                public void onWriteAvailable(TCPClientSocket clientSocket) {
                    Connection conn = allConnections.get(clientSocket);
                    if (conn != null) {
                        conn.setNoWrite(false);
                        conn.dispatch();
                    }
                }

                @Override
                public void onWriteUnavailable(TCPClientSocket clientSocket) {
                    Connection conn = allConnections.get(clientSocket);
                    if (conn != null) {
                        conn.setNoWrite(true);
                    }
                }
            };

            Connection connection = new Connection(acceptedSocket, file.getNextIndex());
            allConnections.put(acceptedSocket, connection);

            for (int i = 0; i < file.getNextIndex(); i++) {
                temp.clear();
                file.read(i, temp);
                temp.flip();
                connection.add(BinaryUtils.toString(temp));
            }

            connection.dispatch();
            return listener;
        });
    }

    public void add(String msg) {
        file.write(ByteBuffer.wrap(msg.getBytes()));

        for (Connection connection : allConnections.values()) {
            connection.add(msg);
            connection.dispatch();
        }
    }

    public void open() {
        // no
    }

    private class Connection {
        private final ByteBuffer writeBuffer = ByteBuffer.allocateDirect(1024 * 1024);
        private final TCPClientSocket connectionSocket;
        private final Queue<String> messages;
        private boolean noWrite;

        public Connection(TCPClientSocket connectionSocket, int size) {
            this.connectionSocket = connectionSocket;
            this.messages = new ArrayDeque<>(size);
        }

        public void add(String msg) {
            messages.add(msg);
        }

        public void dispatch() {
            if (noWrite) {
                return;
            }

            writeBuffer.flip();
            if (writeBuffer.hasRemaining()) {
                connectionSocket.write(writeBuffer);
            }
            if (writeBuffer.hasRemaining()) {
                writeBuffer.compact();
                return;
            }

            writeBuffer.clear();
            while (!messages.isEmpty()) {
                String val = messages.peek();
                if (writeBuffer.remaining() < val.length()) {
                    break;
                }
                writeBuffer.put(val.getBytes());
                messages.remove();
            }
            writeBuffer.flip();
            connectionSocket.write(writeBuffer);

            if (writeBuffer.hasRemaining()) {
                writeBuffer.compact();
            }
            else {
                writeBuffer.clear();
            }
        }

        public void close() {
            messages.clear();
            noWrite = true;
        }

        public void setNoWrite(boolean noWrite) {
            this.noWrite = noWrite;
        }
    }
}
