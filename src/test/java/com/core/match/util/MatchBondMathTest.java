package com.core.match.util;

import com.core.match.GenericAppTest;
import com.core.match.services.order.AbstractOrder;
import com.core.match.services.risk.RiskServiceTest;
import com.core.match.services.security.Bond;
import com.core.match.services.security.MultiLegSecurity;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class MatchBondMathTest extends GenericAppTest<MatchBondMathTest.StubOrderTest> {

    Bond twoYearBond;
    Bond tenYearBond;
    MultiLegSecurity spread;


    public MatchBondMathTest() {
        super(StubOrderTest.class);
    }

    @Before
    public void setup(){
        sendSecurity("2Y", 2.5, 20170515, 20150515);
        sendSecurity("10Y", 2.5, 20250515, 20150515);
        sendSpread("2Y10Y", "2Y", "10Y",4, 1);
        twoYearBond = (Bond)securities.get("2Y");
        tenYearBond = (Bond)securities.get("10Y");
        spread=(MultiLegSecurity) securities.get("2Y10Y");
    }


    @Test
    public void getSignedDV01_tenYearBuy_correctDV01Calculated()  {
        //Arrange

        //Act
        double dv01= MatchBondMath.getSignedDV01(tenYearBond,100,1,true);

        //Assert
        assertEquals(MatchBondMath.getDV01(tenYearBond,100,1),dv01,0);
    }

    @Test
    public void getSignedDV01_twoYearSell_correctDV01Calculated()  {
        //Arrange

        //Act
        double dv01= MatchBondMath.getSignedDV01(twoYearBond,100,1,false);

        //Assert
        assertEquals(MatchBondMath.getDV01(twoYearBond,100,1)*-1,dv01,0);
    }

    @Test
    public void getSignedDV01_spread_correctDV01Calculated() {
        //Arrange

        //Act
        double dv01= MatchBondMath.getSignedDV01(spread,(int)spread.getLeg1Size(),true);
        double expected=Math.abs(MatchBondMath.getDV01(twoYearBond,twoYearBond.getReferencePrice(),(int)spread.getLeg1Size())-
                MatchBondMath.getDV01(tenYearBond,tenYearBond.getReferencePrice(),(int)spread.getLeg2Size()));
        //Assert
        assertEquals(expected,dv01,0);
    }

    public static class StubOrderTest extends AbstractOrder<StubOrderTest>{

    }

}