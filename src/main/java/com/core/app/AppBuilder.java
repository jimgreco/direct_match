package com.core.app;

import com.core.app.heartbeats.HeartbeatSource;
import com.core.app.heartbeats.HeartbeatUDPConnector;
import com.core.app.heartbeats.HeartbeatVirtualMachineImpl;
import com.core.connector.Connector;
import com.core.connector.mold.rewindable.RewindLocation;
import com.core.match.msgs.MatchByteBufferDispatcher;
import com.core.match.msgs.MatchByteBufferMessages;
import com.core.match.msgs.MatchConstants;
import com.core.match.ouch2.factories.OUCHComponentFactory;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.log.LogManager;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerService;
import com.core.util.udp.UDPSocketFactory;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.util.Map;

/**
 * Created by jgreco
 */
public class AppBuilder {
    private final String coreName;
    private final String commonFileName;
    private final String fileName;
    private final TimeSource timeSource;
    private final LogManager logManager;
    private final Log log;

    private final UDPSocketFactory udpSocketFactory;
    private final TCPSocketFactory tcpSocketFactory;
    private final FileFactory fileFactory;
    private final TimerService timerService;

    private HeartbeatVirtualMachineImpl heartbeatVM;
    private HeartbeatUDPConnector heartbeatConnector;

    public AppBuilder(String coreName,
                      String commonFileName,
                      String fileName,
                      UDPSocketFactory udpSocketFactory,
                      TCPSocketFactory tcpSocketFactory,
                      FileFactory fileFactory,
                      TimerService timerService,
                      TimeSource timeSource,
                      LogManager logManager) {
        this.coreName = coreName;
        this.commonFileName = commonFileName;
        this.fileName = fileName;
        this.udpSocketFactory = udpSocketFactory;
        this.tcpSocketFactory = tcpSocketFactory;
        this.fileFactory = fileFactory;
        this.timerService = timerService;
        this.timeSource = timeSource;
        this.logManager = logManager;
        this.log = logManager.get("BUILDER");
    }

    public AppList parseApps() throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(commonFileName);
        doc.getDocumentElement().normalize();

        // <constants>
        // set constants in the MatchConstants class
        parseConstants(doc);

        // <heartbeats>
        // set constants in the MatchConstants class
        parseHeartbeats(doc);

        // <context>
        // apps require a command channel (write), event channel (read), and re-request channel (write)
        // multiple contexts allow you to run a sequencer and applications in the same VM
        Map<String, AppContext> contextMap = parseAppContexts(doc);

        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fileName);
        doc.getDocumentElement().normalize();

        // <app>
        // applications
        AppList appList = parseApps(doc, contextMap);
