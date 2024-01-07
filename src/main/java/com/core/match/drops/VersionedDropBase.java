package com.core.match.drops;

import com.core.app.heartbeats.*;
import com.core.connector.Connector;
import com.core.match.MatchApplication;
import com.core.util.BinaryUtils;
import com.core.util.TextUtils;
import com.core.util.TimeUtils;
import com.core.util.log.Log;
import com.core.util.tcp.TCPClientSocket;
import com.core.util.tcp.TCPClientSocketListener;
import com.core.util.tcp.TCPServerSocket;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerHandler;
import com.core.util.time.TimerService;
import com.gs.collections.impl.list.mutable.FastList;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by jgreco on 2/21/16.
 */
public abstract class VersionedDropBase extends MatchApplication {
    private static final long HEARTBEAT_TIMEOUT = 250 * TimeUtils.NANOS_PER_MILLI;

    private final long updateTimeout;
    private final TimerService timers;
    private final TimeSource time;

    protected final LinearCounter versionCounter;

    private final int port;
    private final TCPSocketFactory factory;
    private final Connector connector;
    private final String name;
    private TCPServerSocket serverSocket;
    private final List<SocketHandler> clientSockets;

    private HeartbeatNumberField versionField;
    private HeartbeatNumberField clientField;
    private HeartbeatBooleanField activeField;
    private HeartbeatNumberField timeoutField;
    private HeartbeatNumberField portField;


    private final List<DropCollection> collections;
    private boolean isActive;

    public VersionedDropBase(Log log,
                             TimeSource time,
                             TimerService timers,
                             TCPSocketFactory factory,
                             Connector connector,
                             String name,
                             int updateTimeoutMS,
                             int port) {
        super(log);

        this.name = name;

        this.connector = connector;
        this.collections = new FastList<>();

        this.factory = factory;
        this.clientSockets = new FastList<>();
        this.port = port;

        this.versionCounter = new LinearCounter();

        this.time = time;
        this.timers = timers;
        this.updateTimeout = updateTimeoutMS * TimeUtils.NANOS_PER_MILLI;

        timers.scheduleTimer(updateTimeout, new UpdateTimer());
        timers.scheduleTimer(HEARTBEAT_TIMEOUT, new HeartbeatTimer());
        isActive=false;
    }

    @Override
    protected void onActive() {
        isActive=true;
        if (serverSocket != null) {
            serverSocket.close();
            clientSockets.clear();
            serverSocket = null;
        }

        try {
            serverSocket = factory.createTCPServerSocket(port, clientSocket -> {
                SocketHandler handler = new SocketHandler(clientSocket);
                clientSockets.add(handler);
                return handler;
            });
            serverSocket.enableAccept(true);
        } catch (IOException e) {
            throw new RuntimeException("Error opening serverSocket");
        }
    }

    @Override
    protected void onPassive() {
        isActive=false;
        if (serverSocket != null) {
            serverSocket.close();
        }
        clientSockets.clear();
    }


    protected void addCollection(DropCollection collection) {
        collections.add(collection);
    }

    @Override
    public void onAddHeartbeatFields(HeartbeatFieldRegister fieldRegister) {
        versionField = fieldRegister.addNumberField("DROP", HeartBeatFieldIDEnum.Version);
        activeField = fieldRegister.addBoolField("DROP", HeartBeatFieldIDEnum.Active);
        clientField = fieldRegister.addNumberField("DROP", HeartBeatFieldIDEnum.ClientCount);
        timeoutField = fieldRegister.addNumberField("DROP", HeartBeatFieldIDEnum.Timeout);
        portField = fieldRegister.addNumberField("DROP", HeartBeatFieldIDEnum.Port);

    }

    @Override
    public void onUpdateHeartbeatFields(HeartbeatFieldUpdater fieldUpdater) {
        versionField.set(versionCounter.getVersion());
        clientField.set(clientSockets.size());
        activeField.set(isActive);
        timeoutField.set(updateTimeout);
        portField.set(port);

    }

