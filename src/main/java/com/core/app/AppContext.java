package com.core.app;

import com.core.connector.BaseCommandSender;
import com.core.connector.ByteBufferDispatcher;
import com.core.connector.Connector;
import com.core.connector.Dispatcher;
import com.core.connector.file.FileConnector;
import com.core.connector.mold.Mold64UDPConnector;
import com.core.connector.mold.QueueingMold64UDPCommandSender;
import com.core.connector.mold.rewindable.MemoryBackedMessageStore;
import com.core.connector.mold.rewindable.RewindLocation;
import com.core.connector.mold.rewindable.RewindableMold64UDPConnector;
import com.core.connector.soup.SoupAppConnector;
import com.core.connector.soup.SoupCommandSender;
import com.core.match.MatchByteBufferCommandSender;
import com.core.match.MatchCommandSender;
import com.core.match.msgs.MatchByteBufferDispatcher;
import com.core.match.msgs.MatchMessages;
import com.core.match.ouch2.factories.OUCHComponentFactory;
import com.core.match.ouch2.factories.OUCHFactory;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.book.MatchBBOBookService;
import com.core.match.services.book.MatchDisplayedPriceLevelBookService;
import com.core.match.services.book.MatchDisplayedPriceLevelBookServiceRoundingType;
import com.core.match.services.contributor.Contributor;
import com.core.match.services.contributor.ContributorService;
import com.core.match.services.events.SystemEventService;
import com.core.match.services.order.DisplayedOrder;
import com.core.match.services.order.DisplayedOrderService;
import com.core.match.services.order.OrderService;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.log.LogManager;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerService;
import com.core.util.udp.UDPSocketFactory;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import com.gs.collections.impl.set.mutable.UnifiedSet;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Created by jgreco
 */
public class AppContext {
    private final LogManager logManager;
    private final Map<Class<?>, Object> clsParamMap = new UnifiedMap<>();
    private final Map<Class<?>, Object> factoryClsParamMap = new UnifiedMap<>();

    private final Map<String, String> keyValuePairs = new UnifiedMap<>();
    private final Set<RewindLocation> rewindLocations = new UnifiedSet<>();
    private final String name;
    private final MatchByteBufferDispatcher dispatcher;

    private Connector connector;
    private SecurityService<BaseSecurity> securities;
    private ContributorService<Contributor> contributors;
    private AccountService<Account> accounts;
    private TraderService<Trader> traders;
    private SystemEventService systems;
    private OUCHComponentFactory ouchAdapterFactory;
    private MatchBBOBookService bbos;

    public AppContext(String name,
                      UDPSocketFactory udpSocketFactory,
                      TCPSocketFactory tcpSocketFactory,
                      FileFactory fileFactory,
                      TimerService timerService,
                      MatchByteBufferDispatcher dispatcher,
                      MatchMessages messages,
                      TimeSource timeSource,
                      LogManager logManager,
                      OUCHComponentFactory ouchAdapterFactory) {
        this.name = name;
        this.logManager = logManager;
        this.dispatcher = dispatcher;
        this.ouchAdapterFactory = ouchAdapterFactory;

        // These are all implementations of inherited by event service
        clsParamMap.put(UDPSocketFactory.class, udpSocketFactory);
        clsParamMap.put(TCPSocketFactory.class, tcpSocketFactory);
        clsParamMap.put(FileFactory.class, fileFactory);
        clsParamMap.put(TimerService.class, timerService);

        clsParamMap.put(MatchByteBufferDispatcher.class, dispatcher);
        clsParamMap.put(Dispatcher.class, dispatcher);
        clsParamMap.put(ByteBufferDispatcher.class, dispatcher);
        clsParamMap.put(MatchMessages.class, messages);
        clsParamMap.put(TimeSource.class, timeSource);

        //Implementations for Factories
        factoryClsParamMap.put(OUCHFactory.class, ouchAdapterFactory);
    }

    public String getName() {
        return name;
    }

    public void addKeyValuePair(String key, String value) {
        keyValuePairs.put(key, value);
    }
    
    public void addRewindLocation(RewindLocation rewindLocation) {
    	rewindLocations.add(rewindLocation);
    }

