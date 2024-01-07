package com.core.app.heartbeats.collector;

import com.core.app.heartbeats.HeartbeatVirtualMachine;
import com.core.util.BinaryUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jgreco on 5/30/15.
 */
public class HeartbeatCollector {
    private static final ObjectMapper mapper = new ObjectMapper();
    static HeartBeatMap newMap=new HeartBeatMap();




    public static void main(String[] args) throws IOException, InterruptedException {
        if(args.length!=1){
            System.out.println("args= [Common XML File] ");
            return;
        }
        String commonXmlFileName = args[0];
        Document doc=null;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(commonXmlFileName);
        } catch (ParserConfigurationException|SAXException e) {
            System.out.println("unable to build app using "+commonXmlFileName+". Exiting..");
            System.out.println(e.getMessage());
        }

        NodeList constantsNodeList = doc.getElementsByTagName("heartbeatcollector");
        Map<String, String> params = new UnifiedMap<>();
        if (constantsNodeList.getLength() > 0) {
            params = parseParams((Element) constantsNodeList.item(0));
        }

        short receiverPort  = Short.parseShort(params.get("receiverPort"));
        String receiverHost = params.get("receiverHost");
        String intf = params.get("Intf");
        short senderPort  = Short.parseShort(params.get("senderPort"));
        String senderHost = params.get("senderHost");
        short httpPort  = Short.parseShort(params.get("httpPort"));


        Map<String, HeartbeatVirtualMachine> vms = new ConcurrentHashMap<>();

        HttpServer server = HttpServer.create(new InetSocketAddress(httpPort), 0);
        server.createContext("/vm", new VMHandler(vms));
        server.createContext("/logins", new AppHandler(vms));
        server.setExecutor(null);
        server.start();

        recvHeartbeats(intf, receiverHost, receiverPort, senderHost, senderPort, vms);
    }

    private static class VMHandler implements HttpHandler {
        private final Map<String, HeartbeatVirtualMachine> vms;

        public VMHandler(Map<String, HeartbeatVirtualMachine> vms) {
            this.vms = vms;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            System.out.println("getHeartbeatStatusArrayForAllVMs  ");

            sendResponse(t, newMap.getHeartbeatStatusArrayForAllVMs());
        }
    }

    private static class AppHandler implements HttpHandler {
        private final Map<String, HeartbeatVirtualMachine> vms;

        public AppHandler(Map<String, HeartbeatVirtualMachine> vms) {
            this.vms = vms;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            System.out.println("getLoginsAllVMs  ");

            Object result=newMap.getLogins();
            if (result == null) {
                sendError(t, "No Login Data " );
                return;
            }
            sendResponse(t, result);
        }
    }

    @SuppressWarnings("unused")
	static void sendError(HttpExchange exch, String errorMessage) {

    }

    private static Map<String, String> parseParams(Element appElement) {
        Map<String, String> keyValues = new UnifiedMap<>();
        NodeList paramNodes = appElement.getElementsByTagName("param");
        for (int j=0; j<paramNodes.getLength(); j++) {
            Node paramNode = paramNodes.item(j);
            if (paramNode.getNodeType() == Node.ELEMENT_NODE) {
                Element paramElement = (Element) paramNode;
                String name1 = paramElement.getAttribute("name");
                String value1 = paramElement.getAttribute("value");
                keyValues.put(name1, value1);
            }
        }
        return keyValues;
    }

    static void sendResponse(HttpExchange exch, Object o) throws IOException {
        Map<String, String> map = queryToMap(exch.getRequestURI().getQuery());
        String callback = map.get("callback");
        String json = mapper.writeValueAsString(o);
        String response;
        if (callback != null) {
            response = callback + "(" + json + ")";
        }
        else {
            response = json;
        }

        exch.sendResponseHeaders(200, response.length());

        OutputStream os = exch.getResponseBody();
        os.write(response.getBytes());

        os.close();
    }

    public static Map<String, String> queryToMap(String query){
        Map<String, String> result = new HashMap<>();
        if (query == null) {
            return result;
        }
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length>1) {
                result.put(pair[0], pair[1]);
            }else{
                result.put(pair[0], "");
            }
        }
        return result;
    }

    private static void recvHeartbeats(String intf, String recvHost, int recvPort, String sendHost, int sendPort, Map<String, HeartbeatVirtualMachine> vms) throws IOException, InterruptedException {
        DatagramChannel recvChannel = getChannel(intf, recvHost);
        recvChannel.bind(new InetSocketAddress(recvPort));
        DatagramChannel sendChannel = getChannel(intf, sendHost);

        ByteBuffer buffer = ByteBuffer.allocate(1500);
        ByteBuffer wrap = ByteBuffer.wrap("GIMME".getBytes());

        sendChannel.send(wrap, new InetSocketAddress(sendHost, sendPort));
        Thread.sleep(1000);
        System.out.println("Heartbeat request sent");

        while (true) {
            buffer.clear();
            recvChannel.receive(buffer);
            buffer.flip();

            buffer.mark();
            // TODO: we should just set the position instead of doing this overhead since we aren't using the result anyway
            buffer.reset();

            if (buffer.remaining() < 3) {
                continue;
            }
            String heatbeatPayload=BinaryUtils.toString(buffer);

            newMap.addHeartBeatData(heatbeatPayload);

        }


    }

    private static DatagramChannel getChannel(String intf, String host) throws IOException {
        DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);
        channel.configureBlocking(true);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
        channel.socket().setReuseAddress(true);

        InetAddress sendGroup = InetAddress.getByName(host);
        NetworkInterface sendInterface = NetworkInterface.getByName(intf);
        channel.join(sendGroup, sendInterface);
        return channel;
    }
}
