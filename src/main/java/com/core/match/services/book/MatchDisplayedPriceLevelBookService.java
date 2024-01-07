package com.core.match.services.book;

import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchSecurityEvent;
import com.core.match.services.order.DisplayedOrder;
import com.core.match.services.order.DisplayedOrderService;
import com.core.match.services.order.DisplayedOrderServiceListener;
import com.core.match.services.order.Order;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.MultiLegSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.security.SecurityServiceListener;
import com.core.match.services.security.SecurityType;
import com.core.match.util.MatchPriceUtils;
import com.core.services.price.PriceLevelBook;
import com.core.services.price.PriceLevelBookService;
import com.core.util.log.Log;

/**
 * User: jgreco
 */

public class MatchDisplayedPriceLevelBookService extends PriceLevelBookService implements
        SecurityServiceListener<BaseSecurity>,
        DisplayedOrderServiceListener<DisplayedOrder> {

    private final MatchDisplayedPriceLevelBookServiceRoundingType type;
    private final SecurityService<BaseSecurity> securities;

    public MatchDisplayedPriceLevelBookService(DisplayedOrderService < DisplayedOrder > orders, SecurityService < BaseSecurity > securities, Log log, MatchDisplayedPriceLevelBookServiceRoundingType type) {
        super(log, MatchConstants.IMPLIED_DECIMALS, MatchConstants.QTY_MULTIPLIER);
        this.type = type;
        this.securities = securities;
        orders.addListener(this);
        securities.addListener(this);
    }

    public PriceLevelBook getBook(BaseSecurity security) {
        return getBook(security.getID());
    }

    @Override
    public void onBond(Bond security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
        if (isNew) {
            addBook(security.getID(), security.getName());
        }
    }

    @Override
    public void onMultiLegSecurityInstrument(MultiLegSecurity security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
        if (isNew) {
            addBook(security.getID(), security.getName());
        }
    }

    @Override
    public void onDisplayedOrder(DisplayedOrder order, long timestamp) {
        long price = getPrice(order, order.getPrice());
        addLevel(order.getSecurityID(),
                timestamp,
                order.isBuy(),
                price,
                order.getRemainingQty(),
                order.getExternalOrderID(),
                price != order.getPrice());
    }

    @Override
    public void onDisplayedFill(DisplayedOrder order, int fillQty, long fillPrice, int matchID, long timestamp) {
        long price = getPrice(order, order.getPrice());
        removeLevel(order.getSecurityID(),
                timestamp,
                order.isBuy(),
                price,
                fillQty,
                order.getRemainingQty() <= 0 ? order.getExternalOrderID() : 0,
                price != order.getPrice());
    }

    @Override
    public void onDisplayedReduced(DisplayedOrder order, long oldPrice, int qtyReduced, boolean dead, long timestamp) {
            long price = getPrice(order, oldPrice);
            removeLevel(order.getSecurityID(),
                timestamp,
                order.isBuy(),
                price,
                qtyReduced,
                dead ? order.getExternalOrderID() : 0,
                price != order.getPrice());
    }

    private long getPrice(Order order, long price) {
        switch (type) {
            case EIGHTHS:
                price = MatchPriceUtils.roundEighth(price, order.isBuy());
                break;
            case IDBS:
                if (securities.isBond(order.getSecurityID())) {
                    int term = securities.getBond(order.getSecurityID()).getTerm();
                    if (term >= 2 && term <= 5) {
                        price = MatchPriceUtils.roundQuarter(price, order.isBuy());
                    }
                    else if (term > 5) {
                        price = MatchPriceUtils.roundHalf(price, order.isBuy());
                    }
                }
                break;
            default:
                break;
        }
        return price;
    }
}
