package com.core.match.ouch.controller;

import com.core.match.ouch.msgs.OUCHConstants;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.MultiLegSecurity;
import com.core.util.log.Log;

import static com.core.match.msgs.MatchConstants.QTY_MULTIPLIER;

/**
 * Created by hli on 6/30/16.
 */
public class OrderQuantityValidator implements QuantityValidator {
    private final Log log;
    public OrderQuantityValidator (Log log){
        this.log=log;

    }
    @Override
    public boolean validateQuantity(BaseSecurity baseSecurity, int qty){
        //Check qty is positive
        if ( qty <= 0 )
        {
            log.debug(log.log().add("Rejected: Negative Qty: ").add(qty));
            return false;
        }

        //Check Qty is in round lots
        if (qty % baseSecurity.getLotSize() != 0)
        {
            log.debug(log.log().add("Rejected: Qty: ").add(qty));
            return false;
        }

        //SPREAD qty is in multiples of the front leg ratio
        if (baseSecurity.isSpread() && qty % ((MultiLegSecurity)baseSecurity).getLeg1Size() != 0)
        {
            log.debug(log.log().add("Rejected Spread Qty not in round log of front leg: ").add(qty));
            return false;
        }

        //BUTTERFLY qty is in multiples of the middle leg ratio
        if (baseSecurity.isButterfly() && qty % ((MultiLegSecurity)baseSecurity).getLeg2Size() != 0)
        {
            log.debug(log.log().add("Rejected butterfly Qty not in round log of front leg: ").add(qty));
            return false;
        }
        return true;
    }
}
