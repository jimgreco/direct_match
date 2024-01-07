package com.core.match.itch.client;

import com.core.connector.ByteBufferDispatcher;
import com.core.connector.soup.SoupBinTCPClientAdapter;
import com.core.match.itch.msgs.ITCHBaseDispatcher;
import com.core.match.itch.msgs.ITCHByteBufferDispatcher;
import com.core.match.itch.msgs.ITCHByteBufferMessages;
import com.core.match.itch.msgs.ITCHCommonEvent;
import com.core.match.msgs.MatchConstants;
import com.core.match.ouch.client.OUCHClientFill;
import com.core.match.ouch.client.OUCHClientOrder;
import com.core.match.ouch.msgs.OUCHAcceptedEvent;
import com.core.match.ouch.msgs.OUCHBaseDispatcher;
import com.core.match.ouch.msgs.OUCHByteBufferDispatcher;
import com.core.match.ouch.msgs.OUCHByteBufferMessages;
import com.core.match.ouch.msgs.OUCHCancelCommand;
import com.core.match.ouch.msgs.OUCHCommonEvent;
import com.core.match.ouch.msgs.OUCHConstants;
import com.core.match.ouch.msgs.OUCHFillEvent;
import com.core.match.ouch.msgs.OUCHOrderCommand;
import com.core.match.ouch.msgs.OUCHReplaceCommand;
import com.core.match.ouch.msgs.OUCHReplacedEvent;
import com.core.match.ouch.msgs.OUCHTradeConfirmationEvent;
import com.core.match.util.MatchPriceUtils;
import com.core.nio.SelectorService;
import com.core.services.bbo.BBOBook;
import com.core.util.PriceUtils;
import com.core.util.log.Log;
import com.core.util.log.SystemOutLog;
import com.core.util.time.SystemTimeSource;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.primitive.LongObjectHashMap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 * Created by jgreco on 7/1/15.
 */
public class ITCHOUCHClients {

    static LongObjectHashMap<OUCHClientOrder> myOrders = new LongObjectHashMap<>();
    static LongObjectHashMap<OUCHClientOrder> myLiveOrders = new LongObjectHashMap<>();
    static LongObjectHashMap<OUCHClientOrder> myFilledOrders = new LongObjectHashMap<>();
    static FastList<OUCHClientFill> myFills = new FastList<>();

