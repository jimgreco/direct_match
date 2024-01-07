package com.core.match.sequencer;

import java.nio.ByteBuffer;

import com.core.app.CommandException;
import com.core.connector.mold.Mold64UDPEventSender;
import com.core.match.msgs.MatchAccountCommand;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchContributorCommand;
import com.core.match.msgs.MatchMessages;
import com.core.match.msgs.MatchSecurityCommand;
import com.core.match.msgs.MatchSystemEventCommand;
import com.core.match.msgs.MatchTraderCommand;
import com.core.match.services.security.SecurityType;
import com.core.match.util.MatchPriceUtils;
import com.core.sequencer.BaseCommandHandler;
import com.core.util.BinaryUtils;
import com.core.util.PriceUtils;
import com.core.util.TimeUtils;
import com.core.util.log.Log;
import com.core.util.time.TimeSource;

/**
 * Created by jgreco on 7/26/15.
 */
class StaticsCommandHandler extends BaseCommandHandler {
    private static final int SECURITY_NAME_LENGTH = 16;
    private static final int ACCOUNT_NAME_LENGTH = 12;
    private static final int TRADER_NAME_LENGTH = 12;
    private static final int CUSIP_LENGTH = 16;

    private final ByteBuffer temp = ByteBuffer.allocate(256);
    private final MatchMessages messages;
    private final SequencerSecurityService securities;
    private final SequencerAccountService accounts;
    private final SequencerTraderService traders;

    public StaticsCommandHandler(Log log,
                                 TimeSource timeSource,
                                 MatchMessages messages,
                                 Mold64UDPEventSender listener,
                                 SequencerContributorService contributors,
                                 SequencerSecurityService securities,
                                 SequencerAccountService accounts,
                                 SequencerTraderService traders) {
        super(log, timeSource, listener, contributors);

        this.messages = messages;
        this.securities = securities;
        this.accounts = accounts;
        this.traders = traders;
    }


    public void systemEvent(char eventCode) {

        MatchSystemEventCommand coreSystemEventCommand = messages.getMatchSystemEventCommand(sender.startMessage());
        coreSystemEventCommand.setEventType(eventCode);
        sendMsgFromSeq(coreSystemEventCommand, true);

    }

    //2Y10Y 2Y 10Y 4 1 0.001 1000 DiscreteSpread
    public void addSpread(String securityName, String securityFirstLeg, String securitySecondLeg,int firstLegRatio,int secondLegRatio,double tickSize, int lotsize,String type){
        SecurityType securityType = SecurityType.lookup(type);
        long tickSizeLong = MatchPriceUtils.toLong(tickSize);

        if (securityType == null)
        {
            throw new CommandException("Invalid security type: " + type);
        }

        short id = securities.getID(securityName);
        if (id == 0)
        {
            id = securities.addSecurity(securityName, tickSizeLong, lotsize,securityType.getValue(),firstLegRatio);
            log.info(log.log().add(securityName).add(" : ").add(id));

        }


        short firstLegID = securities.getID(securityFirstLeg);
        short secondLegID = securities.getID(securitySecondLeg);

        if(firstLegID==0 || secondLegID==0){
            throw new CommandException("Unable to locate leg of spread ");
        }



        MatchSecurityCommand security = messages.getMatchSecurityCommand(sender.startMessage());
        security.setNumLegs(((byte)2));
        security.setSecurityID(id);

        security.setName(securityName);
        security.setLeg1ID(firstLegID);
        security.setLeg2ID(secondLegID);
        security.setLeg1Size(firstLegRatio);
        security.setLeg2Size(secondLegRatio);
        security.setTickSize(tickSize);
        security.setLotSize(lotsize);
        security.setType(securityType.getValue());
        sendMsgFromSeq(security, true);


    }


