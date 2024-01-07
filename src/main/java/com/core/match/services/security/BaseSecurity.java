package com.core.match.services.security;

import com.core.services.StaticsList;

/**
 * Created by hli on 12/14/15.
 */
public abstract class BaseSecurity implements StaticsList.StaticsObject{
    private final short id;
    private final String name;
    private SecurityType type;


    public long getTickSize() {
        return tickSize;
    }

    public void setTickSize(long tickSize) {
        this.tickSize = tickSize;
    }

    public int getLotSize() {
        return lotSize;
    }

    public void setLotSize(int lotSize) {
        this.lotSize = lotSize;
    }

    public abstract boolean isMultiLegInstrument();
    public abstract boolean isBond();
    public abstract boolean isSpread();

    public abstract boolean isButterfly();




    private long tickSize;
    private int lotSize;

    public BaseSecurity(short id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public short getID() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }



    public void setType(SecurityType type) {
        this.type = type;
    }

    public SecurityType getType() {
        return type;
    }

}
