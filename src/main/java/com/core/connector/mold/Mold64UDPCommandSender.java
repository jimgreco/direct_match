package com.core.connector.mold;

import com.core.app.CommandException;
import com.core.connector.BaseCommandSender;
import com.core.util.TimeUtils;
import com.core.util.log.Log;
import com.core.util.time.TimerHandler;
import com.core.util.time.TimerService;
import com.core.util.udp.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by jgreco on 6/19/15.
 */
public class Mold64UDPCommandSender extends BaseCommandSender implements
        UDPSocketReadWriteListener,
        TimerHandler {
    private final String host;
    private Mold64PacketWriter packet;
    private ByteBuffer datagramInFlight;

    private final SingleWriteUDPSocket socket;

    private int retry;
    private boolean failOnSetActive = true;

    protected final TimerService timerService;
    private int sendTimer;

    public Mold64UDPCommandSender(String name,
                                  Log log,
                                  UDPSocketFactory socketFactory,
                                  TimerService timerService,
                                  String intf,
                                  String host,
                                  short port) throws IOException {
        super(log, name);
        this.host=host;

        this.socket = new SingleWriteUDPSocket(socketFactory.createWritableUDPSocket(this), intf, host, port);
        this.timerService = timerService;
        this.packet = new Mold64UDPPacket(log);
    }

    @Override
    public String getDestinationAddress() {
        return host;
    }

    @Override
    public boolean canWrite() {
        return socket.canWrite();
    }

    @Override
    protected void onCommandCleared() {
        //New Method added to interface
    }

    @Override
    protected void onActive() {
        close();

        try {
            socket.open();
        }
        catch (Exception e) {
            if (failOnSetActive) {
                throw new CommandException(e);
            }
        }

        failOnSetActive = false;
        retry = 0;
    }

    @Override
    protected void onPassive() {
        close();
    }

    private void close() {
        socket.close();
        datagramInFlight = null;
    }

    @Override
    public void add(ByteBuffer msgBuffer) {
        packet.addMessage(msgBuffer);
    }

    @Override
    protected void onInit() {
        packet.init(getSession());
    }

    @Override
    protected boolean onSend() {
        if (!canWrite() || !packet.hasAddedMessages()) {
            packet.clear();
            return false;
        }

        ByteBuffer datagram = packet.getDatagram();

        if (!socket.write(datagram)) {
            // put in queue ?
            log.error(log.log().add("Could not write to UDP socket. Waiting for callback."));
            return false;
        }

        retry = 0;
        datagramInFlight = datagram;
        scheduleTimer();
        return true;
    }

    private void scheduleTimer() {
        if (datagramInFlight != null) {
            datagramInFlight.position(0);
            sendTimer = timerService.scheduleTimer(100 * TimeUtils.NANOS_PER_MILLI, this);
        }
    }

    @Override
    public void onDatagram(ReadWriteUDPSocket clientSocket, ByteBuffer buffer, InetSocketAddress addr) {
        // shouldn't happen
    }

    @Override
    public void onWriteAvailable(WritableUDPSocket clientSocket) {
        if (datagramInFlight == null || (socket.canWrite() && socket.write(datagramInFlight))) {
        }
        else {
            log.error(log.log().add("Could not write to UDP socket again. Waiting for callback."));
        }
    }

    @Override
    public void onWriteUnavailable(WritableUDPSocket clientSocket) {
    }

    @Override
    public void onTimer(int internalTimerID, int referenceData) {
        if (datagramInFlight == null || !canWrite()) {
            return;
        }

        if (datagramInFlight.remaining() == 0) {
            log.error(log.log().add("In timer loop trying to send datagram with no bytes"));
            packet.clear();
            return;
        }

        if (++retry > 100) {
            log.info(log.log().add("Retried ")
                    .add(retry).add(" times.  Going inactive"));

            try {
                setPassive();
            }
            catch(Exception ignored) {
            }
            return;
        }

        log.warn(log.log().add("Retrying to write datagram: ")
                .add("Retry ").add(retry)
                .add(" - ").add(datagramInFlight.remaining()).add(" bytes"));

        if (!socket.write(datagramInFlight)) {
            // put in queue ?
            log.error(log.log().add("Could not write to UDP socket. Waiting for callback."));
            return;
        }

        scheduleTimer();
    }

}
