package com.core.match.services.security;

/**
 * Created by hli on 12/14/15.
 */
public class MultiLegSecurity extends BaseSecurity {
    private Bond leg1;
    private Bond leg2;
    private Bond leg3;
    private int numLegs;


    public void setNumLegs(int numLegs) {
        this.numLegs = numLegs;
    }

    public double getLeg1Size() {
        return leg1Size;
    }

    public void setLeg1Size(double leg1Size) {
        this.leg1Size = leg1Size;
    }

    public double getLeg2Size() {
        return leg2Size;
    }

    public void setLeg2Size(double leg2Size) {
        this.leg2Size = leg2Size;
    }

    public double getLeg3Size() {
        return leg3Size;
    }

    public void setLeg3Size(double leg3Size) {
        this.leg3Size = leg3Size;
    }

    private double leg1Size;
    private double leg2Size;
    private double leg3Size;
    @Override
    public boolean isMultiLegInstrument() {
        return true;
    }

    @Override
    public boolean isBond() {
        return false;
    }

    @Override
    public boolean isSpread() {
        return numLegs==2;
    }

    @Override
    public boolean isButterfly() {
        return numLegs==3;
    }

    public MultiLegSecurity(short id, String name) {
        super(id, name);
    }

    public void setNumberOfLegs(int numLegs) {
        this.numLegs = numLegs;
    }

    public void setLeg1(Bond leg1) {
        this.leg1 = leg1;
    }

    public void setLeg2(Bond leg2ID) {
        this.leg2 = leg2ID;
    }

    public void setLeg3(Bond leg3ID) {
        this.leg3 = leg3ID;
    }

    public Bond getLeg1() {
        return leg1;
    }

    public Bond getLeg2() {
        return leg2;
    }

    public Bond getLeg3() {
        return leg3;
    }

    public int getNumLegs() {
        return numLegs;
    }


}
