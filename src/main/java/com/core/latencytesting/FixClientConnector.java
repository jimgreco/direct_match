package com.core.latencytesting;

import com.core.connector.AllCommandsClearedListener;
import com.core.connector.CommandSender;
import com.core.fix.FixParser;
import com.core.fix.InvalidFixMessageException;
import com.core.fix.connector.FIXConnectionListener;
import com.core.fix.connector.FIXConnectorListener;
import com.core.fix.connector.FixConnector;
import com.core.fix.util.FixPrinter;
import com.core.match.fix.FixMachine;
import com.core.match.fix.FixStateMachine;
import com.core.util.log.Log;
import com.core.util.tcp.ReconnectingTCPClientSocket;
import com.core.util.tcp.TCPClientSocket;
import com.core.util.tcp.TCPClientSocketListener;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimerService;
import com.gs.collections.impl.list.mutable.FastList;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static com.core.match.fix.FixStateMachine.FixMessageResult.Disconnect;
import static com.core.match.fix.FixStateMachine.FixMessageResult.NextMessage;

/**
 * User: jgreco
 */
public class FixClientConnector implements
        FixConnector,
        TCPClientSocketListener{
    private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024 * 1024);

    private final Log log;

    private FixParser parser;
    private FixMachine machine;

    private final TCPClientSocket clientSocket;
    private FIXConnectionListener connectionListener;
    private final List<FIXConnectorListener> listeners = new FastList<>();
    private final String host;
    private final short port;

    public FixClientConnector(final Log log,
                              TCPSocketFactory socketFactory,
                              TimerService timers,
                              String host,
                              short port) {
        this.log = log;
        this.host = host;
        this.port = port;
        this.clientSocket = new ReconnectingTCPClientSocket(timers, socketFactory, log, this);
    }

    @Override
    public void addConnectionListener( FIXConnectionListener listener ){
        this.connectionListener = listener;
    }

    @Override
    public void addListener(FIXConnectorListener listener) {
        listeners.add(listener);
    }

    @Override
    public void init(FixParser fixParser, FixMachine fixStateMachine) {
        this.parser = fixParser;
        this.machine = fixStateMachine;
    }

    @Override
    public void open() throws IOException {
        if( !this.clientSocket.isConnected() )
            this.clientSocket.connect(this.host, this.port);
        this.clientSocket.enableRead(true);
    }

    @Override
    public void close() throws IOException {
        clientSocket.close();
    }

    @Override
    public void onConnect(TCPClientSocket clientSocket) {
        log.info(log.log().add("FIX: Client connected"));
        readBuffer.clear();
        machine.reset();

        if (this.connectionListener != null) {
            this.connectionListener.onConnect();
        }
    }

    @Override
    public void onDisconnect(TCPClientSocket socket) {
        log.info(log.log().add("FIX: Client disconnected"));
        readBuffer.clear();
        machine.reset();

        if( this.connectionListener != null ) {
            this.connectionListener.onDisconnect();
        }
    }

    @Override
    public void onReadAvailable(TCPClientSocket clientSocket) {
        log.info(log.log().add("Got something"));
        parseMessages();
    }

    private void parseMessages() {

        if (!clientSocket.isConnected()) {
            if (log.isDebugEnabled()) {
                log.debug(log.log().add("FIX: Socket not connected"));
            }
            readBuffer.clear();
            return;
        }

        if (!clientSocket.canWrite()) {
            if (log.isDebugEnabled()) {
                log.debug(log.log().add("FIX: Socket cannot write"));
            }
            return;
        }


        if (machine.isResending()) {
            if (log.isDebugEnabled()) {
                log.debug(log.log().add("FIX: CommandSender cannot send"));
            }
            return;
        }

        if (!clientSocket.read(readBuffer)) {
            if (log.isDebugEnabled()) {
                log.debug(log.log().add("FIX: Error reading"));
            }
        }

        if (readBuffer.position() == 0) {
            if (log.isDebugEnabled()) {
                log.debug(log.log().add("FIX: Nothing to read"));
            }
            readBuffer.clear();
            return;
        }

        readBuffer.flip();
        int bytesInMessage;

        while (true) {
            try {
                bytesInMessage = parser.parse(readBuffer);
            } catch (InvalidFixMessageException e) {
                log.error(log.log()
                        .add("Error parsing FIX. Closing connection.").add(e));
                clientSocket.close();
                return;
            }

            if (bytesInMessage > 0) {
                int oldLimit = readBuffer.limit();
                readBuffer.limit(readBuffer.position() + bytesInMessage);

                for (int i = 0; i < listeners.size(); i++) {
                    readBuffer.mark();
                    listeners.get(i).onFIXMessageRecv(readBuffer);
                    readBuffer.reset();
                }

                FixStateMachine.FixMessageResult result = machine.onFixMessage();
                if (result == Disconnect) {
                    FixPrinter.prepareFixForPrinting(readBuffer);
                    log.error(log.log().add("Disconnecting FIX port on message: ").add(readBuffer));
                    readBuffer.clear();
                    clientSocket.close();
                    return;
                }

                readBuffer.limit(oldLimit);
                readBuffer.position(readBuffer.position() + bytesInMessage);

                if (result == NextMessage) {
                    continue;
                }
            }
            // No matter what, this means wait for allCommandsCleared
            this.clientSocket.enableRead(false);
            break;
        }

        if (readBuffer.hasRemaining()) {
            readBuffer.compact();
        } else {
            readBuffer.clear();
        }
    }

    @Override
    public void onWriteAvailable(TCPClientSocket clientSocket) {
        log.info(log.log().add("FIX: Write available"));
        parseMessages();
    }

    @Override
    public void onWriteUnavailable(TCPClientSocket clientSocket) {
        log.info(log.log().add("FIX: Write not available"));
    }



    @Override
    public void resendComplete() {
        parseMessages();
    }

    @Override
    public boolean send(ByteBuffer msg) {
        boolean result;

        msg.mark();
        result = clientSocket.write(msg);
        msg.reset();

        for (int i = 0; i<listeners.size(); i++) {
            msg.mark();
            listeners.get(i).onFIXMessageSent(msg);
            msg.reset();
        }

        return result;
    }


}
