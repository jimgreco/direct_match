package com.core.match.drops.gui;

import com.core.app.AppConstructor;
import com.core.app.Param;
import com.core.connector.Connector;
import com.core.connector.Dispatcher;
import com.core.match.drops.LinearCounter;
import com.core.match.drops.VersionedDropBase;
import com.core.match.services.book.BookPositionService;
import com.core.match.services.book.MatchLimitOrder;
import com.core.match.services.order.DisplayedOrderService;
import com.core.match.services.order.OrderService;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.SecurityService;
import com.core.util.log.Log;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerService;

/**
 * Created by jgreco on 2/22/16.
 */
public class OrderBookDrop extends VersionedDropBase {
    @AppConstructor
    public OrderBookDrop(Log log,
                         TimeSource time,
                         TimerService timers,
                         TCPSocketFactory factory,
                         Connector connector,
                         Dispatcher dispatcher,
                         SecurityService<BaseSecurity> securities,
                         @Param(name = "Name") String name,
                         @Param(name = "Port") int port,
                         @Param(name = "TimeoutMS") int timeOutMS,
                         @Param(name = "Levels") int levels) {
        super(log, time, timers, factory, connector, name, timeOutMS, port);

        OrderService<MatchLimitOrder> orderService = OrderService.create(MatchLimitOrder.class, log, dispatcher);
        dispatcher.subscribe(orderService);

        DisplayedOrderService<MatchLimitOrder> displayedOrderService = new DisplayedOrderService<>(orderService, log);

        BookPositionService bookService = new BookPositionService(displayedOrderService, securities, levels);

        addCollection(new OrderBookCollection(versionCounter, new LinearCounter(), bookService));
    }
}
