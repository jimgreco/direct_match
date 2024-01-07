package com.core.match.services.trader;

import java.util.List;

import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchTraderEvent;
import com.core.match.msgs.MatchTraderListener;
import com.core.services.StaticsFactory;
import com.core.services.StaticsServiceBase;
import com.gs.collections.impl.list.mutable.FastList;

/**
 * Created by johnlevidy on 5/26/15.
 */
public class TraderService<T extends Trader> extends StaticsServiceBase<T> implements
        MatchTraderListener {
    private final StaticsFactory<T> factory;
    private final List<TraderServiceListener<T>> listeners = new FastList<>();

    public static TraderService<Trader> create() {
        return new TraderService<>(Trader::new);
    }
    
    public void addListener(TraderServiceListener<T> listener) {
        listeners.add(listener);
    }

    public TraderService(StaticsFactory<T> factory) {
        super(MatchConstants.STATICS_START_INDEX);
        this.factory = factory;
    }

    @Override
    public void onMatchTrader(MatchTraderEvent msg) {
        boolean isNew = false;
        T trader = get(msg.getTraderID());
        if (trader == null) {
            trader = factory.create(msg.getTraderID(), msg.getNameAsString());
            add(trader);
            isNew = true;
        }

        trader.setAccountID(msg.getAccountID());
        trader.setFatFinger2YQtyLimit(msg.getFatFinger2YLimit());
        trader.setFatFinger3YQtyLimit(msg.getFatFinger3YLimit());
        trader.setFatFinger5YQtyLimit(msg.getFatFinger5YLimit());
        trader.setFatFinger7YQtyLimit(msg.getFatFinger7YLimit());
        trader.setFatFinger10YQtyLimit(msg.getFatFinger10YLimit());
        trader.setFatFinger30YQtyLimit(msg.getFatFinger30YLimit());

        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onTrader(trader, msg, isNew);
        }
    }
}
