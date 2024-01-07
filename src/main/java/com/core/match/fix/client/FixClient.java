package com.core.match.fix.client;

import com.core.latencytesting.QuickFixApp;
import com.core.testharness.ouch.NullEventCounter;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import quickfix.ConfigError;
import quickfix.SessionID;
import quickfix.SessionSettings;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class FixClient
{

	public static void main( String args[] ) throws ConfigError
	{
        if(args.length!=1){
            System.out.println("args= [Setting XML File] ");
            return;
        }
        String commonXmlFileName = args[0];
        Document doc=null;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(commonXmlFileName);
        } catch (ParserConfigurationException |SAXException|IOException e) {
            System.out.println("unable to build app using "+commonXmlFileName+". Exiting..");
            System.out.println(e.getMessage());
        }

        NodeList constantsNodeList = doc.getElementsByTagName("FixClientSimulator");
        Map<String, String> params = new UnifiedMap<>();
        if (constantsNodeList.getLength() > 0) {
            params = parseParams((Element) constantsNodeList.item(0));
        }

        String fixOrderEntryHost = params.get("FixOrderEntryHost");
        String fixMarketDataHost = params.get("FixMarketDataHost");
        String fixSTPHost = params.get("FixSTPHost");

        short fixOrderEntryPort  = Short.parseShort(params.get("FixOrderEntryPort"));
        short fixMarketDataPort  = Short.parseShort(params.get("FixMarketDataPort"));
        short fixSTPPort  = Short.parseShort(params.get("FixSTPPort"));

        String fixOrderEntryApplicationName = params.get("FixOrderEntryApplicationName");
        String fixMarketDataApplicationName = params.get("FixMarketDataApplicationName");
        String fixSTPApplicationName = params.get("FixSTPApplicationName");


        String trader = params.get("Trader");

        Boolean enableOrderEntry=Boolean.parseBoolean(params.get("EnableOrderEntry"));
        Boolean enableMarketData=Boolean.parseBoolean(params.get("EnableMarketData"));
        Boolean enableFixSTP=Boolean.parseBoolean(params.get("EnableFixSTP"));

        short heartBeatInterval  = Short.parseShort(params.get("HeartBeatInterval"));
        short reconnectInterval  = Short.parseShort(params.get("ReconnectInterval"));

        SessionSettings settings = new SessionSettings();
        // general
        settings.setString("StartTime", "00:05:00");
        settings.setString("EndTime", "23:55:00");
        settings.setBool("UseLocalTime", true);
        settings.setBool("UseDataDictionary", true);
        settings.setString("FileStorePath", "/tmp");

        // client
        settings.setString("ConnectionType", "initiator");
        settings.setLong("HeartBtInt", heartBeatInterval);
        settings.setLong("ReconnectInterval", reconnectInterval);

        // order entry

            SessionID sessionID = new SessionID("FIX.4.4", fixOrderEntryApplicationName, "DMDEV");
        if(enableOrderEntry){
            settings.setString(sessionID, "SocketConnectHost", fixOrderEntryHost);
            settings.setLong(sessionID, "SocketConnectPort", fixOrderEntryPort);
            settings.setBool(sessionID, "ResetOnLogout", true);
            settings.setBool(sessionID, "ResetOnDisconnect", true) ;
        }



        // market data


            SessionID sessionID_MD = new SessionID("FIX.4.4", fixMarketDataApplicationName, "DMMDDEV");
        if(enableMarketData) {
        settings.setString(sessionID_MD, "SocketConnectHost", fixMarketDataHost);
            settings.setLong(sessionID_MD, "SocketConnectPort", fixMarketDataPort);
            settings.setBool(sessionID_MD, "ResetOnDisconnect", true);
            settings.setBool(sessionID_MD, "ResetOnLogout", true);
            settings.setString(sessionID_MD, "ResetSeqNumFlag", "Y");
        }

        // stp

            SessionID sessionID_STP = new SessionID("FIX.4.4", fixSTPApplicationName, "DMSTPDEV");
        if(enableFixSTP) {
            settings.setString(sessionID_STP, "SocketConnectHost", fixSTPHost);
            settings.setLong(sessionID_STP, "SocketConnectPort", fixSTPPort);
            settings.setBool(sessionID_STP, "ResetOnLogout", true);
            settings.setBool(sessionID_STP, "ResetOnDisconnect", true);
        }


        final QuickFixApp quickFixApp = new QuickFixApp(settings, new NullEventCounter());
        quickFixApp.setAccount(trader);
        System.out.println("Set up complete");
        System.out.println("order <side> <qty(MM)> <security> <price> <display(MM)> " +
                "| replace <clOrdId> <qtyMM> <price> <displayMM>| cancel <clOrdId> | securities | subscribe | quit" );
		Executors.newCachedThreadPool().submit(() -> {
            Scanner s = new Scanner(System.in);
            System.out.println("order <side> <qty(MM)> <security> <price> <display(MM)> " +
                    "| replace <clOrdId> <qtyMM> <price> <displayMM>| cancel <clOrdId> | securities | subscribe | quit" );
            while (true) {
                String next = s.nextLine();
                System.out.println(next);

                try {
                    String[] split = next.split(" ");
                    if (next.startsWith("order")) {
                        boolean buy = split[1].equalsIgnoreCase("buy");
                        int qty = Integer.parseInt(split[2], 10);
                        String security = split[3];
                        double price = Double.parseDouble(split[4]);
                        //int display = Integer.parseInt(split[5], 10);
                        long orderid = quickFixApp.sendOrder(sessionID, buy, qty, security, price);
                        System.out.println("Order sent. order id:" + orderid);
                    } else if (next.startsWith("cancel")) {
                        int orderId = Integer.parseInt(split[1], 10);
                        boolean result = quickFixApp.sendCancel(sessionID, orderId);
                        if (result) {
                            System.out.println("Cancelled order with order id:" + orderId);

                        } else {
                            System.out.println("Fail to cancel order with order id:" + orderId);

                        }
                    } else if (next.startsWith("replace")) {
                        int orderId = Integer.parseInt(split[1], 10);
                        int qty = Integer.parseInt(split[2], 10);
                        double price = Double.parseDouble(split[3]);
                        //int display = Integer.parseInt(split[4], 10);
                        long id=quickFixApp.sendReplace(sessionID, orderId, qty, price);
                        System.out.println("Replace sent. Order id:" + id);

                    } else if (next.startsWith("disconnect")) {
                        quickFixApp.stop();
                        Thread.sleep(10000);
                        quickFixApp.start();
                    } else if (next.startsWith("securities")) {
                        quickFixApp.sendSecurityListRequest(sessionID_MD);
                    } else if (next.startsWith("subscribe")) {
                        String security = split[1];
                        quickFixApp.sendMarketDataRequest(sessionID_MD, security);
                    } else if (next.equalsIgnoreCase("q") || next.equalsIgnoreCase("quit")) {
                        System.exit(0);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("ArrayIndexOutOfBoundsException:" + e.getMessage());
                }
            }
        });
		
		quickFixApp.start();
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
}
