package com.core.match.monitors.md;

import com.core.app.AppConstructor;
import com.core.app.Exposed;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.connector.Dispatcher;
import com.core.match.MatchApplication;
import com.core.match.msgs.MatchSecurityEvent;
import com.core.match.services.book.MatchBBOBookService;
import com.core.match.services.book.MatchDisplayedPriceLevelBookService;
import com.core.match.services.book.MatchDisplayedPriceLevelBookServiceRoundingType;
import com.core.match.services.order.DisplayedOrder;
import com.core.match.services.order.DisplayedOrderService;
import com.core.match.services.order.OrderService;
import com.core.match.services.quote.Quote;
import com.core.match.services.quote.VenueQuoteService;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.MultiLegSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.security.SecurityServiceListener;
import com.core.match.services.security.SecurityType;
import com.core.match.util.MatchPriceUtils;
import com.core.match.util.MessageUtils;
import com.core.services.bbo.BBOBook;
import com.core.util.TimeUtils;
import com.core.util.log.Log;
import com.gs.collections.impl.list.mutable.FastList;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.core.match.msgs.MatchConstants.Venue.Bloomberg;
import static com.core.match.msgs.MatchConstants.Venue.InteractiveData;

/**
 * Created by jgreco on 10/7/15.
 */
public class MarketDataMonitor extends MatchApplication {
    private final VenueQuoteService quotes;
    private final MatchBBOBookService bbos;
    private final SecurityService<BaseSecurity> securities;
    private final List<MarketDataRow> rows = new FastList<>();
    private final Map<String, MarketDataRow> nameRowMap = new HashMap<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;

    @AppConstructor
    public MarketDataMonitor(Log log,
                             Dispatcher dispatcher,
                             SecurityService<BaseSecurity> secs) {
        super(log);

        securities = secs;

        quotes = new VenueQuoteService(secs);
        dispatcher.subscribe(quotes);

        OrderService<DisplayedOrder> orders = OrderService.create(DisplayedOrder.class, log, dispatcher);
        dispatcher.subscribe(orders);

        DisplayedOrderService<DisplayedOrder> displayedOrderService = new DisplayedOrderService<>(orders, log);
        MatchDisplayedPriceLevelBookService priceLevelBookService = new MatchDisplayedPriceLevelBookService(displayedOrderService, securities, log, MatchDisplayedPriceLevelBookServiceRoundingType.NONE);
        bbos = new MatchBBOBookService(priceLevelBookService, securities);

        add("CoreTime", 0, 0);
        add("SourceTime", 0, 1);
        add("Updates", 0, 2);

        securities.addListener(new SecurityServiceListener<BaseSecurity>() {
            @Override
            public void onBond(Bond security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
                if (isNew) {
                    add(security.getName(), msg.getCouponAsDouble(), msg.getMaturityDate());
                }
            }

            @Override
            public void onMultiLegSecurityInstrument(MultiLegSecurity security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
                //TODO: MultiLeg Instrument not currently supported
            }
        });
    }

    private void add(String name, double coupon, int maturityDate) {
        MarketDataRow row = new MarketDataRow(name, coupon, maturityDate);
        rows.add(row);
        nameRowMap.put(name, row);
    }

    @Override
    public void onAddHeartbeatFields(HeartbeatFieldRegister fieldRegister) {
    }

    @Override
    public void onUpdateHeartbeatFields(HeartbeatFieldUpdater fieldUpdater) {
    }

    @Exposed(name="bbo")
    public List<MarketDataRow> getBBO() {
        for (BaseSecurity sec : securities) {
            if(sec.isBond()) {
                Bond security=(Bond)sec;
                Quote bbrg = quotes.getQuote(Bloomberg, security);
                Quote idc = quotes.getQuote(InteractiveData, security);
                BBOBook bbo = bbos.get(security);

                MarketDataRow row = nameRowMap.get(security.getName());
                row.dmBidQty = qty(bbo.getBidQty());
                row.dmAskQty = qty(bbo.getOfferQty());
                row.dmBid = MatchPriceUtils.to32ndPrice(bbo.getBidPrice());
                row.dmAsk = MatchPriceUtils.to32ndPrice(bbo.getOfferPrice());
                row.idcBid = MatchPriceUtils.to32ndPrice(idc.getBidPrice());
                row.idcAsk = MatchPriceUtils.to32ndPrice(idc.getOfferPrice());
            }
        }

        MarketDataRow coreTime = nameRowMap.get("CoreTime");
        coreTime.dmBid = formatTime(bbos.getLastUpdateTime());
        coreTime.idcBid = formatTime(quotes.getCoreTime(InteractiveData));

        MarketDataRow sourceTime = nameRowMap.get("SourceTime");
        sourceTime.dmBid = "";
        sourceTime.idcBid = formatTime(quotes.getSourceTime(InteractiveData));

        MarketDataRow updates = nameRowMap.get("Updates");
        updates.dmBid = Integer.toString(bbos.getNumUpdates());
        updates.idcBid = Integer.toString(quotes.getUpdates(InteractiveData));

        return rows;
    }

    private double qty(int bidQty) {
        return bidQty/1000.0;
    }

    private String formatTime(long timestamp) {
        return TimeUtils.toLocalDateTime(timestamp, MessageUtils.zoneID()).format(formatter);

    }
}
