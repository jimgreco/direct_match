package com.core.match.db.jdbc.securities;

import com.core.match.msgs.MatchConstants;
import com.core.match.services.quote.Quote;
import com.core.match.services.security.Bond;
import com.core.match.services.security.MultiLegSecurity;
import com.core.match.util.MatchBondMath;
import com.core.util.PriceUtils;

import java.util.Map;

public class SecurityDefinitionDAOBuilder {
    public static void build(Bond bond, Quote quote,String session,long now, SecurityDefinitionDAO secDefDao){
        secDefDao.clear();

        secDefDao.session = session;
        secDefDao.nano = now;
        secDefDao.type = bond.getType().getValue();
        secDefDao.security = bond.getName();
        secDefDao.id = bond.getID();
        secDefDao.ask = quote.getOfferPrice();
        secDefDao.bid = quote.getBidPrice();
        secDefDao.mid = 0.5 * (quote.getBidPrice() + quote.getOfferPrice());
        secDefDao.ratio1=0;
        secDefDao.ratio2=0;
        secDefDao.ratio3=0;
    }

    public static void buildDiscreteSpread(MultiLegSecurity multiLegSecurity, String session, long now, Map<Short,Double> securityPriceCache, SecurityDefinitionDAO secDefDao){
        secDefDao.clear();
        secDefDao.session = session;
        secDefDao.nano = now;
        secDefDao.type = multiLegSecurity.getType().getValue();
        secDefDao.security = multiLegSecurity.getName();
        secDefDao.id = multiLegSecurity.getID();
        secDefDao.ask = 0;
        secDefDao.bid = 0;
        secDefDao.mid = 0;
        Bond leg1 = multiLegSecurity.getLeg1();
        Bond leg2 = multiLegSecurity.getLeg2();
        if(securityPriceCache.get(leg1.getID())==null ){
            onNullLegPricing(leg1);
        }
        else if(securityPriceCache.get(leg2.getID())==null ){
            onNullLegPricing(leg2);
        }
        long price1 = PriceUtils.toLong(securityPriceCache.get(leg1.getID()), MatchConstants.IMPLIED_DECIMALS);
        long price2 = PriceUtils.toLong(securityPriceCache.get(leg2.getID()), MatchConstants.IMPLIED_DECIMALS);
        double dv01FirstLeg = MatchBondMath.getDV01(leg1, price1, 1);
        double dv01SecondLeg = MatchBondMath.getDV01(leg2, price2, 1);
        double ratio=dv01SecondLeg/dv01FirstLeg;
        secDefDao.ratio1=ratio;
        secDefDao.ratio2=1;
        secDefDao.ratio3=0;

    }

    public static void buildDiscreteButterFly(MultiLegSecurity multiLegSecurity,String session, long now,  Map<Short,Double> securityPriceCache, SecurityDefinitionDAO secDefDao){
        secDefDao.clear();
        secDefDao.session = session;
        secDefDao.nano = now;
        secDefDao.type = multiLegSecurity.getType().getValue();
        secDefDao.security = multiLegSecurity.getName();
        secDefDao.id = multiLegSecurity.getID();
        secDefDao.ask = 0;
        secDefDao.bid = 0;
        secDefDao.mid = 0;
        Bond leg1 = multiLegSecurity.getLeg1();
        Bond leg2 = multiLegSecurity.getLeg2();
        Bond leg3 = multiLegSecurity.getLeg3();
        if(securityPriceCache.get(leg1.getID())==null ){
            onNullLegPricing(leg1);
        }
        else if(securityPriceCache.get(leg2.getID())==null ){
            onNullLegPricing(leg2);
        }
        else if(securityPriceCache.get(leg3.getID()) ==null){
            onNullLegPricing(leg3);
        }

        long price1 = PriceUtils.toLong(securityPriceCache.get(leg1.getID()), MatchConstants.IMPLIED_DECIMALS);
        long price2 = PriceUtils.toLong(securityPriceCache.get(leg2.getID()), MatchConstants.IMPLIED_DECIMALS);
        long price3 = PriceUtils.toLong(securityPriceCache.get(leg3.getID()), MatchConstants.IMPLIED_DECIMALS);

        double dv01FirstLeg = MatchBondMath.getDV01(leg1, price1, 1);
        double dv01SecondLeg = MatchBondMath.getDV01(leg2, price2, 1);
        double dv01ThirdLeg = MatchBondMath.getDV01(leg3, price3, 1);
        double ratioFirstLeg=dv01SecondLeg/dv01FirstLeg;
        double ratioThirdLeg=dv01SecondLeg/dv01ThirdLeg;
        secDefDao.ratio1=ratioFirstLeg;
        secDefDao.ratio2=2;//2*1
        secDefDao.ratio3=ratioThirdLeg;
    }

    private static void onNullLegPricing(Bond leg){
        throw new IllegalStateException("Leg Securities Prices are not found in cache. LegIDs:"+leg.getID()+" sec: "+leg.getName());

    }
}
