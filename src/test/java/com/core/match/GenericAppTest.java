package com.core.match;

import static com.core.match.msgs.MatchConstants.IMPLIED_DECIMALS;

import java.io.IOException;
import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;

import com.core.GenericTest;
import com.core.connector.Connector;
import com.core.match.msgs.MatchAccountCommand;
import com.core.match.msgs.MatchCancelCommand;
import com.core.match.msgs.MatchCommonCommand;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchContributorCommand;
import com.core.match.msgs.MatchFillCommand;
import com.core.match.msgs.MatchOrderCommand;
import com.core.match.msgs.MatchQuoteCommand;
import com.core.match.msgs.MatchReplaceCommand;
import com.core.match.msgs.MatchSecurityCommand;
import com.core.match.msgs.MatchSystemEventCommand;
import com.core.match.msgs.MatchTestDispatcher;
import com.core.match.msgs.MatchTestMessages;
import com.core.match.msgs.MatchTraderCommand;
import com.core.match.ouch2.controller.OUCHOrdersRepository;
import com.core.match.ouch2.factories.OUCHFactory;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.book.MatchBBOBookService;
import com.core.match.services.book.MatchDisplayedPriceLevelBookService;
import com.core.match.services.book.MatchDisplayedPriceLevelBookServiceRoundingType;
import com.core.match.services.contributor.Contributor;
import com.core.match.services.contributor.ContributorService;
import com.core.match.services.events.SystemEventService;
import com.core.match.services.order.DisplayedOrder;
import com.core.match.services.order.DisplayedOrderService;
import com.core.match.services.order.Order;
import com.core.match.services.order.OrderService;
import com.core.match.services.quote.VenueQuoteService;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.match.services.trades.TradeService;
import com.core.util.PriceUtils;
import com.core.util.TimeUtils;
import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;
import com.gs.collections.impl.map.mutable.primitive.ObjectShortHashMap;

/**
 * Created by johnlevidy on 6/3/15.
 */
public abstract class GenericAppTest<T extends Order<T>> extends GenericTest {
    protected final MatchTestDispatcher dispatcher;
    protected final StubMatchCommandSender sender;
    protected final MatchTestMessages msgs;
    protected final Connector connector;
    
    protected final SecurityService<BaseSecurity> securities;
    protected final AccountService<Account> accounts;
    protected final ContributorService<Contributor> contributors;
    protected final TraderService<Trader> traders;
    protected final SystemEventService systemEventService;
    protected final OrderService<T> orders;
    protected final TradeService<T> trades;
    protected final VenueQuoteService quotes;
    protected final OUCHFactory ouchFactory;
    protected final MatchBBOBookService referenceBBOBookService;


    protected short contributorID;

    protected int nextMatchID = 1;
    protected int nextReplaceID = 1;
    protected int nextOrderID = 1;
    protected int nextContribSeq = 1;
    protected int nextExternalOrderID = 1;

    private final ObjectShortHashMap<String> securityNameIDMap = new ObjectShortHashMap<>();
    private final ObjectShortHashMap<String> contributorNameIDMap = new ObjectShortHashMap<>();
    private final ObjectShortHashMap<String> traderNameIDMap = new ObjectShortHashMap<>();
    private final ObjectShortHashMap<String> accountNameIDMap = new ObjectShortHashMap<>();
    private final IntObjectHashMap<SimpleOrder> idOrderMap = new IntObjectHashMap<>();

    public GenericAppTest(Class<T> clsReal)
    {
        this.msgs = new MatchTestMessages();
        this.dispatcher = new MatchTestDispatcher();
        this.sender = new StubMatchCommandSender("TEST", contributorID, this.dispatcher);
        this.connector = Mockito.mock(Connector.class);
        ouchFactory = Mockito.mock(OUCHFactory.class);
        Mockito.when(ouchFactory.getOUCHRepository()).thenReturn(new OUCHOrdersRepository());



        securities = SecurityService.create(log, timeSource);
        accounts = AccountService.create();
        contributors = ContributorService.create();
        traders = TraderService.create();
        systemEventService = SystemEventService.create();
        orders = OrderService.create(clsReal, log, dispatcher);
        quotes = new VenueQuoteService(securities);
        trades = new TradeService<>(orders);


        OrderService<DisplayedOrder> orders = OrderService.create(DisplayedOrder.class,log , dispatcher);
        dispatcher.subscribe(orders);

        DisplayedOrderService<DisplayedOrder> displayedOrderService = new DisplayedOrderService<>(orders, log);
        MatchDisplayedPriceLevelBookService priceLevelBookService = new MatchDisplayedPriceLevelBookService(displayedOrderService, securities, log, MatchDisplayedPriceLevelBookServiceRoundingType.NONE);
        displayedOrderService.addListener(priceLevelBookService);
        referenceBBOBookService = new MatchBBOBookService(priceLevelBookService, securities);
        dispatcher.subscribe(securities);
        dispatcher.subscribe(accounts);
        dispatcher.subscribe(contributors);
        dispatcher.subscribe(traders);
        dispatcher.subscribe(systemEventService);
        dispatcher.subscribe(orders);
        dispatcher.subscribe(quotes);
    }

