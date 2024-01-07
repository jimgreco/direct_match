package com.core.match.ouch.controller;

import com.core.match.services.account.Account;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.trader.Trader;

/**
 * Created by hli on 6/30/16.
 */
public interface RiskValidator {
    boolean validateFatFingerRisk(BaseSecurity baseSecurity, Trader storedTrader, int qty);

    boolean validateDV01Risk(BaseSecurity baseSecurity, Account account, long limitPrice, int qty, boolean isBuy);
}
