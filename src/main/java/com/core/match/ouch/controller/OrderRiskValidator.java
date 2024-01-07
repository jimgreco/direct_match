package com.core.match.ouch.controller;

import com.core.match.services.account.Account;
import com.core.match.services.risk.RiskService;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.MultiLegSecurity;
import com.core.match.services.trader.Trader;
import com.core.match.util.MatchBondMath;
import com.core.util.log.Log;

/**
 * Created by hli on 6/30/16.
 */
public class OrderRiskValidator implements RiskValidator {
    private final RiskService riskService;
    private final Log log;

    public OrderRiskValidator(Log log, RiskService riskService){
        this.log=log;
        this.riskService=riskService;
    }
    @Override
    public boolean validateFatFingerRisk(BaseSecurity baseSecurity, Trader storedTrader, int qty){
        if (baseSecurity.isBond() && RiskService.violatesFatFingerQuantityLimit(storedTrader, qty,(Bond) baseSecurity))
        {
            log.debug(log.log().add("Rejected: Violate Fat Finger Limit.Qty:").add(qty));

            return false;
        }

        if (baseSecurity.isSpread() && RiskService.violatesFatFingerQuantityLimit(storedTrader, qty,((MultiLegSecurity) baseSecurity).getLeg1()))
        {
            log.debug(log.log().add("Rejected: Violate Fat Finger Limit.Qty:").add(qty));
            return false;
        }

        if (baseSecurity.isButterfly() && RiskService.violatesFatFingerQuantityLimit(storedTrader, qty,((MultiLegSecurity) baseSecurity).getLeg2()))
        {
            log.debug(log.log().add("Rejected: Violate Fat Finger Limit.Qty:").add(qty));
            return false;
        }
        return true;
    }

    @Override
    public boolean validateDV01Risk(BaseSecurity baseSecurity, Account account, long limitPrice, int qty, boolean isBuy){
        double dv01=0;
        if(baseSecurity.isBond()){

            dv01 = MatchBondMath.getSignedDV01((Bond)baseSecurity, limitPrice, qty,isBuy);
        }else {
            dv01=MatchBondMath.getSignedDV01((MultiLegSecurity)baseSecurity,qty,isBuy);
        }

        if (riskService.violatesDV01Limit(account, dv01))
        {
            log.debug(log.log().add("Rejected: Violate Account Net DV01").add(Double.toString(dv01)).add(account.getName()).add(" DV01 Limit is ").add(Math.round(account.getNetDV01Limit())));
            return false;
        }
        return true;
    }
}
