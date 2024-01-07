package com.core.connector.mold;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.core.app.Exposed;
import com.core.app.heartbeats.HeartBeatFieldIDEnum;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.app.heartbeats.HeartbeatNumberField;
import com.core.app.heartbeats.HeartbeatSource;
import com.core.app.heartbeats.HeartbeatStringField;
import com.core.connector.BaseConnector;
import com.core.connector.ByteBufferDispatcher;
import com.core.connector.Connector;
import com.core.util.BinaryUtils;
import com.core.util.ByteStringBuffer;
import com.core.util.log.Log;
import com.core.util.udp.UDPSocketFactory;

/**
 * User: jgreco
 */
public final class Mold64UDPConnector extends BaseConnector implements
        Connector,
        Mold64UDPListener,
        HeartbeatSource {

    private final Log log;

    private final Mold64UDP streamUdp;

    private long lastStreamSeqSeen = 0;
    private long nextStreamSeqToProcess = 1;
    private int requestsSent;
    private int packets;

    private final ByteStringBuffer temp = new ByteStringBuffer();

    private String session;

    private HeartbeatStringField sessionStrMonitor;
    private HeartbeatNumberField caughtUpMonitor;
    private HeartbeatNumberField nextStreamSeqToProcessMonitor;
    private HeartbeatNumberField lastStreamSeqSeenMonitor;
    private HeartbeatNumberField requestsSentMonitor;
    private HeartbeatNumberField packetsMonitor;

    public Mold64UDPConnector(Log log,
                              UDPSocketFactory udpFactory,
                              String intf,
                              String streamHost,
                              short streamPort,
                              ByteBufferDispatcher dispatcher) throws IOException {
    	this(log, new Mold64UDPConnection(udpFactory, log, intf, streamHost, streamPort), dispatcher);
    }

    public Mold64UDPConnector(Log log, Mold64UDP streamUdp, ByteBufferDispatcher dispatcher) {
        super(dispatcher);

        this.log = log;
        this.streamUdp = streamUdp;
        this.streamUdp.setListener(this);
    }

    @Override
    public boolean onMold64Packet(ByteBuffer session, long seqNum) {
        return checkSession(session);
    }

    @Override
    public void onMold64Message(long seqNum, ByteBuffer message) {
        // messages must be handled sequentially
        if (seqNum == nextStreamSeqToProcess) {
            nextStreamSeqToProcess++;

            dispatchMessage(message);

        }
    }

    @Override
    public void onMold64PacketComplete(ByteBuffer session, long nextSeqNum) {
        // seqNum of a heartbeat is the next seqNum to be seen
        // so the first heartbeats of the day are always of seqNum=1
        lastStreamSeqSeen = Math.max(nextSeqNum - 1, lastStreamSeqSeen);

        packets++;

        if (nextSeqNum == nextStreamSeqToProcess) {
            dispatchMessageGroupComplete(nextSeqNum);
        }

    }

    private boolean checkSession(ByteBuffer session) {
        if (this.session == null) {
            this.session = BinaryUtils.toString(session);
            //this.sessionStr = SessionUtils.sessionLongToString(sessionId);
            sessionStrMonitor.set(this.session);
            dispatchSession(this.session);
        }

        if (!BinaryUtils.compare(session, this.session)) {
            log.error(log.log().add("Received packet for invalid session. Expected=").add(this.session)
                    .add(" Recv=").add(session));
            return false;
        }

        return true;
    }

    @Override
    @Exposed(name = "open")
    public void open() throws IOException {
        streamUdp.open();
    }

    @Override
    @Exposed(name = "close")
    public void close() {
        streamUdp.close();
    }

    @Override
    @Exposed(name = "status")
    public ByteStringBuffer status() {
        temp.clear();
        temp.add("Session: ").add(session).addNL();
        temp.add("IsCaughtUp: ").add(isCaughtUp()).addNL();
        temp.add("NextStreamSeqToProcess: ").add(nextStreamSeqToProcess).addNL();
        temp.add("LastStreamSeq: ").add(lastStreamSeqSeen).addNL();
        temp.add("RequestsSent: ").add(requestsSent).addNL();
        temp.add("Packets: ").add(packets).addNL();
        return temp;
    }

    @Override
    public String getSession() {
        return session;
    }

    @Override
    public long getCurrentSeq() {
        return nextStreamSeqToProcess - 1;
    }

    public boolean isCaughtUp() {
        return session != null && nextStreamSeqToProcess == lastStreamSeqSeen + 1;
    }

    @Override
    public void onHeartbeatRegister(HeartbeatFieldRegister register) {
        sessionStrMonitor = register.addStringField("", HeartBeatFieldIDEnum.Session);
        caughtUpMonitor = register.addNumberField("", HeartBeatFieldIDEnum.CaughtUp);
        nextStreamSeqToProcessMonitor = register.addNumberField("", HeartBeatFieldIDEnum.NextSeqNumToProcess);
        lastStreamSeqSeenMonitor = register.addNumberField("", HeartBeatFieldIDEnum.LastSeenSeqNum);
        requestsSentMonitor = register.addNumberField("", HeartBeatFieldIDEnum.RequestSent);
        packetsMonitor = register.addNumberField("", HeartBeatFieldIDEnum.MoldPackets);
    }

    @Override
    public void onHeartbeatUpdate(HeartbeatFieldUpdater register) {
        caughtUpMonitor.set(isCaughtUp() ? 1 : 0);
        nextStreamSeqToProcessMonitor.set(nextStreamSeqToProcess);
        lastStreamSeqSeenMonitor.set(lastStreamSeqSeen);
        requestsSentMonitor.set(requestsSent);
        packetsMonitor.set(packets);
    }

    public void setSeqNum(int seqNum) {
        this.lastStreamSeqSeen = seqNum - 1;
        this.nextStreamSeqToProcess = seqNum;
    }
}
