package com.core.match.ouch.controller;

import com.core.match.GenericAppTest;
import com.core.match.msgs.MatchConstants;
import com.core.match.services.book.MatchBBOBookService;
import com.core.match.services.order.DisplayedOrder;
import com.core.match.services.security.Bond;
import com.core.util.PriceUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SpreadPriceProviderTest extends GenericAppTest<DisplayedOrder> {
    private Bond twoYearBond;
    private Bond tenYearBond;
    private double twoYrOffer1=100.1;
    private double twoYrBid1=99.1;

    private double tenYrOffer1=100.1;
    private double tenYrBid1=99.1;
    private SpreadPriceProvider target;
    private Bond thirtyYearBond;

    public SpreadPriceProviderTest() {
        super(DisplayedOrder.class);
    }

    @Before
    public void setUp() {
        target= createTarget();

        sendSecurity("2Y", 2.5, 20170515, 20150515);
        sendSecurity("10Y", 2.5, 20250515, 20150515);
        sendSecurity("30Y", 2.5, 20450515, 20150515);

        sendSpread("2Y10Y", "2Y", "10Y",4, 1);
        twoYearBond = (Bond)securities.get("2Y");
        tenYearBond = (Bond)securities.get("10Y");
        thirtyYearBond = (Bond)securities.get("30Y");

        this.sendAccount("testAcct", 100000, "NULL", false, 3.5);
        this.sendTrader("Trader1", "testAcct", 1000, 1000, 1000, 1000, 1000, 1000);


    }

    private SpreadPriceProvider createTarget(){
        return new SpreadPriceProvider(securities,this.referenceBBOBookService,dispatcher,log);
    }

    @Test
    public void getPrice_securityHasBidQuote_returnQuotePricing() {
        //Arrange
        sendQuote(MatchConstants.Venue.Bloomberg,"2Y",twoYrBid1,twoYrOffer1);
        sendQuote(MatchConstants.Venue.Bloomberg,"10Y",tenYrBid1,tenYrOffer1);

        //act
        long actualPrice= target.getPrice(twoYearBond,true);

        //assert
        Assert.assertEquals(PriceUtils.toLong(twoYrBid1,MatchConstants.IMPLIED_DECIMALS),actualPrice,0);


    }

    @Test
    public void getPrice_securityHasBestBidONBook_returnBookPricing() {
        //Arrange
        double bestBidPrice=97.5;
        sendPassiveOrder("Trader1",true,1,"2Y",bestBidPrice);
        sendQuote(MatchConstants.Venue.Bloomberg,"2Y",twoYrBid1,twoYrOffer1);
        sendQuote(MatchConstants.Venue.Bloomberg,"10Y",tenYrBid1,tenYrOffer1);

        //act
        long actualPrice= target.getPrice(twoYearBond,true);

        //assert
        Assert.assertEquals(PriceUtils.toLong(bestBidPrice,MatchConstants.IMPLIED_DECIMALS),actualPrice,0);
    }

    @Test
    public void getPrice_securityHasNoQuotesOrBook_returnDefaultPrice() {
        //Arrange
        //act
        long actualPrice= target.getPrice(thirtyYearBond,true);

        //assert
        Assert.assertEquals(PriceUtils.toLong(100.0,MatchConstants.IMPLIED_DECIMALS),actualPrice,0);
    }

    @Test
    public void getPrice_securityHasOfferQuote_returnQuotePricing() {
        //Arrange
        sendQuote(MatchConstants.Venue.Bloomberg,"2Y",twoYrBid1,twoYrOffer1);
        sendQuote(MatchConstants.Venue.Bloomberg,"10Y",tenYrBid1,tenYrOffer1);

        //act
        long actualPrice= target.getPrice(tenYearBond,false);

        //assert
        Assert.assertEquals(PriceUtils.toLong(tenYrOffer1,MatchConstants.IMPLIED_DECIMALS),actualPrice,0);


    }

    @Test
    public void getPrice_securityHasBestOfferONBook_returnBookPricing() {
        //Arrange
        double bestOffer=97.5;
        sendPassiveOrder("Trader1",false,1,"10Y",bestOffer);
        sendQuote(MatchConstants.Venue.Bloomberg,"2Y",twoYrBid1,twoYrOffer1);
        sendQuote(MatchConstants.Venue.Bloomberg,"10Y",tenYrBid1,tenYrOffer1);

        //act
        long actualPrice= target.getPrice(tenYearBond,false);

        //assert
        Assert.assertEquals(PriceUtils.toLong(bestOffer,MatchConstants.IMPLIED_DECIMALS),actualPrice,0);


    }

    @Test
    public void getPrice_securityOfferHasNoQuotesOrBook_returnDefaultPrice() {
        //Arrange
        //act
        long actualPrice= target.getPrice(thirtyYearBond,false);

        //assert
        Assert.assertEquals(PriceUtils.toLong(100.0,MatchConstants.IMPLIED_DECIMALS),actualPrice,0);
    }

}