    @SuppressWarnings("unused")
	@Before
    public void before() throws IOException {
        sendContributor("TESTER01");
    }

    protected void setContributor(String name) {
        this.contributorID = contributorNameIDMap.get(name);
        Assert.assertNotEquals(0, contributorID);
        sender.setContribID(this.contributorID);
    }

    protected void openMarket() {
        MatchSystemEventCommand cmd = msgs.getMatchSystemEventCommand();
        cmd.setEventType(MatchConstants.SystemEvent.Open);
        dispatch(cmd);
    }

    protected void closeMarket() {
        MatchSystemEventCommand cmd = msgs.getMatchSystemEventCommand();
        cmd.setEventType(MatchConstants.SystemEvent.Close);
        dispatch(cmd);
    }

    protected void sendContributor(String name) {
        short contributorID = contributorNameIDMap.get(name);
        if (contributorID == 0) {
            contributorID = (short) (contributorNameIDMap.size() + 1);
            contributorNameIDMap.put(name, contributorID);
        }

        MatchContributorCommand cmd = msgs.getMatchContributorCommand();
        cmd.setSourceContributorID(contributorID);
        cmd.setName(name);

        dispatch(cmd);
    }

    protected void sendTrader(String name, String account) {
        sendTrader(name, account, 2000, 3000, 5000, 7000, 10000, 10000);
    }

    protected void sendTrader(String name, String account, int fatFinger2Y, int fatFinger3Y, int fatFinger5Y, int fatFinger7Y, int fatFinger10Y, int fatFinger30Y) {
    	short traderID = traderNameIDMap.get(name);
        if (traderID == 0) {
            traderID = (short) (traderNameIDMap.size() + 1);
            traderNameIDMap.put(name, traderID);
        }

        short accountID = accountNameIDMap.get(account);
        Assert.assertNotEquals(0, accountID);

        MatchTraderCommand cmd = msgs.getMatchTraderCommand();
        cmd.setAccountID(accountID);
        cmd.setTraderID(traderID);
        cmd.setName(name);
        cmd.setFatFinger2YLimit(fatFinger2Y);
        cmd.setFatFinger3YLimit(fatFinger3Y);
        cmd.setFatFinger5YLimit(fatFinger5Y);
        cmd.setFatFinger7YLimit(fatFinger7Y);
        cmd.setFatFinger10YLimit(fatFinger10Y);
        cmd.setFatFinger30YLimit(fatFinger30Y);

        dispatch(cmd);
	}

    protected void sendAccount(String name) {
    	sendAccount(name, 1000, "", false, 2.5);
	}

    protected void sendAccount(String name, int netDV01, String stateStreetInternalID, boolean isNetting, double commission) {
        short accountID = accountNameIDMap.get(name);
        if (accountID == 0) {
            accountID = (short) (accountNameIDMap.size() + 1);
            accountNameIDMap.put(name, accountID);
        }

        MatchAccountCommand cmd = msgs.getMatchAccountCommand();
        cmd.setSSGMID(stateStreetInternalID);

        cmd.setAccountID(accountID);
        cmd.setName(name);
        cmd.setNetDV01Limit(netDV01);
        cmd.setNettingClearing(isNetting);
        cmd.setCommission(commission);

        dispatch(cmd);
    }

    protected void sendBond(String name) {
        short securityID = securityNameIDMap.get(name);
        if (securityID == 0) {
            securityID = (short) (securityNameIDMap.size() + 1);
            securityNameIDMap.put(name, securityID);
        }

        sendSecurity(name, 2.5, 10,99.16);
    }

    protected void sendSpread(String name,String leg1,String leg2, int leg1Size,int leg2Size) {
        short securityID = securityNameIDMap.get(name);
        short leg1ID = securityNameIDMap.get(leg1);
        short leg2ID = securityNameIDMap.get(leg2);

        if (securityID == 0) {
            securityID = (short) (securityNameIDMap.size() + 1);
            securityNameIDMap.put(name, securityID);
        }

        MatchSecurityCommand cmd = msgs.getMatchSecurityCommand();
        cmd.setSecurityID(securityID);
        cmd.setName(name);
        cmd.setLotSize(MatchConstants.QTY_MULTIPLIER);

        cmd.setNumLegs((byte)2);
        cmd.setLeg1ID(leg1ID);
        cmd.setLeg1Size(leg1Size);
        cmd.setLeg2Size(leg2Size);
        cmd.setLeg2ID(leg2ID);
        cmd.setTickSize(0.0001);
        cmd.setType(MatchConstants.SecurityType.DiscreteSpread);

        dispatch(cmd);    }

