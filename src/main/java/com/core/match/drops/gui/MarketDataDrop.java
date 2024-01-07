package com.core.match.drops.gui;

import com.core.app.AppConstructor;
import com.core.app.Param;
import com.core.connector.Connector;
import com.core.connector.Dispatcher;
import com.core.match.drops.VersionedDropBase;
import com.core.match.drops.LinearCounter;
import com.core.match.services.book.MatchDisplayedPriceLevelBookService;
import com.core.match.services.book.MatchDisplayedPriceLevelBookServiceRoundingType;
import com.core.match.services.events.SystemEventService;
import com.core.match.services.order.DisplayedOrder;
import com.core.match.services.order.DisplayedOrderService;
import com.core.match.services.order.OrderService;
import com.core.match.services.security.*;
import com.core.match.services.trades.TradeService;
import com.core.util.log.Log;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerService;

import java.io.IOException;


/**
 * Created by jgreco on 2/18/16.
 */
public class MarketDataDrop extends VersionedDropBase {
    @AppConstructor
    public MarketDataDrop(Log log,
                          Dispatcher dispatcher,
                          TimerService timers,
                          TCPSocketFactory factory,
                          TimeSource time,
                          Connector connector,
                          SecurityService<BaseSecurity> securities,
                          SystemEventService systems,
                          @Param(name = "Name") String name,
                          @Param(name = "Port") int port,
                          @Param(name = "TimeoutMS") int timeOutMS,
                          @Param(name = "Levels") int levels,
                          @Param(name = "Levels32") int levels32) throws IOException {
        super(log, time, timers, factory, connector, name, timeOutMS, port);

        OrderService<DisplayedOrder> orders = OrderService.create(DisplayedOrder.class, log, dispatcher);
        dispatcher.subscribe(orders);

        DisplayedOrderService<DisplayedOrder> displayedOrderService = new DisplayedOrderService<>(orders, log);
        TradeService<DisplayedOrder> trades = new TradeService<>(orders);

        MatchDisplayedPriceLevelBookService bookServiceNone= new MatchDisplayedPriceLevelBookService(displayedOrderService, securities, log, MatchDisplayedPriceLevelBookServiceRoundingType.NONE);
        MatchDisplayedPriceLevelBookService bookServiceIDBS=new MatchDisplayedPriceLevelBookService(displayedOrderService, securities, log, MatchDisplayedPriceLevelBookServiceRoundingType.IDBS);

        LinearCounter itemCounter = new LinearCounter();
        addCollection(new MiscCollection(securities, systems, versionCounter, itemCounter ));
        addCollection(new MarketDataCollection(securities, bookServiceNone,versionCounter, itemCounter, levels, false));
        addCollection(new MarketDataCollection(securities, bookServiceIDBS, versionCounter, itemCounter, levels32, true));
        addCollection(new TradeCollection(securities, trades, versionCounter, itemCounter));
    }
}