    public void addSecurity(String securityName,
                            String cusip,
                            double coupon,
                            int maturityDate,
                            int issueDate,
                            int tickValue,
                            int lotSize,
                            int couponFrequency,
                            String type, double referencePrice) {
        checkString(securityName, SECURITY_NAME_LENGTH);
        checkString(cusip, CUSIP_LENGTH);


        SecurityType securityType = SecurityType.lookup(type);
        if (securityType == null)
        {
            throw new CommandException("Invalid security type: " + type);
        }

        long tickSize = 0;

        switch (tickValue)
        {
            case 2:
                tickSize = PriceUtils.getPlus(MatchConstants.IMPLIED_DECIMALS);
                break;
            case 4:
                tickSize = PriceUtils.getQuarter(MatchConstants.IMPLIED_DECIMALS);
                break;
            case 8:
                tickSize = PriceUtils.getEighth(MatchConstants.IMPLIED_DECIMALS);
                break;
            case 16:
                tickSize = PriceUtils.getSixteenth(MatchConstants.IMPLIED_DECIMALS);
                break;
            default:
                throw new CommandException("Invalid tick size: " + tickSize + ", expected: [2,4,8,16]");
        }

        if (!TimeUtils.isValidDate(issueDate))
        {
            throw new CommandException("Invalid Issue Date: " + issueDate);
        }

        if (!TimeUtils.isValidDate(maturityDate))
        {
            throw new CommandException("Invalid Maturity Date: " + maturityDate);
        }

        short id = securities.getID(securityName);
        if (id == 0)
        {
            id = securities.addSecurity(securityName, tickSize, lotSize,securityType.getValue(),0);
            log.info(log.log().add(securityName).add(" : ").add(id));
        }

        MatchSecurityCommand security = messages.getMatchSecurityCommand(sender.startMessage());
        security.setSecurityID(id);
        security.setCouponFrequency((byte) couponFrequency);
        security.setMaturityDate(maturityDate);
        security.setIssueDate(issueDate);
        security.setType(securityType.getValue());
        security.setTickSize(tickSize);
        security.setLotSize(lotSize);

        temp.clear();
        BinaryUtils.copy(temp, securityName).flip();
        security.setName(temp);

        temp.clear();
        BinaryUtils.copy(temp, cusip).flip();
        security.setCUSIP(temp);

        long couponRounded = Math.round(coupon * 1000);
        long couponPrice = couponRounded * (MatchPriceUtils.getPriceMultiplier() / 1000);

        if(referencePrice==0){
            referencePrice=100;
        }
        long referencePriceRounded = Math.round(referencePrice * 1000);
        long refPrice = referencePriceRounded * (MatchPriceUtils.getPriceMultiplier() / 1000);
        security.setCoupon(couponPrice);
        security.setReferencePrice(refPrice);
        sendMsgFromSeq(security, true);
    }

    public void addContributor(String contributorName) {

        checkString(contributorName, MatchConstants.CONTRIBUTOR_NAME_LENGTH);

        short id = contributors.getID(contributorName);
        if (id == 0)
        {
            id = contributors.addContributor(contributorName);
        }

        MatchContributorCommand contributor = messages.getMatchContributorCommand(sender.startMessage());
        contributor.setSourceContributorID(id);
        contributor.setName(contributorName);
        contributor.setCancelOnDisconnect(true);

        sendMsgFromSeq(contributor, true);
    }

    public void addTrader(String traderName, String accountName, int ff2yLimit, int ff3yLimit, int ff5yLimit, int ff7yLimit, int ff10yLimit, int ff30yLimit)
    {

        checkString(traderName, TRADER_NAME_LENGTH);

        short accountID = accounts.getID(accountName);
        if (accountID == 0)
        {
            throw new CommandException("Account does not exist: " + accountName);
        }

        short traderID = traders.getID(traderName);
        if (traderID == 0)
        {
            traderID = traders.addTrader(traderName, accountID);
        }

        MatchTraderCommand trader = messages.getMatchTraderCommand(sender.startMessage());
        trader.setName(traderName);
        trader.setAccountID(accountID);
        trader.setTraderID(traderID);
        trader.setFatFinger2YLimit(MatchPriceUtils.roundLotToCoreQty(ff2yLimit));
        trader.setFatFinger3YLimit(MatchPriceUtils.roundLotToCoreQty(ff3yLimit));
        trader.setFatFinger5YLimit(MatchPriceUtils.roundLotToCoreQty(ff5yLimit));
        trader.setFatFinger7YLimit(MatchPriceUtils.roundLotToCoreQty(ff7yLimit));
        trader.setFatFinger10YLimit(MatchPriceUtils.roundLotToCoreQty(ff10yLimit));
        trader.setFatFinger30YLimit(MatchPriceUtils.roundLotToCoreQty(ff30yLimit));
        sendMsgFromSeq(trader, true);
    }

    public void addAccount(String accountName, int netDV01Limit, String stateStreetInternalID, boolean netting, double commission) {

        checkString(accountName, ACCOUNT_NAME_LENGTH);

        //TODO: find out the requirements for this SSinternalID etc length, and do a check here
        short id = accounts.getID(accountName);
        if (id == 0)
        {
            id = accounts.addAccount(accountName);
        }

        MatchAccountCommand account = messages.getMatchAccountCommand(sender.startMessage());
        account.setAccountID(id);
        account.setNetDV01Limit(netDV01Limit);
        account.setName(accountName);
        account.setNettingClearing(netting);
        if(stateStreetInternalID!=null){
            account.setSSGMID(stateStreetInternalID);
        }
        account.setCommission(commission);

        sendMsgFromSeq(account, true);
    }

    public boolean enableAccount(String accountName, boolean enabled) {
        return enable(accounts, accountName, enabled);
    }

    public boolean enableTrader(String traderName, boolean enabled) {
        return enable(traders, traderName, enabled);
    }

    public boolean enableSecurity(String securityName, boolean enabled) {
        return enable(securities, securityName, enabled);
    }
}