    protected void sendButterfly(String name,String leg1,String leg2, String leg3, int leg1Size,int leg2Size, int leg3Size) {
        short securityID = securityNameIDMap.get(name);
        short leg1ID = securityNameIDMap.get(leg1);
        short leg2ID = securityNameIDMap.get(leg2);
        short leg3ID = securityNameIDMap.get(leg3);

        if (securityID == 0) {
            securityID = (short) (securityNameIDMap.size() + 1);
            securityNameIDMap.put(name, securityID);
        }

        MatchSecurityCommand cmd = msgs.getMatchSecurityCommand();
        cmd.setSecurityID(securityID);
        cmd.setName(name);
        cmd.setLotSize(MatchConstants.QTY_MULTIPLIER);

        cmd.setNumLegs((byte)3);
        cmd.setLeg1ID(leg1ID);
        cmd.setLeg1Size(leg1Size);
        cmd.setLeg2ID(leg2ID);
        cmd.setLeg2Size(leg2Size);
        cmd.setLeg3ID(leg3ID);
        cmd.setLeg3Size(leg3Size);
        cmd.setTickSize(0.0001);
        cmd.setType(MatchConstants.SecurityType.DiscreteButterfly);

        dispatch(cmd);
    }

    protected void sendSecurity(String name, double coupon, int term,double previousClosePrice) {
        short securityID = securityNameIDMap.get(name);
        if (securityID == 0) {
            securityID = (short) (securityNameIDMap.size() + 1);
            securityNameIDMap.put(name, securityID);
        }

        LocalDate date=LocalDate.of(2016,3,14);

        int issueDate = TimeUtils.toDateInt(date);
        int maturityDate = issueDate + term * 10000;

        MatchSecurityCommand cmd = msgs.getMatchSecurityCommand();
        cmd.setSecurityID(securityID);
        cmd.setName(name);
        cmd.setLotSize(MatchConstants.QTY_MULTIPLIER);
        cmd.setCUSIP(name + "_CUSIP");
        cmd.setMaturityDate(maturityDate);
        cmd.setCoupon(PriceUtils.toLong(coupon, IMPLIED_DECIMALS));
        cmd.setCouponFrequency((byte) 2);
        cmd.setIssueDate(issueDate);
        cmd.setTickSize(PriceUtils.getEighth(IMPLIED_DECIMALS));
        cmd.setType(MatchConstants.SecurityType.TreasuryNote);
        cmd.setReferencePrice(previousClosePrice);

        dispatch(cmd);
    }

    protected void sendSecurity(String name, double coupon, int maturityDate, int issueDate) {
        short securityID = securityNameIDMap.get(name);
        if (securityID == 0) {
            securityID = (short) (securityNameIDMap.size() + 1);
            securityNameIDMap.put(name, securityID);
        }

        MatchSecurityCommand cmd = msgs.getMatchSecurityCommand();
        cmd.setSecurityID(securityID);
        cmd.setName(name);
        cmd.setCUSIP(name + "_CUSIP");
        cmd.setMaturityDate(maturityDate);
        cmd.setCoupon(PriceUtils.toLong(coupon, IMPLIED_DECIMALS));
        cmd.setCouponFrequency((byte) 2);
        cmd.setIssueDate(issueDate);
        cmd.setTickSize(PriceUtils.getEighth(IMPLIED_DECIMALS));
        cmd.setType(MatchConstants.SecurityType.TreasuryNote);

        dispatch(cmd);
    }

    protected int sendAggressiveOrder(String trader, boolean buy, int qty, String security, double price, boolean ioc) {
        return sendOrder(trader, buy, qty, security, price, ioc, false);
    }

    protected int sendPassiveOrder(String trader, boolean buy, int qty, String security, double price) {
        return sendOrder(trader, buy, qty, security, price, false, true);
    }

