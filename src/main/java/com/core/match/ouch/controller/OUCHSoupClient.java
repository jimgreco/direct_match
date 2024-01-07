package com.core.match.ouch.controller;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.core.connector.soup.SoupBinTCPClientAdapter;
import com.core.connector.soup.msgs.SoupLoginAcceptedEvent;
import com.core.connector.soup.msgs.SoupLoginEventListener;
import com.core.connector.soup.msgs.SoupLoginRejectedEvent;
import com.core.match.msgs.MatchConstants;
import com.core.match.ouch.msgs.OUCHByteBufferDispatcher;
import com.core.match.ouch.msgs.OUCHByteBufferMessages;
import com.core.match.ouch.msgs.OUCHCancelCommand;
import com.core.match.ouch.msgs.OUCHConstants;
import com.core.match.ouch.msgs.OUCHOrderCommand;
import com.core.match.ouch.msgs.OUCHReplaceCommand;
import com.core.nio.SelectorService;
import com.core.util.PriceUtils;
import com.core.util.log.Log;
import com.core.util.log.SystemOutLog;
import com.core.util.time.SystemTimeSource;
import com.gs.collections.impl.map.mutable.UnifiedMap;

/**
 * Created by hli on 10/16/15.
 */
public class OUCHSoupClient {


    public static void main(String[] args) throws IOException {
        /**
         *example args= CLIENT_CORE ouch01a 127.0.0.1 6005 OUCHUN OUCHPW
         */

        if(args.length!=1){
            System.out.println("args= [Setting XML File]. ");
            return;
        }
        String commonXmlFileName = args[0];
        Document doc=null;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(commonXmlFileName);
        } catch (ParserConfigurationException |SAXException |IOException e) {
            System.out.println("unable to build app using "+commonXmlFileName+". Exiting..");
            System.out.println(e.getMessage());
        }

        NodeList constantsNodeList = doc.getElementsByTagName("OUCHClientSimulator");
        Map<String, String> params = new UnifiedMap<>();
        if (constantsNodeList.getLength() > 0) {
            params = parseParams((Element) constantsNodeList.item(0));
        }

        String coreName = params.get("CoreName");
        String instanceName = params.get("ApplicationName");
        String host = params.get("TargetHost");
        short port = Short.parseShort(params.get("TargetPort"));
        String ouch_un = params.get("Username");
        String ouch_pw = params.get("Password");
        Boolean ignoreHeartbeat=Boolean.parseBoolean(params.get("IgnoreHeartbeat"));




        SystemTimeSource timeSource = new SystemTimeSource();

        SystemOutLog log = new SystemOutLog(coreName, "LOG", timeSource);
        SelectorService select = new SelectorService(log, timeSource);

        //
        // OUCH Setup
        //
        SystemOutLog ouchLog = new SystemOutLog(coreName, instanceName, timeSource);
        ouchLog.setDebug(true);
        log.warn(log.log().add("Username:").add(ouch_un));
        OUCHByteBufferMessages ouchMessages = new OUCHByteBufferMessages();
        OUCHByteBufferDispatcher ouchDispatcher = new OUCHByteBufferDispatcher(ouchMessages);
        SoupBinTCPClientAdapter ouchAdapter = new SoupBinTCPClientAdapter(ouchLog, select, select, host, port, ouchDispatcher, ignoreHeartbeat, ouch_un, ouch_pw);
        ouchAdapter.setLoginListener(new SoupLoginEventListener() {
            @Override
            public void onSoupLoginAccepted(SoupLoginAcceptedEvent msg) {
                log.info(log.log().add("login accepted"));
            }
            
            @Override
            public void onSoupLoginRejected(SoupLoginRejectedEvent msg) {
            	log.error(log.log().add("login rejected"));
            }
        });

        Executors.newSingleThreadExecutor().submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                sleep1(log);
                ouchAdapter.open();
                sleep1(log);
                System.out.println(ouchAdapter.toString());

