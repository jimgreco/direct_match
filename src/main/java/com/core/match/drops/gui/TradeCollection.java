package com.core.match.drops.gui;

import com.core.match.drops.DropCollection;
import com.core.match.drops.LinearCounter;
import com.core.match.drops.gui.msgs.GUITrade;
import com.core.match.msgs.MatchSecurityEvent;
import com.core.match.services.order.DisplayedOrder;
import com.core.match.services.security.*;
import com.core.match.services.trades.TradeService;
import com.core.match.services.trades.TradeServiceListener;

import static com.core.match.msgs.MatchConstants.STATICS_START_INDEX;

/**
 * Created by jgreco on 2/20/16.
 */
class TradeCollection extends DropCollection implements
        SecurityServiceListener<BaseSecurity>,
        TradeServiceListener<DisplayedOrder> {
    public TradeCollection(SecurityService<BaseSecurity> securities, TradeService<DisplayedOrder> trades, LinearCounter versionCounter, LinearCounter itemCounter) {
        super(versionCounter, itemCounter);

        securities.addListener(this);
        trades.addListener(this);
    }

    @Override
    public void onBond(Bond security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
        if (isNew) {
            GUITrade tradeItem = new GUITrade(itemCounter.incVersion(), security.getName());
            addVersion(tradeItem);
        }
    }

    @Override
    public void onMultiLegSecurityInstrument(MultiLegSecurity security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
        if (isNew) {
            GUITrade tradeItem = new GUITrade(itemCounter.incVersion(), security.getName());
            addVersion(tradeItem);
        }
    }

    @Override
    public void onTrade(long timestamp, int matchID, long execPrice, int execQty, DisplayedOrder associatedOrder, boolean aggressor) {
        if (!aggressor) {
            GUITrade trade = (GUITrade) getItem(associatedOrder.getSecurityID() - STATICS_START_INDEX);
            trade.setTime(timestamp);
            trade.setSide(associatedOrder.isBuy());
            trade.setQty(execQty);
            trade.setVol(trade.getVol() + execQty);
            trade.setPx(execPrice);
            trade.setMatchId(matchID);
            
            updateVersion(trade);
        }
    }

    @Override
    public void onMatch(long timestamp, int matchID, long execPrice, int execQty, short securityID) {
        // trade does it
    }
}