    public static class ITCHMe implements
            ITCHBaseDispatcher.ITCHBeforeListener,
            OUCHBaseDispatcher.OUCHBeforeListener {
        private final SystemOutLog log;

        public ITCHMe(SystemOutLog log) {
            this.log = log;
        }

        @Override
        public void onITCHBeforeListener(ITCHCommonEvent msg) {
            log.info(log.log().add(msg.toString()));
        }

        @Override
        public void onOUCHBeforeListener(OUCHCommonEvent msg) {
            log.info(log.log().add("RX ").add(msg.toString()));
            switch (msg.getMsgType()) {
                case OUCHConstants.Messages.Accepted: {
                    OUCHClientOrder order = new OUCHClientOrder((OUCHAcceptedEvent)msg);
                    myOrders.put(msg.getClOrdID(), order);
                    myLiveOrders.put(msg.getClOrdID(), order);
                    break;
                }
                case OUCHConstants.Messages.Replaced: {
                    OUCHReplacedEvent evt = (OUCHReplacedEvent)msg;
                    OUCHClientOrder order = myOrders.removeKey(evt.getOldClOrdId());
                    myLiveOrders.removeKey(evt.getOldClOrdId());
                    order.replaced(evt.getClOrdID(),
                                   evt.getQty(),
                                   evt.getPrice()
                    );
                    myOrders.put(evt.getClOrdID(), order);
                    myLiveOrders.put(evt.getClOrdID(), order);
                    break;
                }
                case OUCHConstants.Messages.Canceled: {
                    OUCHClientOrder order = myLiveOrders.removeKey(msg.getClOrdID());
                    if (order.getCumQty()>0) {
                        myFilledOrders.put(msg.getClOrdID(), order);
                    }
                    break;
                }
                case OUCHConstants.Messages.TradeConfirmation: {
                	OUCHTradeConfirmationEvent evt = ( OUCHTradeConfirmationEvent ) msg;
                	System.out.println( evt );
                	break;
                }
                case OUCHConstants.Messages.Fill: {
                    OUCHFillEvent evt = (OUCHFillEvent)msg;
                    OUCHClientOrder order = myOrders.get(evt.getClOrdID());
                    order.fill(evt.getExecutionQty());
                    myFills.add(new OUCHClientFill(
                            evt.getMatchID(), 
                            evt.getClOrdID(),
                            myOrders.get(evt.getClOrdID()).getSecurity(),
                            evt.getExecutionQty(),
                            evt.getExecutionPrice()));
                    if (order.isFilled()) {
                        myLiveOrders.removeKey(evt.getClOrdID());
                        myFilledOrders.put(evt.getClOrdID(), order);
                    }
                    break;
                }
                case OUCHConstants.Messages.Rejected: {
                	myOrders.removeKey(msg.getClOrdID());
                	break;
                }
                default:
                    break;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        SystemTimeSource timeSource = new SystemTimeSource();

        SystemOutLog log = new SystemOutLog("CORE03-1", "LOG", timeSource);
        SelectorService select = new SelectorService(log, timeSource);

        //
        // OUCH Setup
        //
        SystemOutLog ouchLog = new SystemOutLog("CORE03-1", "OUCH01", timeSource);
        ouchLog.setDebug(true);
        OUCHByteBufferMessages ouchMessages = new OUCHByteBufferMessages();
        OUCHByteBufferDispatcher ouchDispatcher = new OUCHByteBufferDispatcher(ouchMessages);
        SoupBinTCPClientAdapter ouchAdapter = new SoupBinTCPClientAdapter(ouchLog, select, select, "127.0.0.1", 6005, ouchDispatcher, true, "OUCHUN", "OUCHPW");
       // ouchAdapter.subscribe(new ITCHMe(log));

        //
        // ITCH Setup
        //
        SystemOutLog itchLog = new SystemOutLog("CORE03-1", "ITCH01", timeSource);
        ITCHByteBufferMessages itchMessages = new ITCHByteBufferMessages();
        ByteBufferDispatcher itchDispatcher = new ITCHByteBufferDispatcher(itchMessages);
   //     SoupBinTCPClientAdapter itchAdapter = new  (itchLog, select, select, "core04.directmatchx.com", 10134, itchDispatcher, false, "IONT01", "IONPW");

        SoupBinTCPClientAdapter itchAdapter = new SoupBinTCPClientAdapter(itchLog, select, select, "127.0.0.1", 6014, itchDispatcher, false, "UN", "PW");
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
                ouchAdapter.open();
                sleep1(log);
                //itchAdapter.login("ITCHUN", "ITCHPW");
                //ouchAdapter.login("OUCHUN", "OUCHPW");
                sleep1(log);
                System.out.println(itchAdapter.toString());
                System.out.println(ouchAdapter.toString());

                Scanner scanner = new Scanner(System.in);
                while (true) {
                    log.info(log.log().add("stopHB | order <security> <side> <qty> <display> <price> <trader> | replace <clOrdId> <qty> <display> <price> | cancel <clOrdId> | cancelall | securities | top | orders <security> | book <security> | myorders | myliveorders | myfilledorders | myfills | logout"));

                    String input = scanner.nextLine();
                    String[] inputs = input.split(" ");
                    String fn = inputs[0];
                    
                    try
                    {
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
                                    log.info(log.log().add(String.format("%3s",next.getSecurityName())).add(" ").add(next.toString()));
                                }
                                break;
                            }
                            case "disconnect": {
                                ouchAdapter.close();
                                break;
                            }

                            case "logout": {
                                itchAdapter.logOut();
                                break;
                            }

                            case "order": {
                                OUCHOrderCommand order = ouchMessages.getOUCHOrderCommand();
                                int clOrdId = Math.abs(new Random().nextInt());
                                order.setClOrdID(clOrdId);
                                log.info(log.log().add("Sending order with clOrdId: ").add(clOrdId));
                                order.setSecurity(inputs[1]);
                                order.setSide(inputs[2].toUpperCase().startsWith("B") ? OUCHConstants.Side.Buy : OUCHConstants.Side.Sell);
                                order.setQty(Integer.valueOf(inputs[3]).intValue() * 1000000);
                                //order.setMaxDisplayedQty(Integer.valueOf(inputs[4]).intValue() * 1000000);
                                order.setPrice(PriceUtils.toLong(Double.valueOf(inputs[5]).doubleValue(), MatchConstants.IMPLIED_DECIMALS));
                                order.setTrader(inputs.length > 6 ? inputs[6] : "JLEVIDY");
                                order.setTimeInForce(OUCHConstants.TimeInForce.DAY);
                                ByteBuffer rawBuffer = order.getRawBuffer().slice();
                                rawBuffer.limit(order.getLength());
                                ouchAdapter.send(rawBuffer);
                                break;
                            }
                            case "cancel": {
                                OUCHCancelCommand command = ouchMessages.getOUCHCancelCommand();
                                command.setClOrdID(Integer.valueOf(inputs[1]).intValue());
                                ByteBuffer rawBuffer = command.getRawBuffer().slice();
                                rawBuffer.limit(command.getLength());
                                ouchAdapter.send(rawBuffer);
                                break;
                            }
                            case "replace": {
                                OUCHReplaceCommand command = ouchMessages.getOUCHReplaceCommand();
                                command.setClOrdID(Long.valueOf(inputs[1]).longValue());
                                command.setNewQty(Integer.valueOf(inputs[2]).intValue() * 1000000);
                                //command.setNewMaxDisplayedQty(Integer.valueOf(inputs[3]).intValue() * 1000000);
                                command.setNewPrice(PriceUtils.toLong(Double.valueOf(inputs[4]).doubleValue(), MatchConstants.IMPLIED_DECIMALS));
                                int clOrdId = Math.abs(new Random().nextInt());
                                command.setNewClOrdID(clOrdId);
                                log.info(log.log().add("Sending order with clOrdId: ").add(clOrdId));
                                ByteBuffer rawBuffer = command.getRawBuffer().slice();
                                rawBuffer.limit(command.getLength());
                                ouchAdapter.send(rawBuffer);
                                break;
                            }
                            case "cancelall": {
                                for (OUCHClientOrder order : myLiveOrders) {
                                    OUCHCancelCommand command = ouchMessages.getOUCHCancelCommand();
                                    command.setClOrdID(order.getClOrdID());
                                    ByteBuffer rawBuffer = command.getRawBuffer().slice();
                                    rawBuffer.limit(command.getLength());
                                    ouchAdapter.send(rawBuffer);
                                }
                                break;
                            }

                            case "myorders":
                                for (OUCHClientOrder order : myOrders) {
                                    logOrderInfo(log, order);
                                }
                                break;
                            case "myliveorders":
                                for (OUCHClientOrder order : myLiveOrders) {
                                    logOrderInfo(log, order);
                                }
                                break;
                            case "myfilledorders":
                                for (OUCHClientOrder order : myFilledOrders) {
                                    logOrderInfo(log, order);
                                }
                                break;
                            case "myfills":
                                for (OUCHClientFill fill : myFills) {
                                    log.info(log.log().
                                            add("matchId=").add(fill.getMatchId()).
                                            add(" clOrdId=").add(fill.getClOrdId()).
                                            add(" security=").add(fill.getSecurity()).
                                            add(" qty=").add(Double.toString(fill.getQty()/1000000.0)).
                                            add(" price=").add(MatchPriceUtils.to32ndPrice(fill.getPrice())));
                                }
                                break;
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

    private static void logOrderInfo(SystemOutLog log, OUCHClientOrder order) {
        log.info(log.log().
                add("clOrdId=").add(order.getClOrdID()).
                add(" trader=").add(order.getTrader()).
                add(" side=").add(order.getSide()).
                add(" security=").add(order.getSecurity()).
                add(" price=").add(MatchPriceUtils.to32ndPrice(order.getPrice())).
                add(" qty=").add(Double.toString(order.getQty()/1000000.0)).
                add(" filled=").add(Double.toString((order.getQty()-order.getRemainingQty())/1000000.0)).
                add(" remaining=").add(Double.toString(order.getRemainingQty()/1000000.0)).
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
}
