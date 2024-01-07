package com.core.match.itch.client;

import com.core.connector.ByteBufferDispatcher;
import com.core.connector.soup.SoupBinTCPClientAdapter;
import com.core.match.itch.msgs.ITCHBaseDispatcher;
import com.core.match.itch.msgs.ITCHByteBufferDispatcher;
import com.core.match.itch.msgs.ITCHByteBufferMessages;
import com.core.match.itch.msgs.ITCHCommonEvent;
import com.core.match.ouch.client.OUCHClientFill;
import com.core.match.ouch.client.OUCHClientOrder;
import com.core.match.util.MatchPriceUtils;
import com.core.nio.SelectorService;
import com.core.services.bbo.BBOBook;
import com.core.util.log.Log;
import com.core.util.log.SystemOutLog;
import com.core.util.time.SystemTimeSource;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import com.gs.collections.impl.map.mutable.primitive.LongObjectHashMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 * Created by hli on 11/20/15.
 */
public class ITCHClient {


    static LongObjectHashMap<OUCHClientOrder> myOrders = new LongObjectHashMap<>();
    static LongObjectHashMap<OUCHClientOrder> myLiveOrders = new LongObjectHashMap<>();
    static LongObjectHashMap<OUCHClientOrder> myFilledOrders = new LongObjectHashMap<>();
    static FastList<OUCHClientFill> myFills = new FastList<>();

    public static class ITCHMe implements ITCHBaseDispatcher.ITCHBeforeListener {
        private final SystemOutLog log;

        public ITCHMe(SystemOutLog log) {
            this.log = log;
        }

        @Override
        public void onITCHBeforeListener(ITCHCommonEvent msg) {
            log.info(log.log().add(msg.toString()));
        }
    }

    public static void main(String[] args) throws IOException {
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

        NodeList constantsNodeList = doc.getElementsByTagName("ITCHClientSimulator");
        Map<String, String> params = new UnifiedMap<>();
        if (constantsNodeList.getLength() > 0) {
            params = parseParams((Element) constantsNodeList.item(0));
        }

        String targetHost = params.get("TargetHost");
        short targetPort = Short.parseShort(params.get("TargetPort"));
        String username = params.get("Username");
        String password = params.get("Password");
        Boolean ignoreHeartbeat=Boolean.parseBoolean(params.get("IgnoreHeartbeat"));

        SystemTimeSource timeSource = new SystemTimeSource();

        SystemOutLog log = new SystemOutLog("CORE03-1", "LOG", timeSource);
        SelectorService select = new SelectorService(log, timeSource);

        //
        // ITCH Setup
        //
        SystemOutLog itchLog = new SystemOutLog("CORE03-1", "ITCH01", timeSource);
        ITCHByteBufferMessages itchMessages = new ITCHByteBufferMessages();
        ByteBufferDispatcher itchDispatcher = new ITCHByteBufferDispatcher(itchMessages);

        SoupBinTCPClientAdapter itchAdapter = new SoupBinTCPClientAdapter(itchLog, select, select, targetHost, targetPort, itchDispatcher, ignoreHeartbeat, username, password);
        itchAdapter.subscribe(new ITCHMe(log));

        // ITCH book setup
        ITCHClientOrderService orderService = new ITCHClientOrderService(itchLog);
        itchAdapter.subscribe(orderService);
        ITCHClientSecurityService securityService = new ITCHClientSecurityService();
        itchAdapter.subscribe(securityService);
        ITCHClientLimitBookService bookService = new ITCHClientLimitBookService(securityService, orderService);
        ITCHClientPriceLevelBookService priceLevelBookService = new ITCHClientPriceLevelBookService(itchLog, securityService, orderService);
        ITCHClientBBOBookService bboService = new ITCHClientBBOBookService(priceLevelBookService, securityService);

        //log.setDebug(true);

        Executors.newSingleThreadExecutor().submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                sleep1(log);
                itchAdapter.open();
                sleep1(log);
                sleep1(log);
                System.out.println(itchAdapter.toString());

                Scanner scanner = new Scanner(System.in);
                while (true) {
                    log.info(log.log().add("stopHB | securities | top | orders <security> | book <security> | myorders | myliveorders | myfilledorders | myfills | logout"));

                    String input = scanner.nextLine();
                    String[] inputs = input.split(" ");
                    String fn = inputs[0];

                    try {
                        switch (fn) {
                            case "securities": {
                                Iterator<ITCHClientSecurity> secIt = securityService.iterator();
                                while (secIt.hasNext()) {
                                    ITCHClientSecurity next = secIt.next();
                                    log.info(log.log().add(next.getID()).add(" ").add(next.getName()));
                                }
                                break;
                            }
                            case "orders": {
                                ITCHClientSecurity security = getSecurity(itchLog, securityService, inputs);
                                if (security != null) {
                                    log.info(log.log().add(bookService.getBook(security.getID()).toString()));
                                }
                                break;
                            }
                            case "book": {
                                ITCHClientSecurity security = getSecurity(itchLog, securityService, inputs);
                                if (security != null) {
                                    log.info(log.log().add(priceLevelBookService.getBook(security.getID()).toString()));
                                }
                                break;
                            }
                            case "top": {
                                Iterator<BBOBook> bboIt = bboService.iterator();
                                while (bboIt.hasNext()) {
                                    BBOBook next = bboIt.next();
                                    log.info(log.log().add(String.format("%3s", next.getSecurityName())).add(" ").add(next.toString()));
                                }
                                break;
                            }

                            case "logout": {
                                itchAdapter.logOut();
                                break;
                            }

                            default:
                                log.error(log.log().add("Unknown command: ").add(input));
                        }
                    } catch (Exception e) {
                        log.error(log.log().add("Exception occurred: ").add(e));
                    }
                }
            }
        });

        select.run();
    }

    private static void logOrderInfo(SystemOutLog log, OUCHClientOrder order) {
        log.info(log.log().
                add("clOrdId=").add(order.getClOrdID()).
                add(" trader=").add(order.getTrader()).
                add(" side=").add(order.getSide()).
                add(" security=").add(order.getSecurity()).
                add(" price=").add(MatchPriceUtils.to32ndPrice(order.getPrice())).
                add(" qty=").add(Double.toString(order.getQty() / 1000000.0)).
                add(" filled=").add(Double.toString((order.getQty() - order.getRemainingQty()) / 1000000.0)).
                add(" remaining=").add(Double.toString(order.getRemainingQty() / 1000000.0)).
                add(" TIF=").add(order.getTIF()));
    }

    static ITCHClientSecurity getSecurity(Log log, ITCHClientSecurityService securityService, String[] inputs) {
        if (inputs.length < 2) {
            log.error(log.log().add("orders <security>"));
            return null;
        }
        String secName = inputs[1];
        ITCHClientSecurity security = securityService.get(secName);
        if (security == null) {
            log.error(log.log().add("Unknown security: ").add(secName));
            return null;
        }
        return security;
    }

    static void sleep1(Log log) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error(log.log().add("Exception occurred: ").add(e));
        }
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
