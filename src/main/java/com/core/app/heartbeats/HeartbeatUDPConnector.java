package com.core.app.heartbeats;

import com.core.connector.mold.Mold64UDPPacket;
import com.core.util.ByteStringBuffer;
import com.core.util.TimeUtils;
import com.core.util.log.Log;
import com.core.util.time.TimerHandler;
import com.core.util.time.TimerService;
import com.core.util.udp.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by jgreco on 5/31/15.
 */
public class HeartbeatUDPConnector implements UDPSocketReadWriteListener, TimerHandler {
    private static final String APP_DELIMITER ="&" ;
    private final ByteStringBuffer stringBuffer= new ByteStringBuffer(2*Mold64UDPPacket.MTU_SIZE);

    private final SimpleReadWriteUDPSocket socket;
    private final Log log;

    private final HeartbeatVirtualMachine vm;

    private final TimerService timers;
    private final int secondsTimeout;

    private boolean hasWrittenHeader = false;

    public HeartbeatUDPConnector(Log log,
                                 UDPSocketFactory socketFactory,
                                 TimerService timers,
                                 String intf,
                                 String sendHost,
                                 short sendPort,
                                 String requestHost,
                                 short requestPort,
                                 int secondsTimeout,
                                 HeartbeatVirtualMachine vm) throws IOException {
        this.log = log;

        this.secondsTimeout = secondsTimeout;
        this.timers = timers;
        timers.scheduleTimer(secondsTimeout * TimeUtils.NANOS_PER_SECOND, this);
        this.socket = new SingleReadWriteUDPSocket(socketFactory.createReadWriteUDPSocket(this), intf, requestHost, sendPort, requestPort);
        this.vm = vm;


    }

    public void open() throws IOException {

        socket.open();
        socket.enableRead(true);
    }

    public void close() {
        socket.close();
    }

    @Override
    public void onDatagram(ReadWriteUDPSocket clientSocket, ByteBuffer datagram, InetSocketAddress addr) {

        hasWrittenHeader = false;
    }

    @Override
    public void onWriteAvailable(WritableUDPSocket clientSocket) {
        log.info(log.log().add("Heartbeat write available"));
    }

    @Override
    public void onWriteUnavailable(WritableUDPSocket clientSocket) {
        log.info(log.log().add("Heartbeat write unavailable"));
    }

    @Override
    public void onTimer(int internalTimerID, int referenceData) {
        try {
            open();
        } catch (IOException e) {

        }

        timers.scheduleTimer(secondsTimeout * TimeUtils.NANOS_PER_SECOND, this);

        if (!socket.canWrite()) {
            log.info(log.log().add("Heartbeat Socket Cannot Write"));

            return;
        }

        if (!hasWrittenHeader) {
            writeApps(false);
            hasWrittenHeader = true;
        }


        writeApps(true);
    }

    private void writeApps(boolean data) {
        List<HeartbeatApp> apps = vm.getApps();
        for (int i=0; i<apps.size(); i++) {
            ((HeartbeatAppImpl)apps.get(i)).updateFields();
        }

        stringBuffer.clear();

        int i = 0;
        while (i < apps.size()) {
            ByteBuffer currentBufferToBeSent=stringBuffer.getByteBuffer();
            int lastPacketPosition = currentBufferToBeSent.position();

            stringBuffer.add(HeartBeatFieldIDEnum.CORE.toString());
            stringBuffer.addColon();
            stringBuffer.add(vm.getVMNameString());
            stringBuffer.addComma();
            HeartbeatAppImpl app = (HeartbeatAppImpl)apps.get(i);
            app.writeApp(stringBuffer);

            if (data) {
                app.writeData(stringBuffer);
            }
            stringBuffer.add(APP_DELIMITER);


            if (currentBufferToBeSent.position() > Mold64UDPPacket.MTU_SIZE) {
                currentBufferToBeSent.position(lastPacketPosition);
                if (!sendToSocket()) {
                    return;
                }
                stringBuffer.clear();
                i--;
            }
            else {
                i++;
            }
        }

        // write final packet
        if (apps.size() > 0) {
            sendToSocket();
        }
    }

    private boolean sendToSocket() {
        ByteBuffer buffer=stringBuffer.getUnderlyingBuffer();
        if (!socket.write(buffer)) {
            log.info(log.log().add("Error writing to heartbeat socket"));
            return false;
        }
        stringBuffer.clear();
        return true;
    }
}
