package com.core.match.ouch.controller;

import com.core.match.services.security.Bond;

/**
 * Created by hli on 6/28/16.
 */
public interface PriceProvider {
    long getPrice(Bond bond, boolean isBuy);
}
