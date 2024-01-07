package com.core.match.ouch.controller;

import com.core.match.services.security.BaseSecurity;

/**
 * Created by hli on 6/30/16.
 */
public interface QuantityValidator {
    boolean validateQuantity(BaseSecurity baseSecurity, int qty);
}
