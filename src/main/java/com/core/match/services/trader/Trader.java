package com.core.match.services.trader;

import com.core.match.util.MatchPriceUtils;
import com.core.services.StaticsList;

/**
 * Created by johnlevidy on 5/26/15.
 */
public class Trader implements StaticsList.StaticsObject {
    private final short traderID;
    private final String traderName;

    private short accountID;

    private int fatFinger2YQtyLimit;
    private int fatFinger3YQtyLimit;
    private int fatFinger5YQtyLimit;
    private int fatFinger7YQtyLimit;
    private int fatFinger10YQtyLimit;
    private int fatFinger30YQtyLimit;

    public int getFatFinger2YQtyLimit() {
        return fatFinger2YQtyLimit;
    }

    public void setFatFinger2YQtyLimit(int fatFinger2YQtyLimit) {
        this.fatFinger2YQtyLimit = fatFinger2YQtyLimit;
    }

    public int getFatFinger3YQtyLimit() {
        return fatFinger3YQtyLimit;
    }

    public void setFatFinger3YQtyLimit(int fatFinger3YQtyLimit) {
        this.fatFinger3YQtyLimit = fatFinger3YQtyLimit;
    }

    public int getFatFinger5YQtyLimit() {
        return fatFinger5YQtyLimit;
    }

    public void setFatFinger5YQtyLimit(int fatFinger5YQtyLimit) {
        this.fatFinger5YQtyLimit = fatFinger5YQtyLimit;
    }

    public int getFatFinger7YQtyLimit() {
        return fatFinger7YQtyLimit;
    }

    public void setFatFinger7YQtyLimit(int fatFinger7YQtyLimit) {
        this.fatFinger7YQtyLimit = fatFinger7YQtyLimit;
    }

    public int getFatFinger10YQtyLimit() {
        return fatFinger10YQtyLimit;
    }

    public void setFatFinger10YQtyLimit(int fatFinger10YQtyLimit) {
        this.fatFinger10YQtyLimit = fatFinger10YQtyLimit;
    }

    public int getFatFinger30YQtyLimit() {
        return fatFinger30YQtyLimit;
    }

    public void setFatFinger30YQtyLimit(int fatFinger30YQtyLimit) {
        this.fatFinger30YQtyLimit = fatFinger30YQtyLimit;
    }


    public Trader(short id, String name) {
        this.traderID = id;
        this.traderName = name;
    }

    @Override
    public short getID() {
        return this.traderID;
    }

    @Override
    public String getName() {
        return this.traderName;
    }

    public void setAccountID(short accountID) {
        this.accountID = accountID;
    }

    public short getAccountID() {
        return accountID;
    }

    public String getLimitsString() {
        return MatchPriceUtils.toQtyRoundLot(getFatFinger2YQtyLimit()) + "," + MatchPriceUtils.toQtyRoundLot(getFatFinger3YQtyLimit()) + "," + MatchPriceUtils.toQtyRoundLot(getFatFinger5YQtyLimit()) +
                "," + MatchPriceUtils.toQtyRoundLot(getFatFinger7YQtyLimit()) + "," + MatchPriceUtils.toQtyRoundLot(getFatFinger10YQtyLimit()) + "," + MatchPriceUtils.toQtyRoundLot(getFatFinger30YQtyLimit());
    }

}