    @SuppressWarnings("unchecked")
	public <T> T getParameter(Class<T> cls) {
        if (cls.equals(SecurityService.class)) {
            if (securities == null) {
                Log log = logManager.get("SECURITIES");
                securities = SecurityService.create(log, dispatcher);
                dispatcher.subscribe(securities);
            }
            return (T)securities;
        }
        else if (cls.equals(AccountService.class)) {
            if (accounts == null) {
                accounts = AccountService.create();
                dispatcher.subscribe(accounts);
            }
            return (T)accounts;
        }
        else if (cls.equals(MatchBBOBookService.class)) {
            if(bbos==null) {
                Log log = logManager.get("REFBBOService");
                OrderService<DisplayedOrder> orders = OrderService.create(DisplayedOrder.class, log, dispatcher);
                dispatcher.subscribe(orders);

                DisplayedOrderService<DisplayedOrder> displayedOrderService = new DisplayedOrderService<>(orders, log);
                MatchDisplayedPriceLevelBookService priceLevelBookService = new MatchDisplayedPriceLevelBookService(displayedOrderService, securities, log, MatchDisplayedPriceLevelBookServiceRoundingType.NONE);
                displayedOrderService.addListener(priceLevelBookService);
                bbos = new MatchBBOBookService(priceLevelBookService, securities);
            }
            return (T)bbos;
        }
        else if (cls.equals(ContributorService.class)) {
            if (contributors == null) {
                contributors = ContributorService.create();
                dispatcher.subscribe(contributors);
            }
            return (T)contributors;
        }
        else if (cls.equals(TraderService.class)) {
            if (traders == null) {
                traders = TraderService.create();
                dispatcher.subscribe(traders);
            }
            return (T)traders;
        }
        else if (cls.equals(SystemEventService.class)) {
            if (systems == null) {
                systems = new SystemEventService();
                dispatcher.subscribe(systems);
            }
            return (T)systems;
        }
        else if (cls.equals(OUCHFactory.class)) {
            if (ouchAdapterFactory == null) {
                ouchAdapterFactory = new OUCHComponentFactory();
            }
            return (T) ouchAdapterFactory;
        }
        return (T) clsParamMap.get(cls);
    }

    public String getParameter(String key) {
        return keyValuePairs.get(key);
    }

    public Connector buildConnector() throws IOException {
        if (connector != null) {
            return connector;
        }

        String type = keyValuePairs.get("Type");

        if (type == null) {
            return null;
        }
        else if (type.equalsIgnoreCase("soup")) {
            String coreHost = keyValuePairs.get("CoreHost");
            short corePort = Short.parseShort(keyValuePairs.get("CorePort"));
            String username = keyValuePairs.get("Username");
            String password = keyValuePairs.get("Password");
            connector = new SoupAppConnector(
                    logManager.get(name),
                    getParameter(TCPSocketFactory.class),
                    getParameter(TimerService.class),
                    coreHost,
                    corePort,
                    false,
                    username,
                    password,
                    getParameter(MatchByteBufferDispatcher.class));
        }
        else if (type.equalsIgnoreCase("file")) {
            String fileName = keyValuePairs.get("FileName");
            connector = new FileConnector(getParameter(FileFactory.class), getParameter(MatchByteBufferDispatcher.class), getParameter(TimerService.class), fileName);
        }
        else if (type.equalsIgnoreCase("mold")) {
            short eventPort = Short.parseShort(keyValuePairs.get("EventPort"));
            String downstreamMulticastGroup = keyValuePairs.get("DownstreamMulticastGroup");
            String intf = keyValuePairs.get("Intf");
            
            connector = new RewindableMold64UDPConnector(
            		logManager.get(name), 
            		getParameter(MatchByteBufferDispatcher.class), 
            		getParameter(UDPSocketFactory.class), 
            		getParameter(TCPSocketFactory.class), 
            		new MemoryBackedMessageStore(logManager.get(name)),
            		downstreamMulticastGroup, 
            		intf, 
            		rewindLocations,
            		eventPort);
        }

        clsParamMap.put(Connector.class, connector);
        return connector;
    }

    public MatchCommandSender buildSender(String senderName, Log log) throws IOException {
        String type = keyValuePairs.get("Type");
        BaseCommandSender sender;

        if (type == null) {
            throw new CommandException("Tried to builder a sender without a type");
        }
        else if (type.equalsIgnoreCase("mold")) {
            sender = new QueueingMold64UDPCommandSender(
                    senderName,
                    log,
                    getParameter(UDPSocketFactory.class),
                    getParameter(TimerService.class),
                    getParameter("Intf"),
                    getParameter("UpstreamMulticastGroup"),
                    Short.parseShort(getParameter("CommandPort")));
        }
        else if (type.equalsIgnoreCase("soup")) {
            sender = new SoupCommandSender(log, senderName, (SoupAppConnector) connector);
        }
        else {
            throw new CommandException("Tried to builder a sender for an invalid type: " + type);
        }

        MatchByteBufferCommandSender matchCmdSender = new MatchByteBufferCommandSender(log, getParameter(TimeSource.class), sender);
        dispatcher.subscribe(matchCmdSender);
        connector.addSessionSourceListener(sender);
        return matchCmdSender;
    }
}
