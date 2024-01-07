package com.core.match.services.risk;

/**
 * Created by hli on 3/1/16.
 */
public class RiskAccount extends RiskHolder {

    public void setID(short accountId) {
        this.accountId = accountId;
    }

    private short accountId;

    @Override
    public short getID() {
        return accountId;
    }
}
