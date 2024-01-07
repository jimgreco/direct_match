package com.core.match.services.risk;

/**
 * Created by jgreco on 11/13/15.
 */
public abstract class RiskHolder {
    private double netDV01;
    private double openBuyDV01;
    private double openSellDV01;

    public double getMaxExposedDV01() {
        double maxExposedLongSigned= openBuyDV01 + netDV01;
        double maxExposedShortSigned= -openSellDV01 + netDV01;

        if(Math.abs(maxExposedLongSigned) > Math.abs(maxExposedShortSigned)){
            return maxExposedLongSigned;
        }
        return maxExposedShortSigned;
    }

    public double getNetDV01()
    {
        return netDV01;
    }

    public abstract short getID();

    public double getOpenSellDV01() {
        return openSellDV01;
    }

    public double getOpenBuyDV01() {
        return openBuyDV01;
    }

    public void addOpenBuyDV01(double dv01) {
        openBuyDV01 += Math.abs(dv01);
    }

    public void addOpenSellDV01(double dv01) {
        openSellDV01 += Math.abs(dv01);
    }

    public void removeOpenBuyDV01(double dv01) {
        openBuyDV01 -= Math.abs(dv01);
    }

    public void removeOpenSellDV01(double dv01) {
        openSellDV01 -= Math.abs(dv01);
    }

    public void addNetDV01(double netDV01) {
        this.netDV01 += netDV01;
    }
}
