package com.core.connector.mold;

import com.core.app.CommandException;
import com.core.connector.BaseCommandSender;
import com.core.util.TimeUtils;
import com.core.util.datastructures.DMQueue;
import com.core.util.log.Log;
import com.core.util.pool.ObjectPool;
import com.core.util.time.TimerHandler;
import com.core.util.time.TimerService;
import com.core.util.udp.*;
import com.core.util.udp.ReadWriteUDPSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class QueueingMold64UDPCommandSender extends BaseCommandSender implements
        UDPSocketWriteListener,
        TimerHandler {
    private static final long SEND_TIMER = 100;
    private final String upstreamMulticastGroup;
    private Mold64PacketWriter packet;


    private final SimpleWriteUDPSocket socket;

    private int retry;
    private boolean failOnSetActive = true;

    protected final TimerService timerService;
    private int sendTimer;

    private DMQueue<LinkableByteBuffer> messageQueue;
    private ObjectPool<LinkableByteBuffer> bufferObjectPool;

    public QueueingMold64UDPCommandSender(String name,
                                          Log log,
                                          UDPSocketFactory socketFactory,
                                          TimerService timerService,
                                          String intf,
                                          String upstreamMulticastGroup,
                                          short port) throws IOException {
        super(log, name);
        this.upstreamMulticastGroup=upstreamMulticastGroup;
        this.socket = new SingleWriteUDPSocket(socketFactory.createWritableUDPSocket(this), intf, upstreamMulticastGroup, port);
        this.timerService = timerService;
        this.packet = new Mold64UDPPacket(log);
        messageQueue = new DMQueue<>();
        bufferObjectPool=new ObjectPool<>(log,"",LinkableByteBuffer::new,1000);

    }


    @Override
    public String getDestinationAddress() {
        return upstreamMulticastGroup;
    }

    @Override
    public boolean canWrite() {
        return socket.canWrite();
    }

    @Override
    protected void onActive() {
        close();

        try {
            socket.open();
        }
        catch (IOException e) {
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
        log.info(log.log().add("Queued messages in flight in passive: ").add(getQueueSize()));
    }


    @Override
    public void add(ByteBuffer msgBuffer) {
        if (log.isDebugEnabled()) {
            log.debug(log.log().add("Adding to Sender Queue. #Msg in Q ").add(messageQueue.size()).add(" ").add(canWrite()));
        }

        LinkableByteBuffer linkedByteBuffer = bufferObjectPool.create();
        linkedByteBuffer.copy(msgBuffer);
        messageQueue.add(linkedByteBuffer);
    }

    @Override
    protected void onInit() {
        // we rebuild the entire packet each time
    }

    @Override
    protected boolean onSend() {
        if (!canWrite() || messageQueue.size()==0 || sendTimer != TimerService.NULL_VALUE) {
            log.warn(log.log().add("Unable to send packet. CanWrite=").add(canWrite()).add(",QueueSize=").add(messageQueue.size()).add(",Timer#=").add(sendTimer));
            return false;
        }

        if (!sendFirstMessageInQueue()) {
            log.warn(log.log().add("Unable to sendFirstMessageInQueue. CanWrite=").add(canWrite()).add(",QueueSize=").add(messageQueue.size()).add(",Timer=").add(sendTimer));
            return false;
        }

        retry = 0;
        return true;
    }

    private boolean sendFirstMessageInQueue() {
        if(messageQueue.size() == 0){
            return false;
        }
        ByteBuffer currentMsg= messageQueue.peek().getValue();
        currentMsg.mark();
        packet.clear();
        packet.init(getSession());
        packet.addMessage(currentMsg);
        currentMsg.reset();
        ByteBuffer datagram = packet.getDatagram();
        datagram.mark();
        datagram.reset();

        if (log.isDebugEnabled()) {
            log.debug(log.log().add("Sending first message in queue. QueueSize=").add(messageQueue.size()));
        }

        if (!socket.write(datagram)) {
            log.error(log.log().add("Could not write to UDP socket. Waiting for callback."));
            return false;
        }

        scheduleTimer();
        return true;
    }

    private void scheduleTimer() {
        timerService.cancelTimer(sendTimer);
        sendTimer = timerService.scheduleTimer(SEND_TIMER * TimeUtils.NANOS_PER_MILLI, this);
    }

    @Override
    public void onWriteAvailable(WritableUDPSocket clientSocket) {
        if(messageQueue.size()!=0) {
            onSend();
        }
        log.error(log.log().add("Able to write to UDP socket again. Waiting for callback."));

    }

    @Override
    public void onWriteUnavailable(WritableUDPSocket clientSocket) {
        log.error(log.log().add("Could not write to UDP socket again. Waiting for callback."));

    }

    @Override
    public void onTimer(int internalTimerID, int referenceData) {
        if (internalTimerID != sendTimer) {
            log.error(log.log().add("onTimer internalTimerID != sendTimer.  Possible that multiple timers on going!!"));
        }
        // reset timer???
        sendTimer = TimerService.NULL_VALUE;

        // nothing else to send
        if (!canWrite() || messageQueue.size()==0) {
            log.error(log.log().add("onTimer do nothing: CanWrite").add(canWrite()).add("msg queue size").add(messageQueue.size()));
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

        log.warn(log.log().add("Retrying to send first message in queue: ")
                .add("Retry ").add(retry).add(" Timer: ").add(internalTimerID));

        if (!sendFirstMessageInQueue()) {
            return;
        }
    }

    @Override
    protected void onCommandCleared() {
        sendTimer = timerService.cancelTimer(sendTimer);
        LinkableByteBuffer roundTripByteBuffer=messageQueue.remove();
        bufferObjectPool.delete(roundTripByteBuffer);
        if (messageQueue.size()!=0) {
                onSend();
        }
    }

    protected int getRetry(){
        return retry;
    }

    public int getQueueSize() {
        return messageQueue.size();
    }
}
