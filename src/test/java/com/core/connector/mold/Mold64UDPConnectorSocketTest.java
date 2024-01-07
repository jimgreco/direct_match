package com.core.connector.mold;

import com.core.app.heartbeats.StubHeartbeatApp;
import com.core.match.msgs.MatchBaseDispatcher;
import com.core.match.msgs.MatchByteBufferDispatcher;
import com.core.match.msgs.MatchByteBufferMessages;
import com.core.nio.SelectorService;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;
import com.core.util.log.SystemOutLog;
import com.core.util.time.SystemTimeSource;
import com.core.util.udp.*;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Random;

/**
 * User: jgreco
 */
@Category(value=ReadWriteUDPSocket.class)
public class Mold64UDPConnectorSocketTest {
    UDPSocketReadWriteListener listener = new UDPSocketReadWriteListener() {
        @Override
        public void onDatagram(ReadWriteUDPSocket clientSocket, ByteBuffer datagram, InetSocketAddress addr) {
            // nothing
        }

        @Override
        public void onWriteAvailable(WritableUDPSocket clientSocket) {
            // nothing
        }

        @Override
        public void onWriteUnavailable(WritableUDPSocket clientSocket) {

        }
    };

    ByteBuffer buffer = ByteBuffer.allocate(1500);
    short port;
    String host = "224.0.0.1";

    @Before
    public void before() {
    	String property = System.getProperty("os.name");
    	Assume.assumeTrue(!property.contains("Windows"));
    	port = (short)(new Random().nextInt(20000) + 5000);
    }

    @SuppressWarnings("boxing")
	@Test
    public void testBasic() throws IOException {
        Selector selector = Selector.open();

        InetAddress group = InetAddress.getByName(host);
        NetworkInterface networkInterface = NetworkInterface.getByName("en0"); 
        InetSocketAddress multicastTarget = new InetSocketAddress(host, port);

        DatagramChannel writer = DatagramChannel.open(StandardProtocolFamily.INET);
        writer.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        writer.join(group, networkInterface);
        writer.configureBlocking(false);

        DatagramChannel reader = DatagramChannel.open(StandardProtocolFamily.INET);
        reader.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        reader.bind(new InetSocketAddress(port));
        reader.join(group, networkInterface);
        //reader.connect(new InetSocketAddress(port));
        //reader.connect(multicastTarget);
        reader.configureBlocking(false);
        reader.register(selector, SelectionKey.OP_READ);

        byte[] send = "Hello World".getBytes();
        ByteBuffer tempBuffer = ByteBuffer.allocateDirect(20).put(send);
        tempBuffer.flip();
        writer.send(tempBuffer, multicastTarget);
        Assert.assertEquals(1, selector.select(1));

        tempBuffer.clear();
        SocketAddress receive = reader.receive(tempBuffer);
        System.out.println(receive);
        tempBuffer.flip();
        tempBuffer.get(send);
        Assert.assertEquals("Hello World", new String(send));
    }

    @Test
    public void testSendMessage() throws IOException, InterruptedException {
        SystemTimeSource time = new SystemTimeSource();
        SelectorService select = new SelectorService(new SystemOutLog("CORE03-1", "TEST", time), time);

        SimpleReadWriteUDPSocket writer = new SingleReadWriteUDPSocket(select.createReadWriteUDPSocket(listener), "en0", host, port);
        writer.open();

        StubHeartbeatApp con01 = new StubHeartbeatApp("CON01");

        MatchByteBufferDispatcher dispatcher = new MatchByteBufferDispatcher(new MatchByteBufferMessages());
        Log log = new SystemOutLog("CORE03-1", "TEST", time);
        Mold64UDPConnector connector = new Mold64UDPConnector(log, select, "en0", host, port, dispatcher);
        connector.onHeartbeatRegister(con01);
        connector.open();

        final ByteBuffer[] recv = new ByteBuffer[1];
        dispatcher.subscribe((MatchBaseDispatcher.MatchAfterListener) msg -> recv[0] = msg.getRawBuffer());

        select.runOnce();
        String message = "XFOO BAR                                       ";

        setHeader(1, 1);

        // message 1
        buffer.putShort((short) message.length());
        BinaryUtils.copy(buffer, message);

        send(writer);
        Thread.sleep(1);
        select.runOnce();

        recv[0].position(Mold64UDPPacket.HEADER_LENGTH);
        Assert.assertEquals(message.length(), recv[0].getShort());
        Assert.assertEquals(message.length(), recv[0].remaining());
    }

    private void send(SimpleReadWriteUDPSocket writer) {
        // send
        buffer.flip();

        writer.write(buffer);
    }

    private void setHeader(int seq, int messages) {
        buffer.clear();
        Mold64UDPPacket.init(buffer, "20140815AA", seq, messages);
    }
}
