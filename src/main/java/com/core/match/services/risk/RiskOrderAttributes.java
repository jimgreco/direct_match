package com.core.match.services.risk;


public interface RiskOrderAttributes
{
    double getFilledNetDV01Contribution();
    void setFilledNetDV01Contribution(double filledDV01);
    double getOpenDV01Contribution();
    void setOpenDV01Contribution(double unfilledDV01);
}