    private int sendOrder(String trader, boolean buy, int qty, String security, double price, boolean ioc, boolean inBook) {
        int id = nextOrderID++;
        int externalOrderID = nextExternalOrderID++;
        long longPrice = PriceUtils.toLong(price, IMPLIED_DECIMALS);

        SimpleOrder simpleOrder = new SimpleOrder();
        simpleOrder.qty = qty;
        simpleOrder.price = longPrice;
        simpleOrder.externalID = externalOrderID;
        simpleOrder.ioc = ioc;
        simpleOrder.inBook = inBook;
        idOrderMap.put(id, simpleOrder);

        MatchOrderCommand cmd = msgs.getMatchOrderCommand();
        cmd.setOrderID(id);
        cmd.setSecurityID(getSecurityID(security));
        cmd.setQty(simpleOrder.qty);
        cmd.setBuy(buy);
        cmd.setPrice(simpleOrder.price);
        cmd.setTraderID(getTraderID(trader));
        cmd.setClOrdID(id + "_CLORDID");
        cmd.setExternalOrderID(simpleOrder.externalID);
        cmd.setIOC(ioc);
        cmd.setInBook(inBook);

        dispatch(cmd);
        return id;
    }

    protected void sendCancel(int id) {
        MatchCancelCommand cmd = msgs.getMatchCancelCommand();
        cmd.setOrderID(id);

        dispatch(cmd);
    }

    protected void sendAggressiveReplace(int id, int newQty, double newPrice) {
        sendReplace(id, newQty, newPrice, false);
    }

    protected void sendPassiveReplace(int id, int newQty, double newPrice) {
        sendReplace(id, newQty, newPrice, true);
    }

    private void sendReplace(int id, int newQty, double newPrice, boolean inBook) {
        long longPrice = PriceUtils.toLong(newPrice, IMPLIED_DECIMALS);

        SimpleOrder simpleOrder = idOrderMap.get(id);
        if (simpleOrder.price != longPrice || newQty > simpleOrder.qty) {
            simpleOrder.externalID = nextExternalOrderID++;
        }
        simpleOrder.qty = newQty;
        simpleOrder.price = longPrice;
        simpleOrder.inBook = inBook;

        MatchReplaceCommand cmd = msgs.getMatchReplaceCommand();
        cmd.setOrderID(id);
        cmd.setQty(simpleOrder.qty);
        cmd.setPrice(simpleOrder.price);
        cmd.setOrigClOrdID(id + "_CLORDID");
        cmd.setClOrdID(id + "_CLORDID_" + nextReplaceID++);
        cmd.setExternalOrderID(simpleOrder.externalID);
        cmd.setInBook(inBook);

        dispatch(cmd);
    }

    protected void sendMatch(boolean lastFill, int id1, int id2, int qty, double price) {
        int matchID = nextMatchID++;
        sendFill(false, id1, qty, price, matchID);
        sendFill(lastFill, id2, qty, price, matchID);
    }

    protected void sendFill(boolean lastFill, int id, int qty, double price) {
        sendFill(lastFill, id, qty, price, nextMatchID++);
    }

    private void sendFill(boolean lastFill, int id, int qty, double price, int matchID) {
        SimpleOrder simpleOrder = idOrderMap.get(id);
        boolean prevPassive = simpleOrder.inBook;
        simpleOrder.cumQty += qty;
        simpleOrder.inBook = simpleOrder.inBook || (lastFill && !simpleOrder.ioc && simpleOrder.hasRemainingQty());

        long longPrice = PriceUtils.toLong(price, IMPLIED_DECIMALS);

        MatchFillCommand cmd = msgs.getMatchFillCommand();
        cmd.setOrderID(id);
        cmd.setQty(qty);
        cmd.setPrice(longPrice);
        cmd.setLastFill(lastFill);
        cmd.setPassive(prevPassive);
        cmd.setMatchID(matchID);
        cmd.setInBook(simpleOrder.inBook);

        dispatch(cmd);
    }

    protected void sendQuote(char venueCode, String security, double bid, double offer) {
        MatchQuoteCommand cmd = msgs.getMatchQuoteCommand();
        cmd.setSecurityID(getSecurityID(security));
        cmd.setVenueCode(venueCode);
        cmd.setBidPrice(bid);
        cmd.setOfferPrice(offer);
        dispatch(cmd);
    }

    protected short getSecurityID(String name) {
        return securityNameIDMap.get(name);
    }

    protected short getTraderID(String name) {
        return traderNameIDMap.get(name);
    }

    protected short getContributorID(String name) {
        return contributorNameIDMap.get(name);
    }

    protected short getAccountID(String name) {
        return accountNameIDMap.get(name);
    }

    protected void dispatch(MatchCommonCommand cmd) {
        cmd.setContributorID(contributorID);
        cmd.setContributorSeq(nextContribSeq++);
        cmd.setTimestamp(timeSource.getTimestamp());

        log.info(log.log().add(cmd.toString()));
        dispatcher.dispatch(cmd);
    }

    private class SimpleOrder {
        int externalID;
        int qty;
        int cumQty;
        long price;
        boolean ioc;
        boolean inBook;

        public boolean hasRemainingQty() {
            return (qty - cumQty) > 0;
        }
    }
}
