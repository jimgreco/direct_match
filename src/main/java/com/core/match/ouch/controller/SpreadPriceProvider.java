package com.core.match.ouch.controller;

import com.core.connector.Dispatcher;
import com.core.match.msgs.MatchConstants;
import com.core.match.services.book.MatchBBOBookService;
import com.core.match.services.book.MatchDisplayedPriceLevelBookService;
import com.core.match.services.book.MatchDisplayedPriceLevelBookServiceRoundingType;
import com.core.match.services.order.DisplayedOrder;
import com.core.match.services.order.DisplayedOrderService;
import com.core.match.services.order.OrderService;
import com.core.match.services.quote.VenueQuoteService;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.SecurityService;
import com.core.util.PriceUtils;
import com.core.util.log.Log;


public class SpreadPriceProvider implements PriceProvider {

    private final VenueQuoteService quotes;
    private final MatchBBOBookService bbos;
    private static long DEFAULT_PRICE=PriceUtils.toLong(100,MatchConstants.IMPLIED_DECIMALS);

    private final SecurityService<BaseSecurity> securities;

    public SpreadPriceProvider(SecurityService securityService, MatchBBOBookService bbos,Dispatcher dispatcher, Log log){
        securities = securityService;
        quotes = new VenueQuoteService(securityService);
        dispatcher.subscribe(quotes);
        dispatcher.subscribe(securities);
        this.bbos = bbos;
    }

    @Override
    public long getPrice(Bond bond, boolean isBuy){
        if(isBuy){
            if (bbos.get(bond).hasBid()){
                return bbos.get(bond).getBidPrice();
            }else if(quotes.getQuote(MatchConstants.Venue.Bloomberg,bond).isValid()){
                return quotes.getQuote(MatchConstants.Venue.Bloomberg,bond).getBidPrice();
            }else if(quotes.getQuote(MatchConstants.Venue.InteractiveData,bond).isValid()){
                return quotes.getQuote(MatchConstants.Venue.InteractiveData,bond).getBidPrice();
            }else{
                return DEFAULT_PRICE;
            }
        }else{
            if (bbos.get(bond).hasOffer()){
                return bbos.get(bond).getOfferPrice();
            }else if(quotes.getQuote(MatchConstants.Venue.Bloomberg,bond).isValid()){
                return quotes.getQuote(MatchConstants.Venue.Bloomberg,bond).getOfferPrice();
            }else if(quotes.getQuote(MatchConstants.Venue.InteractiveData,bond).isValid()){
                return quotes.getQuote(MatchConstants.Venue.InteractiveData,bond).getOfferPrice();
            }else{
                return DEFAULT_PRICE;
            }
        }
    }


}