//        heartbeatConnector.open();
        return appList;
    }

    private void parseConstants(Document doc) {
        NodeList constantsNodeList = doc.getElementsByTagName("constants");
        Map<String, String> constants = new UnifiedMap<>();
        if (constantsNodeList.getLength() > 0) {
            constants = parseParams((Element) constantsNodeList.item(0));
        }

        for (Map.Entry<String, String> keyVal : constants.entrySet()) {
            log.info(log.log().add(keyVal.getKey()).add(": ").add(keyVal.getValue()));
            MatchConstants.setParam(keyVal.getKey(), keyVal.getValue());
        }
    }

    private void parseHeartbeats(Document doc) throws IOException {
        NodeList constantsNodeList = doc.getElementsByTagName("heartbeats");
        Map<String, String> params = new UnifiedMap<>();
        if (constantsNodeList.getLength() > 0) {
            params = parseParams((Element) constantsNodeList.item(0));
        }

        short port = Short.parseShort(params.get("Port"));
        String host = params.get("Host");
        String intf = params.get("Intf");

        String requestHost = params.get("RequestHost");
        short requestPort = Short.parseShort(params.get("RequestPort"));


        heartbeatVM = new HeartbeatVirtualMachineImpl(coreName);
        heartbeatConnector = new HeartbeatUDPConnector(
                logManager.get("HEARTBEAT"),
                udpSocketFactory,
                timerService,
                intf,
                host,
                port,
                requestHost,
                requestPort,
                5,
                heartbeatVM);
    }

    private AppList parseApps(Document doc, Map<String, AppContext> contextMap) throws Exception {
        Map<String, Connector> connectorsAdded = new UnifiedMap<>();

        AppList appList = new AppList(logManager);
        NodeList appNodes = doc.getElementsByTagName("app");
        for (int i=0; i<appNodes.getLength(); i++) {
            Node appNode = appNodes.item(i);
            if (appNode.getNodeType() == Node.ELEMENT_NODE) {
                Element appElement = (Element) appNode;

                String instanceName = appElement.getAttribute("name");
                char num1 = instanceName.charAt(instanceName.length() - 3);
                char num2 = instanceName.charAt(instanceName.length() - 2);
                char num3 = instanceName.charAt(instanceName.length() - 1);
                if (num1 < '0' || num1 > '9' || num2 < '0' || num2 > '9' || num3 < 'A' || num3 > 'Z') {
                    throw new CommandException("Invalid name '" + instanceName + "'. Needs to end in a char");
                }
                String contribName = instanceName.substring(0, instanceName.length() - 1);

                String path = appElement.getAttribute("path");
                String contextName = appElement.getAttribute("context");

                AppContext appContext = contextMap.get(contextName);
                if (appContext == null) {
                    throw new CommandException("Invalid app context: " + contextName);
                }

                Connector connector = connectorsAdded.get(appContext.getName());
                if (connector == null) {
                    connector = appContext.buildConnector();

                    if (connector != null) {
                        appList.addCommands(appContext.getName(), connector);
                        connectorsAdded.put(appContext.getName(), connector);
                        HeartbeatSource conSource = (HeartbeatSource) connector;
                        conSource.onHeartbeatRegister(heartbeatVM.addApp(contextName, conSource));
                    }
                }

                Map<String, String> keyValues = parseParams(appElement);
                Application application = appList.add(instanceName, contribName, path, appContext, keyValues);
                application.onHeartbeatRegister(heartbeatVM.addApp(instanceName, application));
            }
        }

        for (Connector connector : connectorsAdded.values()) {
            connector.open();
        }

        return appList;
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

    private Map<String, AppContext> parseAppContexts(Document doc) {
        Map<String, AppContext> contextMap = new UnifiedMap<>();
        NodeList contextNodes = doc.getElementsByTagName("context");
        for (int i=0; i<contextNodes.getLength(); i++) {
            Node contextNode = contextNodes.item(i);
            if (contextNode.getNodeType() == Node.ELEMENT_NODE) {
                Element contextElement = (Element) contextNode;
                String contextName = contextElement.getAttribute("name");

                MatchByteBufferDispatcher dispatcher = new MatchByteBufferDispatcher(new MatchByteBufferMessages());
                MatchByteBufferMessages messages = new MatchByteBufferMessages();
                OUCHComponentFactory ouchAdapterFactory = new OUCHComponentFactory();
                AppContext appContext = new AppContext(
                        contextName,
                        udpSocketFactory,
                        tcpSocketFactory,
                        fileFactory,
                        timerService,
                        dispatcher,
                        messages,
                        timeSource,
                        logManager, ouchAdapterFactory);
                contextMap.put(contextName, appContext);

                NodeList paramNodes = contextElement.getElementsByTagName("param");
                for (int j=0; j<paramNodes.getLength(); j++) {
                    Node paramNode = paramNodes.item(j);
                    if (paramNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element paramElement = (Element) paramNode;
                        String name = paramElement.getAttribute("name");
                        String value = paramElement.getAttribute("value");
                        appContext.addKeyValuePair(name, value);
                    }
                }
                
                NodeList rewindNodes = contextElement.getElementsByTagName("rewind");
                for(int j=0; j<rewindNodes.getLength(); j++) {
                	Node rewindNode = rewindNodes.item(j);
                	if(rewindNode.getNodeType() == Node.ELEMENT_NODE) {
                		Element paramElement = (Element) rewindNode;
                		String host = paramElement.getAttribute("host");
                		String port = paramElement.getAttribute("port");
                		appContext.addRewindLocation(new RewindLocation(host, Short.parseShort(port)));
                	}
                }
            }
        }
        return contextMap;
    }
}
