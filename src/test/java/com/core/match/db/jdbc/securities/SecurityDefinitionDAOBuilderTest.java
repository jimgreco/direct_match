package com.core.match.db.jdbc.securities;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.core.match.GenericAppTest;
import com.core.match.msgs.MatchConstants;
import com.core.match.services.order.DisplayedOrder;
import com.core.match.services.quote.Quote;
import com.core.match.services.security.Bond;
import com.core.match.services.security.MultiLegSecurity;
import com.core.match.services.security.SecurityType;
import com.core.match.util.MatchBondMath;
import com.core.util.PriceUtils;
import com.gs.collections.impl.map.mutable.UnifiedMap;

public class SecurityDefinitionDAOBuilderTest extends GenericAppTest<DisplayedOrder> {

    private long time;
    private MultiLegSecurity spread;
    private Quote quote;
    private String session;
    private Bond twoYrSecurity;
    private Bond tenYrSecurity;
    private Bond thirtyYrSecurity;


    private MultiLegSecurity twoYearTenYear;
    private Map<Short, Double> cache;
    private MultiLegSecurity butterfly;

    public SecurityDefinitionDAOBuilderTest() {
        super(DisplayedOrder.class);
    }

    @Before
    public void setUp(){
        session="test1233";
        sendSecurity("2Y", 2.5, 20170515, 20150515);
        sendSecurity("10Y", 2.5, 20250515, 20150515);
        sendSecurity("30Y", 2.5, 20450515, 20150515);

        sendSpread("2Y10Y", "2Y", "10Y",4, 1);
        sendButterfly("2Y10Y30Y","2Y","10Y","30Y",8,4,1);

        twoYrSecurity = (Bond)securities.get("2Y");
        tenYrSecurity = (Bond)securities.get("10Y");
        thirtyYrSecurity = (Bond)securities.get("30Y");
        spread = (MultiLegSecurity)securities.get("2Y10Y");
        butterfly = (MultiLegSecurity)securities.get("2Y10Y30Y");

        quote=new Quote(twoYrSecurity);
        quote.setBidOffer(99,100);
        cache= new UnifiedMap<>();

    }
    private SecurityDefinitionDAOBuilder createTarget(){
        return new SecurityDefinitionDAOBuilder();
    }

    @Test
    public void buildBond_any_createsSecurityDaosWithRightField(){
        SecurityDefinitionDAO actual= new SecurityDefinitionDAO();
        //Arrange
        SecurityDefinitionDAOBuilder target=createTarget();

        //Act
        target.build(twoYrSecurity,quote,session,time,actual);
        //Assert
        assertEquals(100,actual.ask,0);
        assertEquals(99,actual.bid,0);
        assertEquals(session,actual.session);
        assertEquals(1,actual.id);
        assertEquals(SecurityType.NOTE.getValue(),actual.type);
        assertEquals(99.5,actual.mid,0);
        //Spread fields will be left 0 as default
        assertEquals(0,actual.ratio1,0);
        assertEquals(0,actual.ratio2,0);
        assertEquals(0,actual.ratio3,0);

    }

    @Test(expected = IllegalStateException.class)
    public void buildSpread_missingLegPriceInCache_createsSecurityDaosWithRightField(){
        //Arrange
        SecurityDefinitionDAOBuilder target=createTarget();
        SecurityDefinitionDAO actual= new SecurityDefinitionDAO();

        cache.put(spread.getLeg1().getID(),99.0);

        double dv01Leg1= MatchBondMath.getDV01(spread.getLeg1(), PriceUtils.toLong(99.0, MatchConstants.IMPLIED_DECIMALS),1);
        double dv01Leg2= MatchBondMath.getDV01(spread.getLeg2(), PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS),1);

