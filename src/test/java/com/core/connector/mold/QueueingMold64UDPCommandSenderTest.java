package com.core.connector.mold;

import com.core.util.log.SystemOutLog;
import com.core.util.time.SimulatedTimeSource;
import com.core.util.time.SystemTimeSource;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerService;
import com.core.util.time.TimerServiceImpl;
import com.core.util.udp.StubReadWriteUDPSocket;
import com.core.util.udp.StubWritableUDPSocket;
import com.core.util.udp.UDPSocketFactory;
import com.core.util.udp.UDPSocketReadWriteListener;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class QueueingMold64UDPCommandSenderTest {

    private String name;
    private SystemOutLog log;
    private TimerService timerService;
    private String INTF;
    private String host;
    private short port;
    private SimulatedTimeSource simulatedTime;
    protected UDPSocketFactory socketFactory;
    private ByteBuffer message1;
    private ByteBuffer message2;


    protected TimeSource time;
    private StubWritableUDPSocket udpSocket;


    private QueueingMold64UDPCommandSender createTarget() throws IOException {
        return new QueueingMold64UDPCommandSender(name,log,socketFactory,
                timerService,INTF,host,port);
    }
    @Before
    public void setUp() throws IOException {
        name="Test";
        simulatedTime = new SimulatedTimeSource();
        time =  new SystemTimeSource();
        log=new SystemOutLog("test",name,time);
        socketFactory=mock(UDPSocketFactory.class);
        udpSocket=new StubWritableUDPSocket();
        when(socketFactory.createWritableUDPSocket(any(UDPSocketReadWriteListener.class))).thenReturn(udpSocket);
        host="test";
        port=1;
        timerService=new TimerServiceImpl(log,time);
    }

    public void createMessage() {
        message1 = ByteBuffer.allocate(1000);
        message2 = ByteBuffer.allocate(1000);

    }



    @Test
    public void onSend_canWriteFalse_cannotSend() throws Exception {
        createMessage();
        udpSocket.setNoWrite(true);

        QueueingMold64UDPCommandSender target=createTarget();
        target.add(message1);
        assertFalse(target.onSend());
    }

    @Test
    public void onSend_canWriteTrue_canSend() throws Exception {
        createMessage();
        udpSocket.setNoWrite(false);

        udpSocket.bind(name,host,port);
        QueueingMold64UDPCommandSender target=createTarget();
        target.onSessionDefinition("20140815AA");
        target.onActive();
        target.add(message1);
        assertTrue(target.onSend());
    }


    @Test
    public void onSend_AnotherMessageInFlightToSequencerRetrying_returnFalse() throws Exception {
        createMessage();
        udpSocket.setNoWrite(false);
        udpSocket.bind(name,host,port);
        QueueingMold64UDPCommandSender target=createTarget();
        target.onSessionDefinition("20140815AA");
        target.onActive();
        target.add(message1);
        target.onTimer(0, 1000);
        assertFalse(target.onSend());


    }

    @Test
    public void onCommandCleared_timerFiredOneMessageInQueue_retryOneTime() throws Exception {
        createMessage();
        udpSocket.setNoWrite(false);
        udpSocket.bind(name, host, port);
        QueueingMold64UDPCommandSender target=createTarget();
        target.onSessionDefinition("20140815AA");
        target.onActive();
        target.add(message1);
        target.onTimer(0, 1000);
        assertEquals(1, target.getRetry());

    }

    @Test
    public void onCommandCleared_timerFiredTwoMessageInQueue_retryTwoTimesWithTwoTimerCreated() throws Exception {
        createMessage();
        udpSocket.setNoWrite(false);
        udpSocket.bind(name, host, port);
        QueueingMold64UDPCommandSender target=createTarget();
        target.onSessionDefinition("20140815AA");
        target.onActive();
        target.add(message1);
        target.add(message2);

        target.onTimer(0, 1);
        assertEquals(1, target.getRetry());
        target.onTimer(0, 2);
        assertEquals(2, target.getRetry());
    }

    @Test
    public void onCommandCleared_timerFiredOneMessageInQueueAndAllCommandsClearedInvoked_retryResetWithRemainingMessageSent() throws Exception {
        createMessage();
        udpSocket.setNoWrite(false);
        udpSocket.bind(name, host, port);
        QueueingMold64UDPCommandSender target=createTarget();
        target.onSessionDefinition("20140815AA");
        target.onActive();
        target.add(message1);
        target.add(message2);

        target.onTimer(0, 1);
        assertEquals(1, target.getRetry());
        target.onCommandCleared();
        assertEquals(0, target.getRetry());
    }
}