    private class HeartbeatTimer implements TimerHandler {
        @Override
        public void onTimer(int internalTimerID, int referenceData) {
            timers.scheduleTimer(HEARTBEAT_TIMEOUT, this);

            for (int i=0; i < clientSockets.size(); i++) {
                SocketHandler handler = clientSockets.get(i);
                handler.writeHeartbeat();
            }
        }
    }

    private class UpdateTimer implements TimerHandler {
        @Override
        public void onTimer(int internalTimerID, int referenceData) {
            timers.scheduleTimer(updateTimeout, this);

            for (int i=0; i < clientSockets.size(); i++) {
                SocketHandler handler = clientSockets.get(i);
                handler.write();
            }
        }
    }

    private class SocketHandler implements TCPClientSocketListener {
        private final TCPClientSocket socket;
        private final ByteBuffer writeBuffer;
        private final DropConnection subscription;
        private boolean noWrite = false;
        private long lastTimestamp;

        SocketHandler(TCPClientSocket socket) {
            this.socket = socket;
            this.writeBuffer = ByteBuffer.allocateDirect(10 * 1024 * 1024);
            this.subscription = new DropConnection(versionCounter, collections);
        }

        @Override
        public void onConnect(TCPClientSocket clientSocket) {
        }

        @Override
        public void onDisconnect(TCPClientSocket clientSocket) {
            noWrite = true;
            clientSockets.remove(this);
        }

        @Override
        public void onReadAvailable(TCPClientSocket clientSocket) {
        }

        @Override
        public void onWriteAvailable(TCPClientSocket clientSocket) {
            noWrite = false;
            write();
        }

        @Override
        public void onWriteUnavailable(TCPClientSocket clientSocket) {
            noWrite = true;
        }

        public void write() {
            if (socketBad()) {
                return;
            }

            if (!subscription.isUpdated()) {
                return;
            }

            writeBuffer.clear();
            List<DropVersionable> updates = subscription.buildUpdates();
            for (int i=0; i<updates.size(); i++) {
                DropVersionable versionable = updates.get(i);
                versionable.write(writeBuffer, connector.getSession(), name);
            }
            writeBuffer.flip();
            socket.write(writeBuffer);

            subscription.confirmVersion();
            lastTimestamp = time.getTimestamp();
        }

        private boolean socketBad() {
            if (noWrite) {
                return true;
            }

            if (!socket.canWrite()) {
                return true;
            }
            return false;
        }

        public void writeHeartbeat() {
            if (socketBad()) {
                return;
            }

            if (time.getTimestamp() < lastTimestamp + TimeUtils.NANOS_PER_SECOND) {
                return;
            }

            writeBuffer.clear();
            BinaryUtils.copy(writeBuffer, "{\"type\":\"heartbeat\",\"id\":0,\"time\":");
            TextUtils.writeNumber(writeBuffer, time.getTimestamp() / TimeUtils.NANOS_PER_SECOND);
            BinaryUtils.copy(writeBuffer, "}\n");
            writeBuffer.flip();
            socket.write(writeBuffer);

            lastTimestamp = time.getTimestamp();
        }
    }

    private class DropConnection {
        private final List<DropCollection> collections;
        private final List<DropVersionable> results = new FastList<>();
        private final LinearCounter counter;

        private int version;
        private int pendingVersion;

        public DropConnection(LinearCounter counter, List<DropCollection> collections) {
            this.counter = counter;
            this.collections = collections;
        }

        public boolean isUpdated() {
            return version < counter.getVersion();
        }

        public List<DropVersionable> buildUpdates() {
            results.clear();
            for (int i=0; i<collections.size(); i++) {
                update(collections.get(i));
            }
            return results;
        }

        private void update(DropCollection collection) {
            DropCollection.DropIterator iterator = collection.getIterator(version);
            while (iterator.hasNext()) {
                DropVersionable next = iterator.next();
                if (next != null) {
                    results.add(next);
                }
            }

            this.pendingVersion = Math.max(iterator.getMaxVersion(), this.pendingVersion);
        }

        public void confirmVersion() {
            this.version = pendingVersion;
        }
    }
}