                Scanner scanner = new Scanner(System.in);
                while (true) {
                    log.info(log.log().add("stopHB | order <clOrdId> <security> <side> <qty(MM)> <display(MM)> <price> <trader> <TIF(IOC or empty for day)>| replace <ClOrdId> <NewClOrdID> <qtyMM> <displayMM> <price> | cancel <clOrdId>"));
                    log.info(log.log().add("E.g. order test2 2Y Buy 1 0 99 TWTRADER1"));
                    String input = scanner.nextLine();
                    String[] inputs = input.split(" ");
                    String fn = inputs[0];

                    try
                    {
                        switch (fn) {
                            case "disconnect": {
                                ouchAdapter.close();
                                break;
                            }

                            case "order": {

                                if(9!=inputs.length && 8!=inputs.length){
                                    log.info(log.log().add("Order command has wrong number of args. Expect 8 for nonIOC or 9 for IOC order. Rec: ").add(inputs.length));

                                }else{
                                    OUCHOrderCommand order = ouchMessages.getOUCHOrderCommand();
                                    long clOrdId = toAscii(inputs[1]);
                                    order.setClOrdID(clOrdId);
                                    log.info(log.log().add("Sending order with clOrdId: ").add(inputs[1]));
                                    order.setSecurity(inputs[2]);
                                    order.setSide(inputs[3].toUpperCase().startsWith("B") ? OUCHConstants.Side.Buy : OUCHConstants.Side.Sell);
                                    order.setQty(Integer.valueOf(inputs[4]).intValue() * 1000000);
                                    long price =PriceUtils.toLong(Double.valueOf(inputs[6]).doubleValue(), MatchConstants.IMPLIED_DECIMALS);
                                    order.setPrice(price);
                                    order.setTrader(inputs[7].trim());
                                    if ((inputs.length==9 && "IOC".equals(inputs[8].toUpperCase().trim()))){
                                        order.setTimeInForce(OUCHConstants.TimeInForce.IOC);
                                    }else{
                                        order.setTimeInForce(OUCHConstants.TimeInForce.DAY);
                                    }
                                    ByteBuffer rawBuffer = order.getRawBuffer().slice();
                                    rawBuffer.limit(order.getLength());

                                    ouchAdapter.send(rawBuffer);
                                }
                                break;
                             }
                            case "cancel": {
                                OUCHCancelCommand command = ouchMessages.getOUCHCancelCommand();
                                if(2!=inputs.length){
                                    log.info(log.log().add("Cancel command has wrong number of args. Expect 2  Rec: ").add(inputs.length));

                                }else{
                                    long clOrdId = toAscii(inputs[1]);

                                    command.setClOrdID(clOrdId);
                                    ByteBuffer rawBuffer = command.getRawBuffer().slice();
                                    rawBuffer.limit(command.getLength());
                                    log.info(log.log().add("Sending order with clOrdId: ").add(clOrdId));
                                    ouchAdapter.send(rawBuffer);
                                }

                                break;
                            }
                            case "replace": {
                                if(6!=inputs.length ){
                                    log.info(log.log().add("Replace command has wrong number of args. Expect 6  Rec: ").add(inputs.length));

                                }else{
                                    OUCHReplaceCommand command = ouchMessages.getOUCHReplaceCommand();
                                    long clOrdId = toAscii(inputs[1]);
                                    command.setClOrdID(clOrdId);
                                    command.setNewQty(Integer.valueOf(inputs[3]).intValue() * 1000000);
                                    command.setNewPrice(PriceUtils.toLong(Double.valueOf(inputs[5]).doubleValue(), MatchConstants.IMPLIED_DECIMALS));
                                    long updatedClOrdId = toAscii(inputs[2]);
                                    command.setNewClOrdID(updatedClOrdId);
                                    log.info(log.log().add("Sending order replace with new clOrdId: ").add(inputs[2]));
                                    ByteBuffer rawBuffer = command.getRawBuffer().slice();
                                    rawBuffer.limit(command.getLength());
                                    ouchAdapter.send(rawBuffer);
                                }

                                break;
                            }

                            default:
                                log.error(log.log().add("Unknown command: ").add(input));
                        }
                    }
                    catch (Exception e)
                    {
                        log.error(log.log().add("Exception occurred: ").add(e));
                    }
                }
            }
        });

        select.run();
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
    static void sleep1(Log log) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error(log.log().add("Exception occurred: ").add(e));
        }
    }

    public static long toAscii(String s){
        StringBuilder sb = new StringBuilder();
        long asciiInt;
        for (int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            asciiInt = (int)c;
            sb.append(asciiInt);
        }
        return Long.parseLong(sb.toString());
    }

}