        //Act
        target.buildDiscreteSpread(spread,session,time, cache,actual);
        //Assert
    }

    @Test
    public void buildSpread_any_createsSecurityDaosWithRightField(){
        //Arrange
        SecurityDefinitionDAOBuilder target=createTarget();
        SecurityDefinitionDAO actual= new SecurityDefinitionDAO();

        cache.put(spread.getLeg1().getID(),99.0);
        cache.put(spread.getLeg2().getID(),100.0);

        double dv01Leg1= MatchBondMath.getDV01(spread.getLeg1(), PriceUtils.toLong(99.0, MatchConstants.IMPLIED_DECIMALS),1);
        double dv01Leg2= MatchBondMath.getDV01(spread.getLeg2(), PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS),1);

        //Act
        target.buildDiscreteSpread(spread,session,time, cache,actual);
        //Assert
        assertEquals(0,actual.ask,0);
        assertEquals(0,actual.bid,0);
        assertEquals(session,actual.session);
        assertEquals(4,actual.id);
        assertEquals(SecurityType.DISCRETE_SPREAD.getValue(),actual.type);
        assertEquals(0,actual.mid,0);
        assertEquals(dv01Leg2/dv01Leg1,actual.ratio1,0);
        assertEquals(1.0,actual.ratio2,0);
        assertEquals(0,actual.ratio3,0);

    }

    @Test
    public void buildButterfly_any_createsSecurityDaosWithRightField(){
        //Arrange
        SecurityDefinitionDAOBuilder target=createTarget();
        cache.put(butterfly.getLeg1().getID(),99.0);
        cache.put(butterfly.getLeg2().getID(),100.0);
        cache.put(butterfly.getLeg3().getID(),101.0);
        SecurityDefinitionDAO actual= new SecurityDefinitionDAO();



        double dv01Leg1= MatchBondMath.getDV01(butterfly.getLeg1(), PriceUtils.toLong(99.0, MatchConstants.IMPLIED_DECIMALS),1);
        double dv01Leg2= MatchBondMath.getDV01(butterfly.getLeg2(), PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS),1);
        double dv01Leg3= MatchBondMath.getDV01(butterfly.getLeg3(), PriceUtils.toLong(101.0, MatchConstants.IMPLIED_DECIMALS),1);

        double expectedFirstLegRatio=dv01Leg2/dv01Leg1;
        double expectedThirdLegRatio=dv01Leg2/dv01Leg3;


        //Act
        target.buildDiscreteButterFly(butterfly,session,time, cache,actual);
        //Assert
        assertEquals(0,actual.ask,0);
        assertEquals(0,actual.bid,0);
        assertEquals(session,actual.session);
        assertEquals(5,actual.id);
        assertEquals(SecurityType.DISCRETE_BUTTERFLY.getValue(),actual.type);
        assertEquals(0,actual.mid,0);
        assertEquals(expectedFirstLegRatio,actual.ratio1,0);
        assertEquals(2.0,actual.ratio2,0);
        assertEquals(expectedThirdLegRatio,actual.ratio3,0);

    }

    @Test(expected = IllegalStateException.class)
    public void buildButterfly_missingLegPrice_throwsException(){
        //Arrange
        SecurityDefinitionDAOBuilder target=createTarget();
        cache.put(butterfly.getLeg1().getID(),99.0);
        cache.put(butterfly.getLeg2().getID(),100.0);

        SecurityDefinitionDAO actual= new SecurityDefinitionDAO();



        double dv01Leg1= MatchBondMath.getDV01(butterfly.getLeg1(), PriceUtils.toLong(99.0, MatchConstants.IMPLIED_DECIMALS),1);
        double dv01Leg2= MatchBondMath.getDV01(butterfly.getLeg2(), PriceUtils.toLong(100.0, MatchConstants.IMPLIED_DECIMALS),1);
        double dv01Leg3= MatchBondMath.getDV01(butterfly.getLeg3(), PriceUtils.toLong(101.0, MatchConstants.IMPLIED_DECIMALS),1);

        double expectedFirstLegRatio=dv01Leg2/dv01Leg1;
        double expectedThirdLegRatio=dv01Leg2/dv01Leg3;


        //Act
        target.buildDiscreteButterFly(butterfly,session,time, cache,actual);
        //Assert

    }
}