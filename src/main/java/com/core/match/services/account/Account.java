package com.core.match.services.account;

import com.core.match.services.risk.RiskHolder;
import com.core.services.StaticsList;

/**
 * User: jgreco
 */
public class Account implements StaticsList.StaticsObject {
    private short id;
    private final String name;

    private String stateStreetInternalID;
    private double dv01Limit;

    private boolean netting;
    private long commission;

    public Account(short id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
	public short getID() {
        return id;
    }

    public void setId( short id ){
        this.id = id;
    }

    @Override
	public String getName() {
        return name;
    }

    public double getNetDV01Limit() {
        return dv01Limit;
    }

    public void setNetDV01Limit(double dv01Limit) {
        this.dv01Limit = dv01Limit;
    }


    public String getStateStreetInternalID() {
        return stateStreetInternalID;
    }

    public void setStateStreetInternalID(String stateStreetInternalID) {
        this.stateStreetInternalID = stateStreetInternalID;
    }

    public boolean isNetting() {
        return netting;
    }

    public boolean isFICC() {
        return !netting;
    }

    public void setNetting(boolean netting) {
        this.netting = netting;
    }

    public void setCommission(long commission) {
        this.commission = commission;
    }

    public long getCommission() {
        return commission;
    }
}
